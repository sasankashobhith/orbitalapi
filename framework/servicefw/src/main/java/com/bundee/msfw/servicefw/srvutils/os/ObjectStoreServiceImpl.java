package com.bundee.msfw.servicefw.srvutils.os;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.bundee.msfw.servicefw.srvutils.utils.FwConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.defs.HealthDetails;
import com.bundee.msfw.defs.ProcessingCode;
import com.bundee.msfw.interfaces.blmodi.BLModServices;
import com.bundee.msfw.interfaces.fcfgi.FileCfgHandler;
import com.bundee.msfw.interfaces.logi.BLogger;
import com.bundee.msfw.interfaces.os.ObjectStoreService;
import com.bundee.msfw.interfaces.vault.VaultService;
import com.bundee.msfw.servicefw.srvutils.utils.ServiceIniter;

public class ObjectStoreServiceImpl implements ObjectStoreService, ServiceIniter {
	private final String OS_SVC_REST_CLIENT = "ObjectStoreServiceRESTCLient";
	
    private final Map<String, OSProperties> osS3PropertiesMap = new HashMap<>();
    private SSLConnectionSocketFactory sslFactory;

    @Override
    public void init(BLogger logger, FileCfgHandler fch, VaultService vaultService, BLModServices blModServices) throws BExceptions {
        if (fch != null) {
            logger.info("Starting load for All S3 Properties");
            String[] osS3PropertiesToLoad = fch.getCfgParamStr(OSProperty.OS_PROPERTY.getValue()).split(",");
            if (osS3PropertiesToLoad != null && osS3PropertiesToLoad.length > 0 && !osS3PropertiesToLoad[0].equals("")) {
                for (String osS3PropertyToLoad : osS3PropertiesToLoad) {
                    HashMap<String, Object> map = (HashMap<String, Object>) fch.getAllCfgParams()
                            .entrySet().stream().filter(x -> x.getKey()
                                    .contains(osS3PropertyToLoad))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    OSProperties osProperties = new OSProperties();
                    logger.info("Loading S3 Properties for " + osS3PropertyToLoad + " connectivity");
                    setPropertiesToObject(logger, vaultService, osS3PropertyToLoad, map, osProperties);
                    osS3PropertiesMap.put(osS3PropertyToLoad, osProperties);

                    if (sslFactory == null) {
                        String cfgPfx = osProperties.getCfgPfx();
                        Object facObj = blModServices.getRESTClientFactory().getSSLFactory(logger, OS_SVC_REST_CLIENT, cfgPfx, blModServices);
                        if (facObj instanceof SSLConnectionSocketFactory) {
                            sslFactory = (SSLConnectionSocketFactory) facObj;
                        }
                    }
                }
                logger.info("Loaded all S3 Connectivity Properties");
            } else {
                logger.info("No S3 properties found to configure AmazonS3 Client");
            }
        }
    }

    private void setPropertiesToObject(BLogger logger, VaultService vaultService, String osS3PropertyToLoad, HashMap<String, Object> map, OSProperties osProperties) throws BExceptions {
        osProperties.setAccessKey(vaultService.getValue(logger, getPropertyValue(map, osS3PropertyToLoad, OSProperty.OS_ACCESSKEY)));
        osProperties.setSecretKey(vaultService.getValue(logger, getPropertyValue(map, osS3PropertyToLoad, OSProperty.OS_SECRETKEY)));
        osProperties.setS3EndPoint(getPropertyValue(map, osS3PropertyToLoad, OSProperty.OS_ENDPOINT));
        osProperties.setS3Region(getPropertyValue(map, osS3PropertyToLoad, OSProperty.OS_REGION));
        osProperties.setInboxBucketName(getPropertyValue(map, osS3PropertyToLoad, OSProperty.OS_INBOXBUCKET));
        osProperties.setOutboxBucketName(getPropertyValue(map, osS3PropertyToLoad, OSProperty.OS_OUTBOXBUCKET));
        osProperties.setCfgPfx(getPropertyValue(map, osS3PropertyToLoad, OSProperty.OS_CFG_PFX));
        validateProperties(logger, osS3PropertyToLoad, osProperties);
    }

