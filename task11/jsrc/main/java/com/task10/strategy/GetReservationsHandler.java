package com.task10.strategy;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.apache.http.HttpStatus;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GetReservationsHandler extends AbstractDBHandler {

    public GetReservationsHandler(AmazonDynamoDB dynamoDB) {
        super(dynamoDB);
    }

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent event) {
        try {
            var scanRequest = new ScanRequest().withTableName(System.getenv("reservations_table"));
            var scanResult = dynamoDB.scan(scanRequest);

            var reservations = new ArrayList<Map<String, Object>>();
            for (var item : scanResult.getItems()) {
                var reservation = new HashMap<String, Object>();
                reservation.put("tableNumber", Integer.parseInt(item.get("tableNumber").getN()));
                reservation.put("clientName", item.get("clientName").getS());
                reservation.put("phoneNumber", item.get("phoneNumber").getS());
                reservation.put("date", item.get("date").getS());
                reservation.put("slotTimeStart", item.get("slotTimeStart").getS());
                reservation.put("slotTimeEnd", item.get("slotTimeEnd").getS());

                reservations.add(reservation);
            }
            var responseBody = new HashMap<String, List<Map<String, Object>>>();
            responseBody.put("reservations", reservations);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatus.SC_OK)
                    .withBody(objectMapper.writeValueAsString(responseBody));
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
