package com.task10.strategy;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.UUID;


public class PostReservationsHandler extends AbstractDBHandler {

    public PostReservationsHandler(AmazonDynamoDB dynamoDB) {
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
            final int tableNumber = Integer.parseInt(input.get("tableNumber"));

            if (isTableExisting(tableNumber)) {
                String reservationId = String.valueOf(UUID.randomUUID());

                var reservationItem = new HashMap<String, AttributeValue>();
                reservationItem.put("id", new AttributeValue(reservationId));
                reservationItem.put("tableNumber", new AttributeValue().withN(String.valueOf(tableNumber)));
                reservationItem.put("clientName", new AttributeValue().withS(String.valueOf(input.get("clientName"))));
                reservationItem.put("phoneNumber", new AttributeValue().withS(String.valueOf(input.get("phoneNumber"))));
                reservationItem.put("date", new AttributeValue().withS(String.valueOf(input.get("date"))));
                reservationItem.put("slotTimeStart", new AttributeValue().withS(String.valueOf(input.get("slotTimeStart"))));
                reservationItem.put("slotTimeEnd", new AttributeValue().withS(String.valueOf(input.get("slotTimeEnd"))));

                dynamoDB.putItem(System.getenv("reservations_table"), reservationItem);
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(HttpStatus.SC_OK)
                        .withBody(String.format("{\"reservationId\": \"%s\"}", reservationId));
            } else {
                throw new IllegalStateException(String.format("Table number '%s' not found.", tableNumber));
            }

        } catch (IllegalStateException e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatus.SC_BAD_REQUEST)
                    .withBody("reservation can't be created");
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatus.SC_BAD_REQUEST)
                    .withBody(e.getMessage());
        }
    }

    private boolean isTableExisting(final int tableNumber) {
        var tableNumberString = String.valueOf(tableNumber);
        var scanRequest = new ScanRequest().withTableName(System.getenv("tables_table"));
        var scanResult = dynamoDB.scan(scanRequest);

        return scanResult.getItems()
                .stream()
                .anyMatch(item -> item.containsKey("number") && (item.get("number").getN().equals(tableNumberString)));
    }
}
