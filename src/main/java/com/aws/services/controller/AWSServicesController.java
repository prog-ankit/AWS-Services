package com.aws.services.controller;

import com.amazonaws.Response;
import com.aws.services.service.impl.AWSS3ServiceImpl;
import com.aws.services.service.impl.AWSSQSServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
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

	@GetMapping("/send-files-to-sqs/")
	public void sendFilesToSqs() {
		awsSqsService.sendMessage("TESTING","ezadmin_integration_apigateway_inbound_physician_fail_queue.fifo");
	}

	@GetMapping("/list-s3-buckets")
	public void listS3Buckets() {
		awss3Service.listS3Buckets();
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
