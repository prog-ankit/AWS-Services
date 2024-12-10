package com.aws.services.service;

import java.util.List;
import java.util.Map;

public interface AWSS3Service {
	void listS3Buckets();
	void uploadFile(String s3Path, String path);
	void uploadFileAsync(String s3Path, String path);
	void uploadData(String s3Path, String fileName, String fileExtension, String data);
	void downloadFile(String key, String destinationPath, String fileName);
	void copyObjects(String sourceBucket, String sourceKey, String destinationBucket, String destinationKey);
	Map<String,List<String>> listAllObjects(String bucketName, String prefix, String destinationPath, boolean isDownload);
}