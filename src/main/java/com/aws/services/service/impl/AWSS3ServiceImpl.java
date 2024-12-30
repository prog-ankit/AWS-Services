package com.aws.services.service.impl;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.aws.services.config.ApplicationProperties;
import com.aws.services.service.AWSS3Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class AWSS3ServiceImpl implements AWSS3Service {

	private static final Logger LOGGER = LogManager.getLogger(AWSS3ServiceImpl.class);

	@Autowired
	S3Client s3Client;

	@Autowired
	ApplicationProperties applicationProperties;

	S3AsyncClient s3AsyncClient;
	S3TransferManager s3TransferManager;

	AWSS3ServiceImpl(S3AsyncClient s3AsyncClient) {
		this.s3AsyncClient = s3AsyncClient;
		s3TransferManager = S3TransferManager.builder()
				.s3Client(s3AsyncClient)
				.build();
	}

	/**
	 * List all the buckets from S3.
	 */
	@Override
	public void listS3Buckets() {
		try {
			ListBucketsResponse response = s3Client.listBuckets();
			List<Bucket> bucketList = response.buckets();
			bucketList.forEach(bucket -> LOGGER.info("Bucket Name: {}",  bucket.name()));
		} catch (S3Exception e) {
			LOGGER.error(e.awsErrorDetails().errorMessage());
			System.exit(1);
		}
	}

	/**
	 * Sends a message to the specified AWS SQS queue.
	 *
	 * @param s3Path The path where the file needs to be uploaded.
	 * @param localPath The local system path from where file has to be read.
	 */
	@Override
	public void uploadFile(String s3Path, String localPath) {
		try {
			Path path = Paths.get(localPath);
			if (!Files.exists(path)) {
				LOGGER.error("Local file does not exist at path: {}", localPath);
				return;
			}

			long fileSize = Files.size(path);
			LOGGER.info("Uploading file of size: {} bytes", fileSize);
			String fileName = path.getFileName().toString();
			if(s3Path.endsWith("/")) {
				s3Path +=fileName;
			} else {
				s3Path += "/" + fileName;
			}
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
					.bucket(applicationProperties.getS3Bucket())
					.key(s3Path)
					.build();

			PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, RequestBody.fromFile(path));

			LOGGER.info("File Uploaded Success. ETag: {}", putObjectResponse.eTag());

		} catch (S3Exception e) {
			LOGGER.error("Failed to upload file to S3. Error: {}", e.awsErrorDetails().errorMessage());
		} catch (Exception e) {
			LOGGER.error("Unexpected error occurred during file upload: {}", e.getMessage());
		}
	}


	/**
	 * Sends a message asynchronously to the specified AWS SQS queue.
	 *
	 * @param s3Path The path where the file needs to be uploaded.
	 * @param localPath The local system path from where file has to be read.
	 */
	@Override
	public void uploadFileAsync(String s3Path, String localPath) {
		Path path = Paths.get(localPath);
		String fileName = path.getFileName().toString();
		if(s3Path.endsWith("/")) {
			s3Path +=fileName;
		} else {
			s3Path += "/" + fileName;
		}
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(applicationProperties.getS3Bucket())
				.key(s3Path)
				.build();
		CompletableFuture<PutObjectResponse> response = s3AsyncClient.putObject(putObjectRequest,AsyncRequestBody.fromFile(Paths.get(localPath)));
		if(response.isDone()) {
			try {
				LOGGER.info(response.get().eTag());
			} catch (S3Exception | ExecutionException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Sends a message asynchronously to the specified AWS SQS queue.
	 *
	 * @param s3Path The path where the file needs to be uploaded.
	 * @param fileName The name of the file that needs to be set for the data that will be uploaded.
	 * @param fileExtension The extension of the file that needs to be set for the file.
	 * @param data The data of the file
	 */
	@Override
	public void uploadData(String s3Path, String fileName, String fileExtension, String data) {
		if (null != data) {
			try {
				Path tempFile  = Files.createTempFile(fileName, fileExtension);
				Files.write(tempFile, data.getBytes());
				PutObjectRequest putObjectRequest = PutObjectRequest.builder()
						.bucket(applicationProperties.getS3Bucket())
						.key(s3Path)
						.build();
				s3Client.putObject(putObjectRequest,tempFile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Downloads a file from the specified bucket
	 *
	 * @param key The path where the file needs to be uploaded.
	 * @param destinationPath The name of the file that needs to be set for the data that will be uploaded.
	 * @param fileName The name of the file that needs to be set for the data that will be uploaded.
	 */
	@Override
	public void downloadFile(String key, String destinationPath, String fileName) {
		String fileExtension = key.substring(key.lastIndexOf("."));
		if(destinationPath == null) destinationPath = "/home/ankitbose/Learning/aws-store-sdk-testing/" + fileName + fileExtension;
		else {
			destinationPath += fileName;
		}
		DownloadFileRequest downloadFileRequest = DownloadFileRequest.builder()
				.getObjectRequest(b -> b.bucket(applicationProperties.getS3Bucket()).key(key))
				.destination(Paths.get(destinationPath))  // Specify destination file path
				.build();
		FileDownload downloadFile = s3TransferManager.downloadFile(downloadFileRequest);
		CompletedFileDownload downloadResult = downloadFile.completionFuture().join();
		LOGGER.info("File {} Downloaded in {} and tag: {}",fileName,destinationPath, downloadResult.response().eTag());
	}

	/**
	 * Get the metadata of the s3 path's object.
	 * @param key Specifies the s3 path of the object whose metadata is to be found.
	 * @return Metadata of the object whether the request is succeed or failed.
	 */
	@Override
	public ResponseEntity<Map<String, HeadObjectResponse>> getObjectMetadata(String key) {
		Map<String, HeadObjectResponse> response = new HashMap<>();
		try {
			HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
					.bucket(applicationProperties.getS3Bucket())
					.key(key)
					.build();
			HeadObjectResponse headObject = s3Client.headObject(headObjectRequest);
			LOGGER.info("Successfully retrieved {}/{} of type {}", applicationProperties.getS3Bucket(),key,headObject.contentType());
			response.put("Success",headObject);
			return ResponseEntity.ok(response);
		} catch (NoSuchKeyException e) {
			response.put("Invalid Key",null);
			return ResponseEntity.status(404).body(response);
		}
	}

	/**
	 * Lists all objects in the specified path in s3.
	 *
	 * @param bucketName The bucket name for which you want to list the objects
	 * @param destinationPath The local path where you want to download the data
	 * @param prefix The path within the s3 bucket.
	 * @param isDownload If you want to download the file then true, otherwise false
	 *
	 */
	@Override
	public Map<String,List<String>> listAllObjects(String bucketName, String prefix, String destinationPath, boolean isDownload) {
		Map<String,List<String>> response = new HashMap<>();
		boolean isTruncated;
		String continuationToken = null;
		do {
			ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
					.bucket(bucketName)
					.prefix(prefix)
					.continuationToken(continuationToken)
					.build();
			ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsRequest);
			isTruncated = listObjectsV2Response.isTruncated();
			continuationToken = listObjectsV2Response.nextContinuationToken();
			List<S3Object> s3Objects  = listObjectsV2Response.contents();
			if (!isDownload) {
				List<String> s3ObjectsInPath = new ArrayList<>();
				for (S3Object s3Object : s3Objects) {
					s3ObjectsInPath.add(s3Object.key());
				}
				response.put("false",s3ObjectsInPath);
			} else {
				if(!destinationPath.endsWith("/")) {
					destinationPath += "/";
				}
				for (S3Object s3Object : s3Objects) {
					downloadFile(s3Object.key(),destinationPath,s3Object.key().replace(prefix,""));
				}
				response.put("true",null);
			}
		} while(isTruncated);
		return response;
	}
}
