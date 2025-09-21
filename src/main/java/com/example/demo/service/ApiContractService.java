package com.example.demo.service;

import com.example.demo.entity.ApiPath;
import com.example.demo.utils.OpenApiUtil;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.List;

@Service
public class ApiContractService {

    @Autowired
    private ResourceLoader resourceLoader;

    public List<ApiPath> getApiContracts() {
        try {
            String yamlContent = new String(Files.readAllBytes(Paths.get(resourceLoader.getResource("classpath:openapi.yaml").getURI())));
            return OpenApiUtil.parseOpenApiToContractsFromContent(yamlContent);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read openapi.yaml", e);
        }
    }
}
