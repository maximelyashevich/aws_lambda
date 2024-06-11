package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@LambdaHandler(lambdaName = "uuid_generator", roleName = "uuid_generator-role")
@RuleEventSource(targetRule = "uuid_trigger")
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "region", value = "${region}"),
        @EnvironmentVariable(key = "target_bucket", value = "${target_bucket}")
})
public class UuidGenerator implements RequestHandler<Object, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String handleRequest(Object request, Context context) {

        var uuids = IntStream.range(0, 10)
                .mapToObj(i -> UUID.randomUUID().toString())
                .collect(Collectors.toList());

        var data = new HashMap<String, Object>();
        data.put("ids", uuids);

        try (var s3Client = S3Client.builder().build()) {
            var fileName = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
            var content = objectMapper.writeValueAsString(data);

            var putRequest = PutObjectRequest.builder()
                    .bucket(System.getenv("target_bucket"))
                    .key(fileName)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(content.getBytes(StandardCharsets.UTF_8)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }

        return "Completed";
    }
}
