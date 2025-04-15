package com.ss.imagesDownloader.service;

import com.ss.imagesDownloader.dto.CounterDto;

public class ImageThread extends Thread{
  private CounterDto counterDto;

  public ImageThread(CounterDto counterDto) {
    this.counterDto = counterDto;
  }
}
