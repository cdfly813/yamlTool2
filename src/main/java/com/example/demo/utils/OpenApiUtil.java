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
        if (swaggerSchema == null) return null;

        if (swaggerSchema.get$ref() != null) {
            String ref = swaggerSchema.get$ref();
            String schemaName = ref.substring(ref.lastIndexOf('/') + 1);
            io.swagger.v3.oas.models.media.Schema<?> refSchema = openAPI.getComponents().getSchemas().get(schemaName);
            if (refSchema != null) {
                return mapToCustomSchema(openAPI, refSchema);
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

        if (swaggerSchema.getProperties() != null) {
            schema.setProperties(swaggerSchema.getProperties().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> mapToCustomSchema(openAPI, e.getValue()))));
        }

        if (swaggerSchema instanceof ArraySchema) {
            schema.setItems(mapToCustomSchema(openAPI, ((ArraySchema) swaggerSchema).getItems()));
        }

        if (swaggerSchema.getAllOf() != null) {
            schema.setAllOf(swaggerSchema.getAllOf().stream().map(s -> mapToCustomSchema(openAPI, s)).collect(Collectors.toList()));
        }

        if (swaggerSchema.getOneOf() != null) {
            schema.setOneOf(swaggerSchema.getOneOf().stream().map(s -> mapToCustomSchema(openAPI, s)).collect(Collectors.toList()));
        }

        // Add more mappings as needed

        return schema;
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
                        ApiResponse resp = rEntry.getValue();

                        PathResponse res = parseResponse(code, resp, openAPI);
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

    private static PathRequest parseRequest(PathItem.HttpMethod method, Operation op, OpenAPI openAPI) {
        List<ApiParameter> parameters = parseRequestParameters(op.getParameters(), openAPI);

        RequestBody body = parseRequestBody(op.getRequestBody(), openAPI);

        return new PathRequest(method.name().toLowerCase(), parameters, body);
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
