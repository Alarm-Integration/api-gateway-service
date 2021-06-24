package com.gabia.apigateway.request;

import java.util.ArrayList;

public class RequestAlarmCommon {
    private Long groupId;
    private String title;
    private String content;

    private ArrayList<Integer> bookmarks;

    private ArrayList<Raw> raws;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ArrayList<Integer> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(ArrayList<Integer> bookmarks) {
        this.bookmarks = bookmarks;
    }

    public ArrayList<Raw> getRaws() {
        return raws;
    }

    public void setRaws(ArrayList<Raw> raws) {
        this.raws = raws;
    }
}
