# AWS Services Integration Project  

This project demonstrates the integration of AWS S3 and AWS SQS services with Java Spring applications. It provides various REST endpoints for managing S3 storage and SQS messaging, making it easier to handle large-scale operations with Amazon Web Services.

---

## Features  

### **SQS Operations**  
- **Send a message**  
- **Send large messages using S3**:  
- **Receive messages**  
- **Receive large messages via S3**  
- **Delete messages**


### **S3 Operations**
- **Li7st S3 buckets:**
- **Upload a file:**
- **Asynchronous file upload:**
- **Upload data to S3:**
- **Download a file:**
- **Copy objects between buckets:**
- **List and optionally download objects from a bucket**

### **Step 1: Add Dependencies in `pom.xml`**  

Add the following AWS SDK dependencies in your `pom.xml` file:  

```xml
<dependencies>
    <!-- AWS SDK for Java -->
    <dependency>
        <groupId>software.amazon.awssdk</groupId>
        <artifactId>s3</artifactId>
        <version>2.20.100</version>
    </dependency>
    <dependency>
        <groupId>software.amazon.awssdk</groupId>
        <artifactId>sqs</artifactId>
        <version>2.20.100</version>
    </dependency>
    
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```
 
### **Step 2: Add Configuration in `application.properties`**  

Update the `application.properties` file with your AWS credentials and configuration:  

```properties
# AWS Credentials
aws.accessKeyId=your-access-key-id
aws.secretAccessKey=your-secret-access-key
aws.region=your-aws-region

# S3 and SQS Settings
aws.s3.bucketName=your-bucket-name
aws.sqs.queueName=your-queue-name
```
### **Step 3: Implement and Run**  

1. Clone the repository or copy the implementation files into your project.  
2. Replace placeholders in `application.properties` with your actual AWS credentials.  
3. Build the project using Maven:  
   ```bash
   mvn clean install
4. Run the Spring Boot application:
```bash
   mvn spring-boot:run
```
## **Usage**  

- Access S3 endpoints to manage files and objects.  
- Use SQS endpoints for messaging operations.  
- Customize the methods to fit your application's requirements.  