    private void validateProperties(BLogger logger, String osS3PropertyToLoad, OSProperties osProperties) throws BExceptions {
        if (StringUtils.isBlank(osProperties.getAccessKey())) {
            throw new BExceptions(FwConstants.PCodes.MANDATORY_FIELD_MISSING, "Some Properties for Object Store Connectivity Missing: " + OSProperty.OS_ACCESSKEY.getValue());
        } else if (StringUtils.isBlank(osProperties.getSecretKey())) {
            throw new BExceptions(FwConstants.PCodes.MANDATORY_FIELD_MISSING, "Some Properties for Object Store Connectivity Missing: " + OSProperty.OS_SECRETKEY.getValue());
        } else if (StringUtils.isBlank(osProperties.getS3EndPoint())) {
            throw new BExceptions(FwConstants.PCodes.MANDATORY_FIELD_MISSING, "Some Properties for Object Store Connectivity Missing: " + OSProperty.OS_ENDPOINT.getValue());
        } else if (StringUtils.isBlank(osProperties.getS3Region())) {
            throw new BExceptions(FwConstants.PCodes.MANDATORY_FIELD_MISSING, "Some Properties for Object Store Connectivity Missing: " + OSProperty.OS_REGION.getValue());
        } else if (StringUtils.isBlank(osProperties.getInboxBucketName())) {
            throw new BExceptions(FwConstants.PCodes.MANDATORY_FIELD_MISSING, "Some Properties for Object Store Connectivity Missing: " + OSProperty.OS_INBOXBUCKET.getValue());
        } else if (StringUtils.isBlank(osProperties.getOutboxBucketName())) {
            throw new BExceptions(FwConstants.PCodes.MANDATORY_FIELD_MISSING, "Some Properties for Object Store Connectivity Missing: " + OSProperty.OS_OUTBOXBUCKET.getValue());
        } else {
            logger.info("All Object Store config parameters loaded for " + osS3PropertyToLoad);
        }
    }

    private String getPropertyValue(HashMap<String, Object> map, String osS3PropertyToLoad, OSProperty key) {
        String appender = ".";
        return (String) map.get(osS3PropertyToLoad + appender + key.getValue());
    }

    /**
     * This method helps to create a file in the Amazon S3 Object Store
     * using the config source component, with the key given on inbox or outbox based on flag
     *
     * @param BLogger        logger to add logs on statuses
     * @param component        name of the config to retrieve Object store details
     * @param key              key to be used to create the Object in Object Store
     * @param content          Byte Array of data to be created in object store
     * @param isInboxOperation flag to pick either inbox or outbox true for Inbox and false for outbox
     * @throws BExceptions on exception
     */
    @Override
    public void createObject(BLogger BLogger, String component, String key, byte[] content, boolean isInboxOperation) throws BExceptions {

        if (content == null || content.length == 0) {
            throw new BExceptions(FwConstants.PCodes.MANDATORY_FIELD_MISSING, "Data to be created to S3 Bucket missing content");
        }
        OSProperties osProperties = getOSProperties(component);
        String bucketName = getBucketName(isInboxOperation, osProperties);
        try {
            AmazonS3 s3Client = getSSLS3Client(BLogger, osProperties);
            try (InputStream is = new ByteArrayInputStream(content)) {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType("text/plain");
                metadata.setContentLength(content.length);
                s3Client.putObject(bucketName, key, is, metadata);
            }
        } catch (Exception exception) {
            BLogger.error(exception);
            BLogger.error(String.format("Cannot create Object in the bucket: [%s,%s]", bucketName, key));
            throw new BExceptions(exception, FwConstants.PCodes.OBJECT_STORE_SERVICE_FAILURE);
        }
    }

    private String getBucketName(boolean isInboxOperation, OSProperties osProperties) {
        return isInboxOperation ? osProperties.getInboxBucketName() : osProperties.getOutboxBucketName();
    }

    private OSProperties getOSProperties(String component) throws BExceptions {
        OSProperties osProperties = osS3PropertiesMap.get(component);
        if (osProperties == null) {
            throw new BExceptions(FwConstants.PCodes.INVALID_VALUE, "S3 Component Name given is invalid: " + component);
        }
        return osProperties;
    }

