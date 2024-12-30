package com.aws.services.service.impl;

import com.amazon.sqs.javamessaging.AmazonSQSExtendedClient;
import com.amazon.sqs.javamessaging.ExtendedClientConfiguration;
import com.aws.services.config.ApplicationProperties;
import com.aws.services.service.AWSSQSService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AWSSQSServiceImpl implements AWSSQSService {

	private static Logger LOGGER = LogManager.getLogger(AWSSQSServiceImpl.class);

	SqsClient sqsClient;
	S3Client s3Client;
	ApplicationProperties applicationProperties;
	SqsClient sqsClientExtended;

	//Here, the constructor extends the SQS in order to upload large message to SQS that > 256KB.
	public AWSSQSServiceImpl(SqsClient sqsClient, S3Client s3Client, ApplicationProperties applicationProperties) {
		this.sqsClient = sqsClient;
		this.applicationProperties = applicationProperties;
		this.s3Client = s3Client;
		ExtendedClientConfiguration extendedClientConfig = new ExtendedClientConfiguration().withPayloadSupportEnabled(
				s3Client, applicationProperties.getS3Bucket());
		sqsClientExtended = new AmazonSQSExtendedClient(sqsClient, extendedClientConfig);
	}

	/**
	 * Sends a message to the specified AWS SQS queue.
	 *
	 * @param queueName The name of the SQS queue to which the message should be sent.
	 * @param message   The message body to be sent to the SQS queue.
	 */
	public ResponseEntity<String> sendMessage(String queueName, String message, String groupId) {
		try {
			GetQueueUrlResponse getQueueUrlResponse = sqsClient.getQueueUrl(
					GetQueueUrlRequest.builder().queueName(queueName).build());
			String queueUrl = getQueueUrlResponse.queueUrl();
			if (groupId == null)
				groupId = UUID.randomUUID().toString();
			SendMessageRequest sendMessageRequest = SendMessageRequest.builder().queueUrl(queueUrl)
					.messageGroupId(groupId).messageBody(message).build();

			SendMessageResponse sendMessageResponse = sqsClient.sendMessage(sendMessageRequest);
			LOGGER.info("Message Sent Success: {}", sendMessageResponse.messageId());
			return ResponseEntity.ok("Message Sent");
		} catch (QueueDoesNotExistException e) {
			return ResponseEntity.status(400).body("Invalid Queue Name");
		} catch (SqsException e) {
			if (e.toString().contains("Message must be shorter")) {
				return ResponseEntity.status(500).body("Message shorter than 256 kb can be sent");
			}
		}
		return ResponseEntity.status(404).body("Something Went Wrong. Check Logs");

	}
	//

	/**
	 * Sends a message to the specified AWS SQS queue.
	 *
	 * @param queueName    The name of the SQS queue to which the message should be sent.
	 * @param largeMessage The message body that is > 256 KB to be sent to the SQS queue.
	 */
	public ResponseEntity<String> sendMessageViaS3(String queueName, String largeMessage, String groupId) {
		try {
			GetQueueUrlResponse getQueueUrlResponse = sqsClient.getQueueUrl(
					GetQueueUrlRequest.builder().queueName(queueName).build());
			String queueUrl = getQueueUrlResponse.queueUrl();
			if (groupId == null)
				groupId = UUID.randomUUID().toString();

			SendMessageRequest sendMessageRequest = SendMessageRequest.builder().queueUrl(queueUrl)
					.messageGroupId(groupId).messageBody(largeMessage).build();
			SendMessageResponse sendMessageResponse = sqsClientExtended.sendMessage(sendMessageRequest);
			LOGGER.info("Message Sent Success: {}", sendMessageResponse.messageId());
			return ResponseEntity.ok("Message Sent");
		} catch (QueueDoesNotExistException e) {
			return ResponseEntity.status(400).body("Invalid Queue Name");
		}
	}

	/**
	 * Sends a messages in batch to the specified AWS SQS queue. Max number of message in batch(10).
	 *
	 * @param sqsClient The name of the SQS queue to which the message should be sent.
	 * @param queueUrl  The name of the SQS queue to which the message should be sent.
	 * @param messages  The list of messages in SendMessageBatchRequestEntry type.
	 */
	public void sendBatchMessages(SqsClient sqsClient, String queueUrl, List<String> messages) {
		LOGGER.info("Sending multiple messages");
		List<SendMessageBatchRequestEntry> batchMessages = messages.stream()
				.map(message -> SendMessageBatchRequestEntry.builder().id(UUID.randomUUID().toString())
						.messageBody(message).build()).toList();
		try {
			SendMessageBatchRequest sendMessageBatchRequest = SendMessageBatchRequest.builder().queueUrl(queueUrl)
					.entries(batchMessages).build();
			sqsClient.sendMessageBatch(sendMessageBatchRequest);
		} catch (SqsException e) {
			LOGGER.error(e.awsErrorDetails().errorMessage());
			System.exit(1);
		}
	}

	/**
	 * Sends a message to the specified AWS SQS queue.
	 *
	 * @param queueName           The name of the SQS queue to which the message should be sent.
	 * @param maxNumberOfMessages Number of messages to be retrieved from the queue.
	 */
	public ResponseEntity<Map<String, List<String>>> receiveMessage(String queueName, int maxNumberOfMessages) {
		Map<String, List<String>> response = new HashMap<>();
		try {
			GetQueueUrlResponse getQueueUrlResponse = sqsClient.getQueueUrl(
					GetQueueUrlRequest.builder().queueName(queueName).build());
			String queueURL = getQueueUrlResponse.queueUrl();
			ReceiveMessageResponse receiveMessageResponse = sqsClient.receiveMessage(
					ReceiveMessageRequest.builder().queueUrl(queueURL).maxNumberOfMessages(maxNumberOfMessages)
							.build());
			List<String> messageResponse = receiveMessageResponse.messages().stream().map(Message::body).toList();
			response.put("Success", messageResponse);
			return ResponseEntity.ok(response);
		} catch (QueueDoesNotExistException e) {
			response.put("Invalid Queue Name", null);
			return ResponseEntity.status(400).body(response);
		}
	}

	/**
	 * Sends a message to the specified AWS SQS queue.
	 *
	 * @param queueName           The name of the SQS queue to which the message should be sent.
	 * @param maxNumberOfMessages Number of messages(size greater than 256 KB) to be retrieved from the queue.
	 */
	@Override
	public ResponseEntity<Map<String, List<String>>> receiveMessageViaS3(String queueName, int maxNumberOfMessages) {
		Map<String, List<String>> response = new HashMap<>();
		try {
			GetQueueUrlResponse getQueueUrlResponse = sqsClient.getQueueUrl(
					GetQueueUrlRequest.builder().queueName(queueName).build());
			String queueURL = getQueueUrlResponse.queueUrl();
			ReceiveMessageResponse receiveMessageResponse = sqsClientExtended.receiveMessage(
					ReceiveMessageRequest.builder().queueUrl(queueURL).maxNumberOfMessages(maxNumberOfMessages)
							.build());
			List<String> responseMessage = receiveMessageResponse.messages().stream().map(Message::body
			).toList();
			response.put("Success", responseMessage);
			return ResponseEntity.ok(response);
		} catch (QueueDoesNotExistException e) {
			response.put("Invalid Queue Name", null);
			return ResponseEntity.status(400).body(response);
		}
	}

	/**
	 * Purge the specified queue
	 *
	 * @param sqsClient
	 * @param queueName
	 * @param messages
	 */
	@Override
	public void deleteMessages(SqsClient sqsClient, String queueName, List<Message> messages) {
		LOGGER.info("\nDelete Messages");
		GetQueueUrlResponse getQueueUrlResponse = sqsClient.getQueueUrl(
				GetQueueUrlRequest.builder().queueName(queueName).build());
		String queueUrl = getQueueUrlResponse.queueUrl();
		try {
			for (Message message : messages) {
				DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder().queueUrl(queueUrl)
						.receiptHandle(message.receiptHandle()).build();
				sqsClient.deleteMessage(deleteMessageRequest);
			}
		} catch (SqsException e) {
			LOGGER.error(e.awsErrorDetails().errorMessage());
			System.exit(1);
		}
	}

}


