package com.personalproject.homepage.config.web;

public class UploadFilePathResolver {

    private final String uploadPath;

    public UploadFilePathResolver(String activeProfiles) {
        this.uploadPath = System.getProperty("user.dir") +
            (activeProfiles.equals("prod")
                ? "/uploadfile"
                : "/src/main/resources/static/uploadfile");
    }

    public String getUploadPath() {
        return uploadPath;
    }

}
