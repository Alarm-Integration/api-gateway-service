package com.gabia.apigateway.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class RequestAlarmCommon {
    private Long groupId;
    private String title;
    private String content;

    private List<Integer> bookmarks;

    private List<Raw> raws;

    @Builder

    public RequestAlarmCommon(Long groupId, String title, String content, List<Integer> bookmarks, List<Raw> raws) {
        this.groupId = groupId;
        this.title = title;
        this.content = content;
        this.bookmarks = bookmarks;
        this.raws = raws;
    }

    public static RequestAlarmCommon createRequestAlarm(Long groupId, String title, String content, List<Integer> bookmarks, List<Raw> raws){
        return RequestAlarmCommon.builder()
                .groupId(groupId)
                .title(title)
                .content(content)
                .bookmarks(bookmarks)
                .raws(raws)
                .build();
    }
}
