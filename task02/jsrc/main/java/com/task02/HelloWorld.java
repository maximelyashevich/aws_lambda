package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.util.Collections;

@LambdaHandler(lambdaName = "hello_world",
        roleName = "hello_world-role",
        isPublishVersion = false,
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
        authType = AuthType.NONE,
        invokeMode = InvokeMode.BUFFERED
)
public class HelloWorld implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final int STATUS_OK = 200;
    private static final int STATUS_BAD_REQUEST = 400;


    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
        var path = request.getRequestContext().getHttp().getPath();
        var method = request.getRequestContext().getHttp().getMethod();

        return isNotSupportedRequest(path, method) ? constructBadRequestResponse(path, method) : constructSuccessfulResponse();
    }

    private boolean isNotSupportedRequest(String path, String method) {
        return !"/hello".equals(path) || !"GET".equalsIgnoreCase(method);
    }

    private APIGatewayV2HTTPResponse constructBadRequestResponse(String path, String method) {
        var response = new APIGatewayV2HTTPResponse();
        response.setHeaders(Collections.singletonMap(CONTENT_TYPE, CONTENT_TYPE_JSON));
        response.setStatusCode(STATUS_BAD_REQUEST);
        response.setBody(String.format("{\"statusCode\": 400,\"message\": \"Bad request syntax or unsupported method. Request path: %s. " +
                        "HTTP method: %s\"}", path, method));
        return response;
    }

    private APIGatewayV2HTTPResponse constructSuccessfulResponse() {
        var response = new APIGatewayV2HTTPResponse();
        response.setHeaders(Collections.singletonMap(CONTENT_TYPE, CONTENT_TYPE_JSON));
        response.setStatusCode(STATUS_OK);
        response.setBody("{\"statusCode\": 200,\"message\": \"Hello from Lambda\"}");
        return response;
    }
}
