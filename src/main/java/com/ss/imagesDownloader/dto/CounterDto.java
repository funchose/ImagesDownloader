package com.ss.imagesDownloader.dto;

public class CounterDto {
  private int success = 0;
  private int failed = 0;
  private int total = 0;

  public int getSuccess() {
    return success;
  }

  public CounterDto addSuccess() {
    this.success++;
    return this;
  }

  public int getFailed() {
    return failed;
  }

  public CounterDto addFailed() {
    this.failed++;
    return this;
  }

  public int getTotal() {
    return total;
  }

  public CounterDto setTotal(int total) {
    this.total = total;
    return this;
  }
}
