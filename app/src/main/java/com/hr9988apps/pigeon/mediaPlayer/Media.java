package com.hr9988apps.pigeon.mediaPlayer;

public class Media {
    String fileName;
    String fileUrl;
    String fileId;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public Media() {
    }

    public Media(String fileName, String fileUrl, String fileId) {
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}
