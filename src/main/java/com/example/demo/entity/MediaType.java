package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonInclude;

public class MediaType {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Schema schema;

    // Constructor, getters, setters
    public MediaType() {}

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }
}
