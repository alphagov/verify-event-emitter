package uk.gov.ida.eventemitter;

import com.amazonaws.services.sqs.AmazonSQS;
import com.google.inject.Injector;

public class EventEmitterBaseConfiguration {
    protected static final String ACCESS_KEY_ID = "accessKeyId";
    protected static final String ACCESS_SECRET_KEY = "accessSecretKey";
    protected static final String KEY = "aesEncryptionKey";
    protected static final String QUEUE_ACCOUNT_ID = "queueAccountId";
    protected static final String SOURCE_QUEUE_NAME = "sourceQueueName";
    protected static Injector injector;
    protected static String queueUrl;
    protected static AmazonSQS sqs;
}
