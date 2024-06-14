package com.task10;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;


@FunctionalInterface
public interface MethodHandler {
    APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent event);
}
