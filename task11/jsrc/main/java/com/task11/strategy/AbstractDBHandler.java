package com.task11.strategy;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task11.MethodHandler;


public abstract class AbstractDBHandler implements MethodHandler {

    protected AmazonDynamoDB dynamoDB;

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    protected AbstractDBHandler(AmazonDynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }
}
