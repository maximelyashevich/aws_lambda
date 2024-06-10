package com.task05;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CustomRequest {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private int principalId;
    private Map<String, String> content;

    public CustomRequest() {
    }

    public CustomRequest(int principalId, Map<String, String> content) {
        this.principalId = principalId;
        this.content = content;
    }

    public int getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(int principalId) {
        this.principalId = principalId;
    }

    public Map<String, String> getContent() {
        return content;
    }

    public void setContent(Map<String, String> content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomRequest that = (CustomRequest) o;
        return principalId == that.principalId && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(principalId, content);
    }

    public static CustomEvent convertToEvent(CustomRequest request) {
        return new CustomEvent(
                UUID.randomUUID(),
                request.getPrincipalId(),
                ZonedDateTime.now().format(formatter),
                request.getContent()
        );
    }

    @Override
    public String toString() {
        return "CustomRequest{" +
                "principalId=" + principalId +
                ", content=" + content +
                '}';
    }
}
