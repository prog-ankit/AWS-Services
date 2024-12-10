package com.aws.services.service.impl;

import com.amazon.sqs.javamessaging.AmazonSQSExtendedClient;
import com.amazon.sqs.javamessaging.ExtendedClientConfiguration;
import com.aws.services.config.ApplicationProperties;
import com.aws.services.service.AWSSQSService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.model.*;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import java.util.List;
import java.util.UUID;

@Service
public class AWSSQSServiceImpl implements AWSSQSService {

	private static Logger LOGGER = LogManager.getLogger(AWSSQSServiceImpl.class);

	@Autowired
	SqsClient sqsClient;
	S3Client s3Client;
	ApplicationProperties applicationProperties;
	SqsClient sqsClientExtended;

	//Here, the constructor extends the SQS in order to upload large messag to SQS that > 256KB.
	public AWSSQSServiceImpl(S3Client s3Client, ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
		this.s3Client = s3Client;
		ExtendedClientConfiguration extendedClientConfig = new ExtendedClientConfiguration()
				.withPayloadSupportEnabled(s3Client, applicationProperties.getS3Bucket());
		sqsClientExtended = new AmazonSQSExtendedClient(sqsClient,extendedClientConfig);
	}

	/**
	 * Sends a message to the specified AWS SQS queue.
	 *
	 * @param queueName The name of the SQS queue to which the message should be sent.
	 * @param message   The message body to be sent to the SQS queue.
	 */
	public void sendMessage(String queueName, String message) {
		GetQueueUrlResponse getQueueUrlResponse = sqsClient.getQueueUrl(
				GetQueueUrlRequest.builder()
						.queueName(queueName)
						.build()
		);
		String queueUrl = getQueueUrlResponse.queueUrl();
		SendMessageRequest sendMessageRequest = SendMessageRequest
				.builder()
				.queueUrl(queueUrl)
				.messageBody(message)
				.build();

		SendMessageResponse sendMessageResponse = sqsClient.sendMessage(sendMessageRequest);
		LOGGER.info("Message Sent Success: {}", sendMessageResponse.messageId());
	}

	/**
	 * Sends a message to the specified AWS SQS queue.
	 *
	 * @param queueName The name of the SQS queue to which the message should be sent.
	 * @param largeMessage   The message body that is > 256 KB to be sent to the SQS queue.
	 */
	public void sendMessageViaS3(String queueName, String largeMessage) {
		GetQueueUrlResponse getQueueUrlResponse = sqsClient.getQueueUrl(
				GetQueueUrlRequest.builder()
						.queueName(queueName)
						.build()
		);
		String queueUrl = getQueueUrlResponse.queueUrl();
		SendMessageRequest sendMessageRequest = SendMessageRequest
				.builder()
				.queueUrl(queueUrl)
				.messageBody(largeMessage)
				.build();
		SendMessageResponse sendMessageResponse = sqsClientExtended.sendMessage(sendMessageRequest);
		LOGGER.info("Message Sent Success: {}",sendMessageResponse.messageId());
	}

	/**
	 * Sends a messages in batch to the specified AWS SQS queue. Max number of message in batch(10).
	 *
	 * @param sqsClient The name of the SQS queue to which the message should be sent.
	 * @param queueUrl  The name of the SQS queue to which the message should be sent.
	 * @param messages The list of messages in SendMessageBatchRequestEntry type.
	 */
	public void sendBatchMessages(SqsClient sqsClient, String queueUrl, List<String> messages) {
		LOGGER.info("Sending multiple messages");
		List<SendMessageBatchRequestEntry> batchMessages = messages.stream()
				.map(message -> SendMessageBatchRequestEntry.builder()
						.id(UUID.randomUUID().toString())
						.messageBody(message)
						.build())
				.toList();
		try {
			SendMessageBatchRequest sendMessageBatchRequest =
					SendMessageBatchRequest.builder()
							.queueUrl(queueUrl)
							.entries(batchMessages)
							.build();
			sqsClient.sendMessageBatch(sendMessageBatchRequest);
		} catch (SqsException e) {
			LOGGER.error(e.awsErrorDetails().errorMessage());
			System.exit(1);
		}
	}

	/**
	 * Sends a message to the specified AWS SQS queue.
	 *
	 * @param queueName The name of the SQS queue to which the message should be sent.
	 * @param maxNumberOfMessages  Number of messages to be retrieved from the queue.
	 */
	public List<Message> receiveMessage(String queueName, int maxNumberOfMessages) {
		GetQueueUrlResponse getQueueUrlResponse = sqsClient.getQueueUrl(GetQueueUrlRequest.builder().build());
		String queueURL = getQueueUrlResponse.queueUrl();
		ReceiveMessageResponse receiveMessageResponse = sqsClient.receiveMessage(ReceiveMessageRequest.builder().queueUrl(queueURL).maxNumberOfMessages(maxNumberOfMessages).build());
		return receiveMessageResponse.messages();
	}

	/**
	 * Sends a message to the specified AWS SQS queue.
	 *
	 * @param queueName The name of the SQS queue to which the message should be sent.
	 * @param maxNumberOfMessages  Number of messages(size greater than 256 KB) to be retrieved from the queue.
	 */
	@Override
	public List<Message> receiveMessageViaS3(String queueName, int maxNumberOfMessages) {
		GetQueueUrlResponse getQueueUrlResponse = sqsClient.getQueueUrl(GetQueueUrlRequest.builder().build());
		String queueURL = getQueueUrlResponse.queueUrl();
		ReceiveMessageResponse receiveMessageResponse = sqsClientExtended.receiveMessage(ReceiveMessageRequest.builder().queueUrl(queueURL).maxNumberOfMessages(maxNumberOfMessages).build());
		return receiveMessageResponse.messages();
	}

	/**
	 * Purge the specified queue
	 * @param sqsClient
	 * @param queueName
	 * @param messages
	 */
	@Override
	public void deleteMessages(SqsClient sqsClient, String queueName,
			List<Message> messages) {
		LOGGER.info("\nDelete Messages");
		GetQueueUrlResponse getQueueUrlResponse = sqsClient.getQueueUrl(
				GetQueueUrlRequest.builder()
						.queueName(queueName)
						.build()
		);
		String queueUrl = getQueueUrlResponse.queueUrl();
		try {
			for (Message message : messages) {
				DeleteMessageRequest deleteMessageRequest =
						DeleteMessageRequest.builder()
								.queueUrl(queueUrl)
								.receiptHandle(message.receiptHandle())
								.build();
				sqsClient.deleteMessage(deleteMessageRequest);
			}
		} catch (SqsException e) {
			LOGGER.error(e.awsErrorDetails().errorMessage());
			System.exit(1);
		}
	}



}