    /**
     * This method lists out all the keys of objects stored in object store based
     * on config source component for the inbox/outbox based on flag
     *
     * @param logger           logger to add logs on statuses
     * @param component        name of the config to retrieve Object store details
     * @param isInboxOperation flag to pick either inbox or outbox true for Inbox and false for outbox
     * @return List of Object Store Keys for files stored
     * @throws BExceptions on exception
     */
    @Override
    public List<String> listObjects(BLogger logger, String component, boolean isInboxOperation) throws BExceptions {
        OSProperties osProperties = getOSProperties(component);
        String bucketName = getBucketName(isInboxOperation, osProperties);
        try {
            List<String> keyList = new ArrayList<>();
            AmazonS3 s3Client = getSSLS3Client(logger, osProperties);

            ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName);
            ObjectListing objectListing = s3Client.listObjects(listObjectsRequest);
            List<S3ObjectSummary> s3ObjectSummary = objectListing.getObjectSummaries();
            if (CollectionUtils.isNotEmpty(s3ObjectSummary)) {
                keyList.addAll(s3ObjectSummary.stream().map(S3ObjectSummary::getKey).collect(Collectors.toList()));
            }
            return keyList;

        } catch (Exception exception) {
            logger.error(exception);
            logger.error(String.format("Cannot List Objects from the bucket: %s", bucketName));
            throw new BExceptions(exception, FwConstants.PCodes.OBJECT_STORE_SERVICE_FAILURE);
        }
    }


    /**
     * This method deletes Object store file/object based on key given for
     * the specific component of Object Store and the bucket chosen based on flag
     *
     * @param logger           logger to add logs on statuses
     * @param component        name of the config to retrieve Object store details
     * @param key              key to be used to create the Object in Object Store
     * @param isInboxOperation flag to pick either inbox or outbox true for Inbox and false for outbox
     * @throws BExceptions on exception
     */
    @Override
    public void deleteObject(BLogger logger, String component, String key, boolean isInboxOperation) throws BExceptions {
        OSProperties osProperties = getOSProperties(component);
        String bucketName = getBucketName(isInboxOperation, osProperties);
        try {
            AmazonS3 s3Client = getSSLS3Client(logger, osProperties);
            s3Client.deleteObject(bucketName, key);
        } catch (Exception exception) {
            logger.error(exception);
            logger.error(String.format("Failed to delete Object [%s,%s]", bucketName, key));
            throw new BExceptions(exception, FwConstants.PCodes.OBJECT_STORE_SERVICE_FAILURE);
        }
    }


    /**
     * This method reads the object data from Amazon Object store using the component given
     * for the given key, using inbox or outbox bucket based on flag
     *
     * @param logger           logger to add logs on statuses
     * @param component        name of the config to retrieve Object store details
     * @param key              key to be used to create the Object in Object Store
     * @param isInboxOperation flag to pick either inbox or outbox true for Inbox and false for outbox
     * @return Input Stream of Object from Object Store
     * @throws BExceptions on exception
     */
    @Override
    public InputStream readObject(BLogger logger, String component, String key, boolean isInboxOperation) throws BExceptions {
        OSProperties osProperties = getOSProperties(component);
        String bucketName = getBucketName(isInboxOperation, osProperties);
        try {
            AmazonS3 s3Client = getSSLS3Client(logger, osProperties);
            S3Object object = s3Client.getObject(bucketName, key);
            return object.getObjectContent();
        } catch (Exception exception) {
            logger.error(exception);
            logger.error(String.format("Failed to read Object [%s,%s]", bucketName, key));
            throw new BExceptions(exception, FwConstants.PCodes.OBJECT_STORE_SERVICE_FAILURE);
        }
    }

    private AmazonS3ClientBuilder createS3Client(OSProperties osProperties) {
        return AmazonS3Client.builder()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(osProperties.getS3EndPoint(), osProperties.getS3Region()))
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(osProperties.getAccessKey(), osProperties.getSecretKey())
                ))
                .withPathStyleAccessEnabled(true);
    }

    public AmazonS3 getSSLS3Client(BLogger logger, OSProperties osProperties) throws BExceptions {
        ClientConfiguration conf = new ClientConfiguration();
        conf.getApacheHttpClientConfig().setSslSocketFactory(sslFactory);
        return createS3Client(osProperties).withClientConfiguration(conf).build();
    }

    @Override
    public HealthDetails checkHealth(BLogger logger) {
        HealthDetails hd = new HealthDetails();

        osS3PropertiesMap.forEach((key, value) -> {
            try {
                String url = value.getS3EndPoint();
                try {
                    listObjects(logger, key, true);
                    hd.add(ProcessingCode.SUCCESS_PC, ProcessingCode.SUCCESS_KEY, url);
                } catch (BExceptions e) {
                    logger.error(e);
                    hd.add(e.getCode(), e.getMessage(), url);
                }
            } catch (Exception e1) {
                logger.error(e1);
                hd.add(FwConstants.PCodes.OBJECT_STORE_SERVICE_FAILURE, e1.getMessage(), value.getS3EndPoint());
            }
        });

        return hd;
    }

}
