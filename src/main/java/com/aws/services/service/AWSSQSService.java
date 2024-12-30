package com.aws.services.service;

import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;
import java.util.Map;

public interface AWSSQSService {
	ResponseEntity<String> sendMessage(String queueName, String message, String groupId);

	ResponseEntity<String> sendMessageViaS3(String queueName, String largeMessage, String groupId);

	ResponseEntity<Map<String, List<String>>> receiveMessage(String queueName, int maxNumberOfMessages);

	ResponseEntity<Map<String, List<String>>> receiveMessageViaS3(String queueName, int maxNumberOfMessages);

	void sendBatchMessages(SqsClient sqsClient, String queueUrl, List<String> messages);

	void deleteMessages(SqsClient sqsClient, String queueName, List<Message> messages);
}