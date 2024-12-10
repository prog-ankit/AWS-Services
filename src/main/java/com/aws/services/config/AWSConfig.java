package com.aws.services.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class AWSConfig {

	ApplicationProperties applicationProperties;

	AWSConfig(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}

	@Bean
	public SqsClient sqsClient() {
		return SqsClient.builder().region(Region.of(applicationProperties.getRegion())).credentialsProvider(
				StaticCredentialsProvider.create(AwsBasicCredentials.create(applicationProperties.getAccessKeyId(),
						applicationProperties.getSecretAccessKey()))).build();
	}

	@Bean
	public S3Client s3Client() {
		/*DefaultCredentialsProvider looks for environment variables, java system properties or AWS Profile Configuration File for the configurations such as:
			AWS_ACCESS_KEY and AWS_SECRET_KEY, aws.accessKeyId and aws.secretKey
		* */
//		return S3Client.builder().region(Region.of("us-east-1"))
//				.credentialsProvider(DefaultCredentialsProvider.create()).build();
		return S3Client.builder().region(Region.of(applicationProperties.getRegion())).credentialsProvider(
				StaticCredentialsProvider.create(AwsBasicCredentials.create(applicationProperties.getAccessKeyId(),
						applicationProperties.getSecretAccessKey()))).build();
	}

	@Bean("customS3Async")
	public S3AsyncClient s3AsyncClient() {
		return S3AsyncClient.builder().region(Region.of(applicationProperties.getRegion())).credentialsProvider(
				StaticCredentialsProvider.create(AwsBasicCredentials.create(applicationProperties.getAccessKeyId(),
						applicationProperties.getSecretAccessKey()))).build();
	}
}
