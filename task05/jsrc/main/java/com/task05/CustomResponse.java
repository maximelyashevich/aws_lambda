package com.task05;

import java.util.Objects;

public class CustomResponse {
    private int statusCode;
    private CustomEvent event;

    public CustomResponse() {
    }

    public CustomResponse(int statusCode, CustomEvent event) {
        this.statusCode = statusCode;
        this.event = event;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public CustomEvent getEvent() {
        return event;
    }

    public void setEvent(CustomEvent event) {
        this.event = event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomResponse that = (CustomResponse) o;
        return statusCode == that.statusCode && Objects.equals(event, that.event);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusCode, event);
    }

    @Override
    public String toString() {
        return "CustomResponse{" +
                "statusCode=" + statusCode +
                ", event=" + event +
                '}';
    }
}
