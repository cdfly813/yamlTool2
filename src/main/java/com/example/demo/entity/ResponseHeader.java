package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonInclude;

public class ResponseHeader {
    private String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Schema schema;

    public ResponseHeader(String name) {
        this.name = name;
    }

    public ResponseHeader(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public ResponseHeader(String name, String description, Schema schema) {
        this.name = name;
        this.description = description;
        this.schema = schema;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Schema getSchema() {
        return schema;
    }
}
