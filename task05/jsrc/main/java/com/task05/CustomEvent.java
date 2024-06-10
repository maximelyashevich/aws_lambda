package com.task05;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public class CustomEvent {
    private UUID id;
    private int principalId;
    private String createdAt;
    private Map<String, String> body;

    public CustomEvent() {
    }

    public CustomEvent(UUID id, int principalId, String createdAt, Map<String, String> body) {
        this.id = id;
        this.principalId = principalId;
        this.createdAt = createdAt;
        this.body = body;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(int principalId) {
        this.principalId = principalId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, String> getBody() {
        return body;
    }

    public void setBody(Map<String, String> body) {
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomEvent that = (CustomEvent) o;
        return Objects.equals(id, that.id) && Objects.equals(principalId, that.principalId) && Objects.equals(createdAt, that.createdAt) && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, principalId, createdAt, body);
    }

    @Override
    public String toString() {
        return "CustomEvent{" +
                "id=" + id +
                ", principalId='" + principalId + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
