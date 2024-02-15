package com.bundee.msfw.interfaces.os;

import com.bundee.msfw.defs.BExceptions;
import com.bundee.msfw.interfaces.blmodi.HealthCheck;
import com.bundee.msfw.interfaces.logi.BLogger;

import java.io.InputStream;
import java.util.List;

public interface ObjectStoreService extends HealthCheck {

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
    void createObject(BLogger BLogger, String component, String key, byte[] content, boolean isInboxOperation) throws BExceptions;

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
    List<String> listObjects(BLogger logger, String component, boolean isInboxOperation) throws BExceptions;

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
    void deleteObject(BLogger logger, String component, String key, boolean isInboxOperation) throws BExceptions;

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
    InputStream readObject(BLogger logger, String component, String key, boolean isInboxOperation) throws BExceptions;
}
