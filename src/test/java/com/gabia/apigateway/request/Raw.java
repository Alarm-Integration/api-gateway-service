package com.gabia.apigateway.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Raw {
    private String appName;
    private List<String> address;
}
