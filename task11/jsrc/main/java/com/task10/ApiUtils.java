package com.task10;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import java.util.Optional;


public final class ApiUtils {

    private ApiUtils() {
        throw new IllegalStateException("Unable to create an instance of utility class");
    }

    public static String extractStrategyKey(final APIGatewayProxyRequestEvent event) {

        var method = event.getRequestContext().getHttpMethod();
        var path = event.getPath().split("/")[1];
        var pathParameters = Optional.ofNullable(event.getPathParameters())
                .filter(data -> !data.isEmpty())
                .map(data -> "/{tableId}")
                .orElse("");

        return path + pathParameters + "_" + method;
    }
}
