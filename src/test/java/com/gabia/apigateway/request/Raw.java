package com.gabia.apigateway.request;

import java.util.ArrayList;

public class Raw {
    private String appName;
    private ArrayList<String> address;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public ArrayList<String> getAddress() {
        return address;
    }

    public void setAddress(ArrayList<String> address) {
        this.address = address;
    }
}
