package com.task06;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.UUID;


@LambdaHandler(lambdaName = "audit_producer", roleName = "audit_producer-role")
@DynamoDbTriggerEventSource(targetTable = "Configuration", batchSize = 1)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "region", value = "${region}"),
        @EnvironmentVariable(key = "target_table", value = "${target_table}")
})
public class AuditProducer implements RequestHandler<DynamodbEvent, String> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private static final String VALUE = "value";
    private static final String KEY = "key";

    private final AmazonDynamoDB amazonDynamoDB;

    public AuditProducer() {
        this.amazonDynamoDB = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();
    }

    public String handleRequest(DynamodbEvent event, Context context) {
        System.out.println("Try to parse event: " + event.toString());

        for (DynamodbStreamRecord streamRecord : event.getRecords()) {
            System.out.println(streamRecord.toString());
            var eventName = streamRecord.getEventName();
            switch (eventName) {
                case "INSERT": {
                    var newImage = streamRecord.getDynamodb().getNewImage();

                    var attributesMap = new HashMap<String, AttributeValue>();
                    attributesMap.put("id", new AttributeValue(UUID.randomUUID().toString()));
                    attributesMap.put("itemKey", new AttributeValue(newImage.get(KEY).getS()));
                    attributesMap.put("modificationTime", new AttributeValue(ZonedDateTime.now().format(formatter)));

                    var newValue = new HashMap<String, AttributeValue>();
                    newValue.put(KEY, new AttributeValue(newImage.get(KEY).getS()));
                    newValue.put(VALUE, new AttributeValue(newImage.get(VALUE).getN()));

                    attributesMap.put("newValue", new AttributeValue().withM(newValue));

                    System.out.println(attributesMap);

                    amazonDynamoDB.putItem(System.getenv("target_table"), attributesMap);
                    break;
                }
                case "MODIFY": {
                    var newImage = streamRecord.getDynamodb().getNewImage();
                    var oldImage = streamRecord.getDynamodb().getOldImage();

                    var attributesMap = new HashMap<String, AttributeValue>();

                    attributesMap.put("id", new AttributeValue(UUID.randomUUID().toString()));
                    attributesMap.put("itemKey", new AttributeValue(newImage.get(KEY).getS()));
                    attributesMap.put("modificationTime", new AttributeValue(ZonedDateTime.now().format(formatter)));
                    attributesMap.put("updatedAttribute", new AttributeValue(VALUE));
                    attributesMap.put("oldValue", new AttributeValue(oldImage.get(VALUE).getS()));
                    attributesMap.put("newValue", new AttributeValue(newImage.get(VALUE).getN()));

                    System.out.println(attributesMap);

                    amazonDynamoDB.putItem(System.getenv("target_table"), attributesMap);
                    break;
                }
                default: {
                    System.out.println("Do nothing");
                }
            }
        }
        return "Completed";
    }
}

