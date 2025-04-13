package com.ss.imagesDownloader.dto;

public class DownloadResponseDto {
  private int downloadedImagesAmount;
  private int downloadErrorsAmount;
  private String errorMessage;

  public DownloadResponseDto() {
  }

  public DownloadResponseDto(int downloadedImagesAmount, int downloadErrorsAmount, String responseMessage) {
    this.downloadedImagesAmount = downloadedImagesAmount;
    this.downloadErrorsAmount = downloadErrorsAmount;
  }

  public int getDownloadedImagesAmount() {
    return downloadedImagesAmount;
  }

  public int getDownloadErrorsAmount() {
    return downloadErrorsAmount;
  }


  public DownloadResponseDto setDownloadedImagesAmount(int downloadedImagesAmount) {
    this.downloadedImagesAmount = downloadedImagesAmount;
    return this;
  }

  public DownloadResponseDto setDownloadErrorsAmount(int downloadErrorsAmount) {
    this.downloadErrorsAmount = downloadErrorsAmount;
    return this;
  }

  public DownloadResponseDto setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}
