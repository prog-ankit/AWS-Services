package com.aws.services.service;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;

public interface AWSSQSService {
	void sendMessage(String queueName, String message);

	void sendMessageViaS3(String queueName, String largeMessage);

	List<Message> receiveMessage(String queueName, int maxNumberOfMessages);

	List<Message> receiveMessageViaS3(String queueName, int maxNumberOfMessages);

	void sendBatchMessages(SqsClient sqsClient, String queueUrl, List<String> messages);

	void deleteMessages(SqsClient sqsClient, String queueName, List<Message> messages);
}