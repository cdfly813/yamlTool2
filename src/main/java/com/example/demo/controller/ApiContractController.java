package com.example.demo.controller;

import com.example.demo.service.ApiContractService;
import com.example.demo.entity.ApiPath;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@RestController
public class ApiContractController {

    @Autowired
    private ApiContractService apiContractService;

    @GetMapping("/getAPIContract")
    public List<ApiPath> getAPIContract() {
        return apiContractService.getApiContracts();
    }
}
