package com.task10.strategy;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.HttpStatus;

import java.util.HashMap;


public class PostTablesHandler extends AbstractDBHandler {

    public PostTablesHandler(AmazonDynamoDB dynamoDB) {
        super(dynamoDB);
    }

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent event) {
        try {
            var input = objectMapper.readValue(
                    event.getBody(),
                    new TypeReference<HashMap<String, String>>() {
                    }
            );

            var attributesMap = new HashMap<String, AttributeValue>();

            var id = input.get("id");
            attributesMap.put("id", new AttributeValue().withN(id));
            attributesMap.put("number", new AttributeValue().withN(String.valueOf(input.get("number"))));
            attributesMap.put("places", new AttributeValue().withN(String.valueOf(input.get("places"))));
            attributesMap.put("isVip", new AttributeValue().withBOOL(Boolean.valueOf((input.get("isVip")))));
            if (input.containsKey("minOrder")) {
                String minOrder = String.valueOf(input.get("minOrder"));
                attributesMap.put("minOrder", new AttributeValue().withN(minOrder));
            }

            dynamoDB.putItem(System.getenv("tables_table"), attributesMap);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatus.SC_OK)
                    .withBody(String.format("{\"id\":%s}", id));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatus.SC_BAD_REQUEST)
                    .withBody(String.format("{\"Invalid input\":\"%s\"}", e.getMessage()));
        }
    }
}
