package com.task09;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.epam.meteo.OpenMeteoAPIClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.TracingMode;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@LambdaHandler(lambdaName = "processor",
        roleName = "processor-role",
        layers = {"sdk-layer"},
        tracingMode = TracingMode.Active
)
@LambdaLayer(
        layerName = "sdk-layer",
        libraries = {"lib/open-meteo-1.0.jar"},
        artifactExtension = ArtifactExtension.ZIP
)
@LambdaUrlConfig(
        authType = AuthType.NONE,
        invokeMode = InvokeMode.BUFFERED
)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "region", value = "${region}"),
        @EnvironmentVariable(key = "target_table", value = "${target_table}")
})
public class Processor implements RequestHandler<Object, Map<String, Object>> {

    private final OpenMeteoAPIClient apiClient = new OpenMeteoAPIClient();
    private final DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());
    private static final ObjectMapper objectMapper = new ObjectMapper();


    public Map<String, Object> handleRequest(Object request, Context context) {
        try {
            var rawJsonForecast = apiClient.getWeatherForecast();
            var forecastMap = processJsonForecast(rawJsonForecast);

            var tableName = System.getenv("target_table");
            var eventsTable = dynamoDB.getTable(tableName);

            var item = new Item()
                    .withPrimaryKey("id", UUID.randomUUID().toString())
                    .with("forecast", forecastMap);

            eventsTable.putItem(item);

            var result = new HashMap<String, Object>();
            result.put("statusCode", 200);
            result.put("body", item.toJSONPretty());
            return result;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private Map<String, Object> processJsonForecast(String forecast) throws JsonProcessingException {
        return objectMapper.readValue(forecast, new TypeReference<HashMap<String, Object>>() {
        });
    }
}
