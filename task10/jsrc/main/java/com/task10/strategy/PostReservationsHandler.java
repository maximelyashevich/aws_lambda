package com.task10.strategy;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


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

            if (!isTableExisting(tableNumber)) {
                throw new IllegalStateException(String.format("Table number '%s' not found.", tableNumber));
            }

            var reservations = getExistingReservations(tableNumber);
            for (var existingReservation : reservations) {
                if (hasOverlap(input, existingReservation)) {
                    throw new IllegalStateException("Conflicting reservation");
                }
            }

            var reservationId = String.valueOf(UUID.randomUUID());

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

    private List<Map<String, AttributeValue>> getExistingReservations(final int tableNumber) {
        var tableNumberString = String.valueOf(tableNumber);
        var scanRequest = new ScanRequest().withTableName(System.getenv("reservations_table"));
        var scanResult = dynamoDB.scan(scanRequest);

        return scanResult.getItems()
                .stream()
                .filter(item -> item.containsKey("tableNumber") && (item.get("tableNumber").getN().equals(tableNumberString)))
                .collect(Collectors.toList());
    }

    private boolean hasOverlap(Map<String, String> newReservation, Map<String, AttributeValue> existingReservation) {
        return newReservation.get("tableNumber").equals(existingReservation.get("tableNumber").getN())
                && newReservation.get("date").equals(existingReservation.get("date").getS())
                && (newReservation.get("slotTimeStart").compareTo(existingReservation.get("slotTimeStart").getS()) <= 0
                && newReservation.get("slotTimeEnd").compareTo(existingReservation.get("slotTimeEnd").getS()) >= 0);
    }
}
