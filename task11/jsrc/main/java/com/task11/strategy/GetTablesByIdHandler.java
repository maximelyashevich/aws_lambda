package com.task11.strategy;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;


public class GetTablesByIdHandler extends AbstractDBHandler {

    public GetTablesByIdHandler(AmazonDynamoDB dynamoDB) {
        super(dynamoDB);
    }

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent event) {
        try {
            var tableId = event.getPathParameters().get("tableId");
            var item = dynamoDB.getItem(
                            System.getenv("tables_table"),
                            Map.of("id", new AttributeValue().withN(tableId))
                    )
                    .getItem();

            var responseBody = new HashMap<String, Object>();

            responseBody.put("id", Integer.parseInt(item.get("id").getN()));
            responseBody.put("number", Integer.parseInt(item.get("number").getN()));
            responseBody.put("places", Integer.parseInt(item.get("places").getN()));
            responseBody.put("isVip", Boolean.parseBoolean(item.get("isVip").getBOOL().toString()));

            if (item.containsKey("minOrder")) {
                responseBody.put("minOrder", Integer.parseInt(item.get("minOrder").getN()));
            }

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatus.SC_OK)
                    .withBody(objectMapper.writeValueAsString(responseBody));

        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatus.SC_BAD_REQUEST)
                    .withBody(e.getMessage());
        }
    }
}
