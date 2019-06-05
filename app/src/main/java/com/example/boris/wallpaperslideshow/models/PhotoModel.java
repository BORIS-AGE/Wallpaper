package com.example.boris.wallpaperslideshow.models;

import java.io.File;

public class PhotoModel {
    private File file;

    public PhotoModel(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(String url) {
        this.file = file;
    }
}
