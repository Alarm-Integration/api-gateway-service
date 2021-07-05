package com.gabia.apigateway.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class Raw {
    private String appName;
    private List<String> addresses;

    @Builder
    public Raw(String appName, List<String> addresses) {
        this.appName = appName;
        this.addresses = addresses;
    }

    public static Raw createRaw(String appName, List<String> addresses) {
        return Raw.builder()
                .appName(appName)
                .addresses(addresses)
                .build();
    }
}
