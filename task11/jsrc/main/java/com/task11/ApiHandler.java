package com.task11;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.task11.strategy.GetReservationsHandler;
import com.task11.strategy.GetTablesByIdHandler;
import com.task11.strategy.GetTablesHandler;
import com.task11.strategy.PostReservationsHandler;
import com.task11.strategy.PostTablesHandler;
import com.task11.strategy.SignInHandler;
import com.task11.strategy.SignupHandler;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import java.util.Map;


@LambdaHandler(lambdaName = "api_handler",
        roleName = "api_handler-role"
)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "region", value = "${region}"),
        @EnvironmentVariable(key = "tables_table", value = "${tables_table}"),
        @EnvironmentVariable(key = "reservations_table", value = "${reservations_table}"),
        @EnvironmentVariable(key = "booking_userpool", value = "${booking_userpool}")
})
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Map<String, MethodHandler> handlers;


    public ApiHandler() {
        var userPoolName = System.getenv("booking_userpool");

        @SuppressWarnings("all")
        var cognitoClient = CognitoIdentityProviderClient.builder()
                .region(Region.of(System.getenv("region")))
                .build();

        var dynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(System.getenv("region"))
                .build();

        this.handlers = Map.of(
                "signup_POST", new SignupHandler(cognitoClient, userPoolName),
                "signin_POST", new SignInHandler(cognitoClient, userPoolName),
                "tables_GET", new GetTablesHandler(dynamoDB),
                "tables_POST", new PostTablesHandler(dynamoDB),
                "tables/{tableId}_GET", new GetTablesByIdHandler(dynamoDB),
                "reservations_GET", new GetReservationsHandler(dynamoDB),
                "reservations_POST", new PostReservationsHandler(dynamoDB)
        );
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        var key = ApiUtils.extractStrategyKey(event);

        System.out.println(key);
        var handler = handlers.get(key);

        if (handler == null) {
            return buildErrorResponse(400, "Invalid resource.");
        }

        return handler.handle(event);
    }

    private static APIGatewayProxyResponseEvent buildErrorResponse(int statusCode, String message) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withBody(message);
    }
}
