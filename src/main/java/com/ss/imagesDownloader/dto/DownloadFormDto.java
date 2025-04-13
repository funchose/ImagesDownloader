package com.ss.imagesDownloader.dto;

public class DownloadFormDto {
  private String url;
  private String incorrectUrlMessage;
  private String folderName;
  private String emptyNameMessage;
  private String incorrectFolderName;
  private String response;

  public DownloadFormDto() {
  }

  public DownloadFormDto(String url, String folderName) {
    this.url = url;
    this.folderName = folderName;
  }

  public DownloadFormDto setUrl(String url) {
    this.url = url;
    return this;
  }

  public DownloadFormDto setFolderName(String folderName) {
    this.folderName = folderName;
    return this;
  }

  public String getUrl() {
    return url;
  }

  public String getFolderName() {
    return folderName;
  }

  public String getIncorrectUrlMessage() {
    return incorrectUrlMessage;
  }

  public DownloadFormDto setIncorrectUrlMessage(String incorrectUrlMessage) {
    this.incorrectUrlMessage = incorrectUrlMessage;
    return this;
  }

  public String getEmptyNameMessage() {
    return emptyNameMessage;
  }

  public DownloadFormDto setEmptyNameMessage(String emptyNameMessage) {
    this.emptyNameMessage = emptyNameMessage;
    return this;
  }

  public String getResponse() {
    return response;
  }

  public DownloadFormDto setResponse(String response) {
    this.response = response;
    return this;
  }

  public String getIncorrectFolderName() {
    return incorrectFolderName;
  }

  public DownloadFormDto setIncorrectFolderName(String incorrectFolderName) {
    this.incorrectFolderName = incorrectFolderName;
    return this;
  }
}
