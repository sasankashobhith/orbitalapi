package com.bundee.ums.utils;

import com.azure.storage.blob.*;
import com.azure.storage.blob.specialized.*;
import com.bundee.ums.pojo.*;

import java.io.*;
import java.util.*;

public class ImageUploadUtil {

    public static Image uploadImage(String base64String, int userid) throws IOException {
        // Azure Blob Storage connection string
        Image image=new Image();
        String connectionString = "DefaultEndpointsProtocol=https;AccountName=d64supplychain1;AccountKey=Kz44jKH9SOqX4GngCkZa3nW/6IzixPc+p7etiQYCPTmroylDwfh+vQiVigm1K4JIyvhqISMywYIz+AStVDKV9g==;EndpointSuffix=core.windows.net";
        // Container name in Azure Blob Storage
        String containerName = "bundeeimage";
        byte[] imageBytes = Base64.getDecoder().decode(base64String);
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        UUID uuid = UUID.randomUUID();
        String guidString = uuid.toString();
        // Optionally, you can remove hyphens from the GUID string
        guidString = guidString.replace("-", "");
        String blobName = "userprofile"+userid + "/" + guidString + ".jpg";
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        BlockBlobClient blockBlobClient = blobClient.getBlockBlobClient();
        // Upload the decoded image data to Azure Blob Storage
        blockBlobClient.upload(new ByteArrayInputStream(imageBytes), imageBytes.length, true);
        System.out.println("Image uploaded to Azure Blob Storage.");
        String blobURL = containerClient.getBlobClient(blobName).getBlobUrl();
        System.out.println("Blob URL: " + blobURL);
        image.setImageId(guidString);
        image.setImageUrl(blobURL);
        return image;
    }
}
