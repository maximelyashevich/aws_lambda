package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "hello_world",
        roleName = "hello_world-role",
        isPublishVersion = false,
        aliasName = "${lambdas_alias_name}",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
        authType = AuthType.NONE,
        invokeMode = InvokeMode.BUFFERED
)
public class HelloWorld implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final int STATUS_OK = 200;
    private static final int STATUS_BAD_REQUEST = 400;

    /**
     * @param request API Gateway proxy requestEvent
     * @param context context
     * @return response event
     */
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        var path = request.getPath();
        var method = request.getHttpMethod();

        return isNotSupportedRequest(path, method) ? constructBadRequestResponse(path, method) : constructSuccessfulResponse();

    }

    private boolean isNotSupportedRequest(String path, String method) {
        return !"/hello".equals(path) || !"GET".equalsIgnoreCase(method);
    }

    private APIGatewayProxyResponseEvent constructBadRequestResponse(String path, String method) {
        return new APIGatewayProxyResponseEvent()
                .withHeaders(Collections.singletonMap(CONTENT_TYPE, CONTENT_TYPE_JSON))
                .withStatusCode(STATUS_BAD_REQUEST)
                .withBody(String.format("{\"message\": \"Bad request syntax or unsupported method. Request path: %s. " +
                        "HTTP method: %s\"}", path, method));
    }

    private APIGatewayProxyResponseEvent constructSuccessfulResponse() {
        return new APIGatewayProxyResponseEvent()
                .withHeaders(Collections.singletonMap(CONTENT_TYPE, CONTENT_TYPE_JSON))
                .withStatusCode(STATUS_OK)
                .withBody("{\"message\": \"Hello from Lambda\"}");
    }
}
