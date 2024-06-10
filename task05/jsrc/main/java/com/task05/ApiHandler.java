package com.task05;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@LambdaHandler(lambdaName = "api_handler", roleName = "api_handler-role")
@DynamoDbTriggerEventSource(targetTable = "${target_table}", batchSize = 1)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "region", value = "${region}"),
        @EnvironmentVariable(key = "target_table", value = "${target_table}")
})
public class ApiHandler implements RequestHandler<CustomRequest, CustomResponse> {

    private final AmazonDynamoDB amazonDynamoDB;

    public ApiHandler() {
        this.amazonDynamoDB = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();
    }

    public CustomResponse handleRequest(final CustomRequest request, final Context context) {

        var event = CustomRequest.convertToEvent(request);
        var attributesMap = prepareAttributesMap(event);

        System.out.println(attributesMap);

        amazonDynamoDB.putItem(System.getenv("target_table"), attributesMap);

        return new CustomResponse(201, event);
    }

    private static Map<String, AttributeValue> prepareAttributesMap(final CustomEvent event) {
        var attributesMap = new HashMap<String, AttributeValue>();

        attributesMap.put("id", new AttributeValue(String.valueOf(event.getId())));
        attributesMap.put("principalId", new AttributeValue().withN(String.valueOf(event.getPrincipalId())));
        attributesMap.put("createdAt", new AttributeValue(event.getCreatedAt()));
        attributesMap.put("body", new AttributeValue().withM(convertToMap(event)));

        return attributesMap;
    }

    private static Map<String, AttributeValue> convertToMap(final CustomEvent event) {
        return event.getBody().entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                o -> new AttributeValue(o.getValue()
                                )
                        )
                );

    }
}
