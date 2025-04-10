package com.ss.ImagesDownloader.dto;

public class DtoDownloadForm {
  private String url;
  private String path;

  public DtoDownloadForm() {
  }

  public DtoDownloadForm(String url, String path) {
    this.url = url;
    this.path = path;
  }

  public DtoDownloadForm setUrl(String url) {
    this.url = url;
    return this;
  }

  public DtoDownloadForm setPath(String path) {
    this.path = path;
    return this;
  }

  public String getUrl() {
    return url;
  }

  public String getPath() {
    return path;
  }
}
