package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

public class PathResponse {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ResponseHeader> headers;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, MediaType> content;

    public PathResponse(String status, List<ResponseHeader> headers, Map<String, MediaType> content) {
        this.status = status;
        this.headers = headers;
        this.content = content;
    }

    // Getters
    public String getStatus() {
        return status;
    }

    public List<ResponseHeader> getHeaders() {
        return headers;
    }

    public Map<String, MediaType> getContent() {
        return content;
    }
}
