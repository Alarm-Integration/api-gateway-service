package com.gabia.apigateway.request;

import java.util.List;

public class Raw {
    private String appName;
    private List<String> address;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public List<String> getAddress() {
        return address;
    }

    public void setAddress(List<String> address) {
        this.address = address;
    }
}
