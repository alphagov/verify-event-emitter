package uk.gov.ida.eventemitter;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sqs.AmazonSQS;
import com.google.inject.Injector;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class EventEmitterBaseConfiguration {
    protected static final String ACCESS_KEY_ID = "accessKeyId";
    protected static final String ACCESS_SECRET_KEY = "accessSecretKey";
    protected static final String KEY = "aesEncryptionKey";
    protected static final String QUEUE_ACCOUNT_ID = "queueAccountId";
    protected static final String SOURCE_QUEUE_NAME = "sourceQueueName";
    protected static final String BUCKET_NAME = "bucket.name";
    protected static final String KEY_NAME = "keyName";
    protected static Injector injector;
    protected static String queueUrl;
    protected static AmazonSQS sqs;
    protected static AmazonS3 s3;
    protected static ByteArrayOutputStream errorContent;
    protected static PrintStream printStream;
}
