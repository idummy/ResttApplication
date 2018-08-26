package com.example.roshan.resttapplication;

public class FileObject {
    private String path;
    private String name;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setName() {
        String str = this.path;
        String[] strings = str.split("\\\\");
        this.name = strings[strings.length - 1];
    }

    public String getName() {
        return this.name;
    }

}
