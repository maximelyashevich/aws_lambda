package com.task11.strategy;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminRespondToAuthChallengeRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminRespondToAuthChallengeResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType;

import java.util.HashMap;
import java.util.Map;


public class SignupHandler extends AbstractAuthHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public SignupHandler(CognitoIdentityProviderClient cognitoClient, String userPoolName) {
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

            var email = input.get("email");
            var password = input.get("password");

            var adminCreateUserRequest = AdminCreateUserRequest.builder()
                    .userPoolId(userPoolId)
                    .messageAction("SUPPRESS")
                    .username(email)
                    .temporaryPassword(password)
                    .userAttributes(
                            AttributeType.builder().name("name").value(input.get("firstName")).build(),
                            AttributeType.builder().name("family_name").value(input.get("lastName")).build(),
                            AttributeType.builder().name("email").value(email).build(),
                            AttributeType.builder().name("email_verified").value("true").build()
                    )
                    .forceAliasCreation(Boolean.FALSE)
                    .build();

            cognitoClient.adminCreateUser(adminCreateUserRequest);
            confirmSignUp(email, password);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatus.SC_OK)
                    .withBody("User registered successfully");
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatus.SC_BAD_REQUEST)
                    .withBody(String.format("{\"error\": \"%s\"}", e.getMessage()));
        }
    }

    @SuppressWarnings("all")
    private AdminRespondToAuthChallengeResponse confirmSignUp(final String username, final String password) {
        var authResponse = signIn(username, password);

        if (!ChallengeNameType.NEW_PASSWORD_REQUIRED.name().equals(authResponse.challengeNameAsString())) {
            throw new IllegalStateException("unexpected challenge: " + authResponse.challengeNameAsString());
        }

        return cognitoClient.adminRespondToAuthChallenge(
                AdminRespondToAuthChallengeRequest.builder()
                        .challengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                        .challengeResponses(
                                Map.of(
                                        "USERNAME", username,
                                        "PASSWORD", password,
                                        "NEW_PASSWORD", password
                                )
                        )
                        .userPoolId(userPoolId)
                        .clientId(clientId)
                        .session(authResponse.session())
                        .build()
        );
    }
}
