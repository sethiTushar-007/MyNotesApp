package com.example.mynotesapp;

public class Notes {

    private String title;
    private String date;
    private String content;

    public Notes() {}

    public Notes(String title,String date, String content) {
        this.title = title;
        this.date = date;
        this.content = content;
    }

    public String getTitle(){
        return title;
    }
    public String getDate(){
        return date;
    }
    public String getContent(){
        return content;
    }
}
