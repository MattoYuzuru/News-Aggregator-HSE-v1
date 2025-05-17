package com.news.model;

public class RequestModel {
    public String model;
    public String prompt;
    public boolean stream;

    public RequestModel(String model, String prompt, boolean stream) {
        this.model = model;
        this.prompt = prompt;
        this.stream = stream;
    }
}