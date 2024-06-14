package com.task11.strategy;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.apache.http.HttpStatus;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GetTablesHandler extends AbstractDBHandler {

    public GetTablesHandler(AmazonDynamoDB dynamoDB) {
        super(dynamoDB);
    }

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent event) {
        try {
            var scanRequest = new ScanRequest().withTableName(System.getenv("tables_table"));
            var scanResult = dynamoDB.scan(scanRequest);
            var tables = new ArrayList<Map<String, Object>>();
            for (Map<String, AttributeValue> item : scanResult.getItems()) {
                var table = new HashMap<String, Object>();
                table.put("id", Integer.parseInt(item.get("id").getN()));
                table.put("number", Integer.parseInt(item.get("number").getN()));
                table.put("places", Integer.parseInt(item.get("places").getN()));
                table.put("isVip", Boolean.parseBoolean(item.get("isVip").getBOOL().toString()));

                if (item.containsKey("minOrder")) {
                    table.put("minOrder", Integer.parseInt(item.get("minOrder").getN()));
                }
                tables.add(table);
            }

            var responseBody = new HashMap<String, List<Map<String, Object>>>();
            responseBody.put("tables", tables);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatus.SC_OK)
                    .withBody(String.format(objectMapper.writeValueAsString(responseBody)));
        } catch (NotAuthorizedException ex) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatus.SC_BAD_REQUEST)
                    .withBody("Not authorized");
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatus.SC_BAD_REQUEST)
                    .withBody(e.getMessage());
        }
    }
}
