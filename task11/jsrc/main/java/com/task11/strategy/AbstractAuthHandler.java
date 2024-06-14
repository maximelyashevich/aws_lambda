package com.task11.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task11.MethodHandler;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolClientsRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolsRequest;

import java.util.Map;


public abstract class AbstractAuthHandler implements MethodHandler {

    protected final CognitoIdentityProviderClient cognitoClient;
    protected final String userPoolId;
    protected final String clientId;

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    protected AbstractAuthHandler(CognitoIdentityProviderClient cognitoClient, String userPoolName) {
        this.cognitoClient = cognitoClient;
        this.userPoolId = getUserPoolId(userPoolName);
        this.clientId = getClientId();
    }

    protected AdminInitiateAuthResponse signIn(final String email, final String password) {
        return cognitoClient.adminInitiateAuth(
                AdminInitiateAuthRequest.builder()
                        .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                        .authParameters(Map.of(
                                "USERNAME", email,
                                "PASSWORD", password
                        ))
                        .userPoolId(userPoolId)
                        .clientId(clientId)
                        .build()
        );
    }

    private String getUserPoolId(final String userPoolName) throws IllegalStateException {
        return cognitoClient.listUserPools(
                        ListUserPoolsRequest.builder().maxResults(10).build()
                )
                .userPools().stream()
                .filter(pool -> pool.name().contains(userPoolName))
                .findAny()
                .orElseThrow(() -> new IllegalStateException(String.format("user pool %s not found", userPoolName)))
                .id();
    }

    private String getClientId() throws IllegalStateException {
        return cognitoClient.listUserPoolClients(
                        ListUserPoolClientsRequest.builder().userPoolId(userPoolId).maxResults(1).build()
                )
                .userPoolClients().stream()
                .filter(client -> client.clientName().contains("booking-app"))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("client 'booking-app' not found"))
                .clientId();
    }
}
