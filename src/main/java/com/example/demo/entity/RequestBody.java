package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

public class RequestBody {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private boolean required;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, MediaType> content;

    // Constructor, getters, setters
    public RequestBody() {}

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, MediaType> getContent() {
        return content;
    }

    public void setContent(Map<String, MediaType> content) {
        this.content = content;
    }
}
