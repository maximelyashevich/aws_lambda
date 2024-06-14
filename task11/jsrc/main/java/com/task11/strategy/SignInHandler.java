package com.task11.strategy;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.HttpStatus;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import java.util.HashMap;


public class SignInHandler extends AbstractAuthHandler {

    public SignInHandler(CognitoIdentityProviderClient cognitoClient, String userPoolName) {
        super(cognitoClient, userPoolName);
    }

    @Override
    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent event) {
        try {
            var input = objectMapper.readValue(
                    event.getBody(),
                    new TypeReference<HashMap<String, String>>() {
                    }
            );

            var authResponse = signIn(input.get("email"), input.get("password"));

            final String token = authResponse.authenticationResult().idToken();
            System.out.println("TOKEN " + token);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatus.SC_OK)
                    .withBody(String.format("{\"accessToken\":\"%s\"}", token));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatus.SC_BAD_REQUEST)
                    .withBody(String.format("{\"error\": \"%s\"}", e.getMessage()));
        }
    }
}
