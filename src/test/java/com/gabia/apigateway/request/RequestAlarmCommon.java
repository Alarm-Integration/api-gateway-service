package com.gabia.apigateway.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RequestAlarmCommon {
    private Long groupId;
    private String title;
    private String content;

    private List<Integer> bookmarks;

    private List<Raw> raws;
}
