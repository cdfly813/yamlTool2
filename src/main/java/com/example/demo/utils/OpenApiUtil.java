package com.example.demo.utils;

import com.example.demo.entity.*;
import com.example.demo.entity.MediaType;
import com.example.demo.entity.Schema;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.oas.models.media.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Collections;
import org.springframework.web.bind.annotation.RequestHeader;
import com.example.demo.entity.ApiParameter;

public class OpenApiUtil {

    private static Schema mapToCustomSchema(OpenAPI openAPI, io.swagger.v3.oas.models.media.Schema<?> swaggerSchema) {
        return mapToCustomSchema(openAPI, swaggerSchema, new HashMap<>());
    }

    private static Schema mapToCustomSchema(OpenAPI openAPI, io.swagger.v3.oas.models.media.Schema<?> swaggerSchema, Map<String, Schema> cache) {
        if (swaggerSchema == null) return null;

        if (swaggerSchema.get$ref() != null) {
            String ref = swaggerSchema.get$ref();
            String schemaName = ref.substring(ref.lastIndexOf('/') + 1);

            // Check cache first to avoid recursion
            if (cache.containsKey(schemaName)) {
                return cache.get(schemaName);
            }

            io.swagger.v3.oas.models.media.Schema<?> refSchema = openAPI.getComponents().getSchemas().get(schemaName);
            if (refSchema != null) {
                // Put a placeholder to detect cycles
                Schema placeholder = new Schema();
                cache.put(schemaName, placeholder);

                Schema resolved = mapToCustomSchema(openAPI, refSchema, cache);

                // Update the cache with the resolved schema
                placeholder.setType(resolved.getType());
                placeholder.setFormat(resolved.getFormat());
                placeholder.setDescription(resolved.getDescription());
                placeholder.setPattern(resolved.getPattern());
                placeholder.setMinLength(resolved.getMinLength());
                placeholder.setMaxLength(resolved.getMaxLength());
                placeholder.setExample(resolved.getExample());
                placeholder.setProperties(resolved.getProperties());
                placeholder.setItems(resolved.getItems());
                placeholder.setAllOf(resolved.getAllOf());
                placeholder.setOneOf(resolved.getOneOf());

                return placeholder;
            } else {
                throw new RuntimeException("Unresolved schema reference: " + ref);
            }
        }

        Schema schema = new Schema();
        schema.setType(swaggerSchema.getType());
        schema.setFormat(swaggerSchema.getFormat());
        schema.setDescription(swaggerSchema.getDescription());
        schema.setPattern(swaggerSchema.getPattern());
        schema.setMinLength(swaggerSchema.getMinLength());
        schema.setMaxLength(swaggerSchema.getMaxLength());
        schema.setExample(swaggerSchema.getExample());

        // Handle properties recursively
        if (swaggerSchema.getProperties() != null) {
            schema.setProperties(swaggerSchema.getProperties().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> mapToCustomSchema(openAPI, e.getValue(), cache))));
        }

        // Handle array items recursively
        if (swaggerSchema instanceof ArraySchema) {
            schema.setItems(mapToCustomSchema(openAPI, ((ArraySchema) swaggerSchema).getItems(), cache));
        }

        // Handle allOf by merging properties recursively
        if (swaggerSchema.getAllOf() != null && !swaggerSchema.getAllOf().isEmpty()) {
            Map<String, Schema> mergedProperties = new HashMap<>();
            for (io.swagger.v3.oas.models.media.Schema<?> allOfSchema : swaggerSchema.getAllOf()) {
                Schema resolved = mapToCustomSchema(openAPI, allOfSchema, cache);
                if (resolved.getProperties() != null) {
                    mergedProperties.putAll(resolved.getProperties());
                }
            }
            schema.setProperties(mergedProperties);
            // Note: For simplicity, we're merging properties here. Handle other fields if needed (e.g., required).
        }

        // Handle oneOf (not merging, as it's a union - you may need custom logic based on use case)
        if (swaggerSchema.getOneOf() != null) {
            schema.setOneOf(swaggerSchema.getOneOf().stream().map(s -> mapToCustomSchema(openAPI, s, cache)).collect(Collectors.toList()));
        }

        // Add more mappings as needed (e.g., anyOf, not, etc.)

        return schema;
    }

    private static PathRequest parseRequest(PathItem.HttpMethod method, Operation op, OpenAPI openAPI) {
        List<ApiParameter> parameters = parseRequestParameters(op.getParameters(), openAPI);

        RequestBody body = parseRequestBody(op.getRequestBody(), openAPI);

        return new PathRequest(method.name().toLowerCase(), parameters, body);
    }

    // New method to resolve ApiResponse if it's a reference
    private static ApiResponse resolveApiResponse(OpenAPI openAPI, ApiResponse response) {
        if (response.get$ref() != null) {
            String ref = response.get$ref();
            String responseName = ref.substring(ref.lastIndexOf('/') + 1);
            ApiResponse refResponse = openAPI.getComponents().getResponses().get(responseName);
            if (refResponse != null) {
                // Recurse in case of nested refs (though rare)
                return resolveApiResponse(openAPI, refResponse);
            } else {
                throw new RuntimeException("Unresolved response reference: " + ref);
            }
        }
        return response;
    }

    private static List<ApiPath> parseOpenAPI(OpenAPI openAPI) {
        if (openAPI == null) {
            throw new RuntimeException("OpenAPI is null");
        }

        List<ApiPath> result = new ArrayList<>();

        Map<String, PathItem> paths = openAPI.getPaths();
        for (String path : paths.keySet()) {
            PathItem item = paths.get(path);

            List<ApiPair> pairs = new ArrayList<>();

            Map<PathItem.HttpMethod, Operation> operations = item.readOperationsMap();
            for (Map.Entry<PathItem.HttpMethod, Operation> entry : operations.entrySet()) {
                PathItem.HttpMethod method = entry.getKey();
                Operation op = entry.getValue();

                PathRequest req = parseRequest(method, op, openAPI);

                if (op.getResponses() != null) {
                    for (Map.Entry<String, ApiResponse> rEntry : op.getResponses().entrySet()) {
                        String code = rEntry.getKey();
                        ApiResponse resolvedResp = resolveApiResponse(openAPI, rEntry.getValue());
                        PathResponse res = parseResponse(code, resolvedResp, openAPI);
                        pairs.add(new ApiPair(method.name().toLowerCase(), req, res));
                    }
                } else {
                    PathResponse emptyRes = new PathResponse("", new ArrayList<>(), null);
                    pairs.add(new ApiPair(method.name().toLowerCase(), req, emptyRes));
                }
            }

            result.add(new ApiPath(path, pairs));
        }

        return result;
    }

    private static List<ApiParameter> parseRequestParameters(List<io.swagger.v3.oas.models.parameters.Parameter> swaggerParams, OpenAPI openAPI) {
        List<ApiParameter> params = new ArrayList<>();
        if (swaggerParams != null) {
            for (io.swagger.v3.oas.models.parameters.Parameter param : swaggerParams) {
                params.add(new ApiParameter(param.getName(), param.getIn(), param.getRequired(), param.getDescription(), mapToCustomSchema(openAPI, param.getSchema())));
            }
        }
        return params;
    }

    private static RequestBody parseRequestBody(io.swagger.v3.oas.models.parameters.RequestBody rb, OpenAPI openAPI) {
        if (rb == null) return null;

        RequestBody body = new RequestBody();
        body.setRequired(rb.getRequired() != null ? rb.getRequired() : false);
        body.setDescription(rb.getDescription());

        if (rb.getContent() != null) {
            Map<String, MediaType> content = rb.getContent().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> {
                                MediaType mt = new MediaType();
                                mt.setSchema(mapToCustomSchema(openAPI, e.getValue().getSchema()));
                                return mt;
                            }
                    ));
            body.setContent(content);
        }

        return body;
    }

    private static PathResponse parseResponse(String code, ApiResponse resp, OpenAPI openAPI) {
        List<ResponseHeader> resHeaders = parseResponseHeaders(resp.getHeaders(), openAPI);

        Map<String, MediaType> content = parseContent(resp.getContent(), openAPI);

        return new PathResponse(code, resHeaders, content);
    }

    private static List<ResponseHeader> parseResponseHeaders(Map<String, Header> headers, OpenAPI openAPI) {
        List<ResponseHeader> resHeaders = new ArrayList<>();
        if (headers != null) {
            for (Map.Entry<String, Header> hEntry : headers.entrySet()) {
                Header header = hEntry.getValue();
                resHeaders.add(new ResponseHeader(hEntry.getKey(), header.getDescription(), mapToCustomSchema(openAPI, header.getSchema())));
            }
        }
        return resHeaders;
    }

    private static Map<String, MediaType> parseContent(Content swaggerContent, OpenAPI openAPI) {
        if (swaggerContent == null) return null;

        return swaggerContent.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> {
                            MediaType mt = new MediaType();
                            mt.setSchema(mapToCustomSchema(openAPI, e.getValue().getSchema()));
                            return mt;
                        }
                ));
    }

    public static List<ApiPath> parseOpenApiToContracts(String yamlPath) {
        OpenAPI openAPI = new OpenAPIV3Parser().read(yamlPath);
        return parseOpenAPI(openAPI);
    }

    public static List<ApiPath> parseOpenApiToContractsFromContent(String yamlContent) {
        OpenAPI openAPI = new OpenAPIV3Parser().readContents(yamlContent).getOpenAPI();
        return parseOpenAPI(openAPI);
    }
}
