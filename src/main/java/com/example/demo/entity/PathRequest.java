package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PathRequest {
    private String method;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<RequestHeader> headers;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ApiParameter> parameters;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private RequestBody body;

    public PathRequest(String method, List<ApiParameter> parameters, RequestBody body) {
        this.parameters = parameters;
        this.body = body;
    }

    // Getters
    public List<RequestHeader> getHeaders() {
        return headers;
    }

    public List<ApiParameter> getParameters() {
        return parameters;
    }

    public RequestBody getBody() {
        return body;
    }
}
