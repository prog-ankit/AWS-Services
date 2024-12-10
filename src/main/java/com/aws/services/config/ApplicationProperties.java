package com.aws.services.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationProperties {

	@Value("${aws.access.key}")
	private String accessKeyId;

	@Value("${aws.secret.key}")
	private String secretAccessKey;

	@Value("${aws.region}")
	private String region;

	@Value("${aws.s3.bucket.name}")
	private String s3Bucket;

	public String getAccessKeyId() {
		return accessKeyId;
	}

	public String getSecretAccessKey() {
		return secretAccessKey;
	}

	public String getRegion() {
		return region;
	}

	public String getS3Bucket() {
		return s3Bucket;
	}
}
