package com.example.demo.entity;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;

public class Schema {
    private String type;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String format;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String pattern;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer minLength;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer maxLength;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object example;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Schema> properties;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Schema items;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Schema> allOf;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Schema> oneOf;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;
    // Add more fields as needed for full representation

    // Constructor, getters, setters
    public Schema() {}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Map<String, Schema> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Schema> properties) {
        this.properties = properties;
    }

    public Schema getItems() {
        return items;
    }

    public void setItems(Schema items) {
        this.items = items;
    }

    public List<Schema> getAllOf() {
        return allOf;
    }

    public void setAllOf(List<Schema> allOf) {
        this.allOf = allOf;
    }

    public List<Schema> getOneOf() {
        return oneOf;
    }

    public void setOneOf(List<Schema> oneOf) {
        this.oneOf = oneOf;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Object getExample() {
        return example;
    }

    public void setExample(Object example) {
        this.example = example;
    }
}
