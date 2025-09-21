package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiParameter {
    private String name;
    private String in;
    private Boolean required;
    private String description;
    private Schema schema;

    public ApiParameter(String name, String in, Boolean required, String description, Schema schema) {
        this.name = name;
        this.in = in;
        this.required = required;
        this.description = description;
        this.schema = schema;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getIn() {
        return in;
    }

    public Boolean getRequired() {
        return required;
    }

    public String getDescription() {
        return description;
    }

    public Schema getSchema() {
        return schema;
    }
}
