package com.aws.services.controller;

import com.aws.services.service.impl.AWSS3ServiceImpl;
import com.aws.services.service.impl.AWSSQSServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class AWSServicesController {

	@Autowired
	private AWSSQSServiceImpl awsSqsService;

	@Autowired
	private AWSS3ServiceImpl awss3Service;


	AWSServicesController() {
		//default constructor
	}

	AWSServicesController(AWSSQSServiceImpl awsSqsService) {
		this.awsSqsService = awsSqsService;
	}

	//	SQS Service Controller
	@GetMapping("/send-message")
	public ResponseEntity<String> sendMessage(@RequestBody Map<String,String> messageRequest) {
		String message = messageRequest.get("message");
		String queueName = messageRequest.get("queueName");
		String groupId = messageRequest.get("groupId");
		return awsSqsService.sendMessage(queueName,message, groupId);
	}

	@GetMapping("/send-message-s3")
	public ResponseEntity<String> sendMessageViaS3(@RequestBody Map<String,String> messageRequest) {
		String message = messageRequest.get("message");
		String queueName = messageRequest.get("queueName");
		String groupId = messageRequest.get("groupId");
		return awsSqsService.sendMessageViaS3(queueName,message, groupId);
	}

	@GetMapping("/receive-message")
	public ResponseEntity<Map<String,List<String>>> receiveMessage(@RequestBody Map<String,String> messageRequest) {
		int maxMessages = Integer.parseInt(messageRequest.get("maxMessages"));
		String queueName = messageRequest.get("queueName");
		return awsSqsService.receiveMessage(queueName,maxMessages);
	}

	@GetMapping("/receive-message-s3")
	public ResponseEntity<Map<String,List<String>>> receiveMessageViaS3(@RequestBody Map<String,String> messageRequest) {
		int maxMessages = Integer.parseInt(messageRequest.get("maxMessages"));
		String queueName = messageRequest.get("queueName");
		return awsSqsService.receiveMessageViaS3(queueName,maxMessages);
	}

	//	S3 Service Controller
	@GetMapping("/list-s3-buckets")
	public void listS3Buckets() {
		awss3Service.listS3Buckets();
	}

	@GetMapping("/upload-file")
	public void uploadFile(@RequestBody Map<String,String> uploadRequest) {
		String s3Path = uploadRequest.get("s3Path");
		String filePath = uploadRequest.get("localPath");
		awss3Service.uploadFile(s3Path,filePath);
	}


	@GetMapping("/download-file")
	public void downloadFile(@RequestBody Map<String,String> downloadRequest) {
		String s3Path = downloadRequest.get("s3Path");
		String destinationDirectory = downloadRequest.get("destinationDirectory");
		awss3Service.downloadFile(s3Path,destinationDirectory, null);
	}

	@GetMapping("/list-all-objects")
	public Map<String, List<String>> listAllObjects(@RequestBody Map<String,Object> listObjectRequest) {
		String bucketName = String.valueOf(listObjectRequest.get("bucketName"));
		String prefix = String.valueOf(listObjectRequest.get("prefix"));
		boolean isDownload = (Boolean) listObjectRequest.get("isDownload");
		String destinationPath = String.valueOf(listObjectRequest.get("destinationPath"));
		return awss3Service.listAllObjects(bucketName, prefix,destinationPath,isDownload);
	}
}
