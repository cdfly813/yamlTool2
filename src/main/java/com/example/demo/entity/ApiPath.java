package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public class ApiPath {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String path;
    private List<PathRequest> requests;
    private List<PathResponse> responses;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ApiPair> pairs;

    public ApiPath(String path, List<PathRequest> requests, List<PathResponse> responses) {
        this.path = path;
        this.requests = requests;
        this.responses = responses;
    }

    public ApiPath(String path, List<ApiPair> pairs) {
        this.path = path;
        this.pairs = pairs;
    }

    // Getters
    public String getPath() {
        return path;
    }

    public List<PathRequest> getRequests() {
        return requests;
    }

    public List<PathResponse> getResponses() {
        return responses;
    }

    public List<ApiPair> getPairs() {
        return pairs;
    }
}
