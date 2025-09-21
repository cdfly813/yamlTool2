package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonInclude;

public class ApiPair {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String method;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PathRequest request;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PathResponse response;

    public ApiPair(String method, PathRequest request, PathResponse response) {
        this.method = method;
        this.request = request;
        this.response = response;
    }

    // Getters
    public PathRequest getRequest() {
        return request;
    }

    public PathResponse getResponse() {
        return response;
    }

    public String getMethod() {
        return method;
    }
}
