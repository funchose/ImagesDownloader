package com.ss.imagesDownloader.service;

import com.ss.imagesDownloader.dto.CounterDto;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageThread extends Thread {
  private final String folderPath;
  private String imgStringUrl;
  private String imgThreadState;
  public static final String STATE_NEW = "New";
  public static final String STATE_RUNNING = "Running";
  public static final String STATE_FINISHED = "Finished";
  public static final String STATE_ERROR = "Error";
  private static long ID = 0;
  private CounterDto counterDto;
  private ExecutionService__I executionService;

  public ImageThread(CounterDto counterDto, String folderPath,
                     String imgStringUrl, ExecutionService__I executionService) {
    this.imgThreadState = STATE_NEW;
    this.counterDto = counterDto;
    this.folderPath = folderPath;
    this.imgStringUrl = imgStringUrl;
    this.executionService = executionService;
  }

  @Override
  public void run() {
    imgThreadState = STATE_RUNNING;
    URL imageUrl = null;
    try {
      imageUrl = new URL(this.imgStringUrl);
    } catch (MalformedURLException exception) {
      counterDto.addFailed();
      imgThreadState = STATE_ERROR;
    }
    downloadImagesIntoFolder(imageUrl);
    imgThreadState = STATE_FINISHED;
  }

  private void downloadImagesIntoFolder(URL imageUrl) {
    String filename = getFilenameFromUrl(imgStringUrl);

    InputStream in = null;
    OutputStream out = null;
    if (imageUrl != null) {
      try {
        File imgFile = getFileToWriteImg(filename, folderPath);
        in = new BufferedInputStream(imageUrl.openStream());
        out = new BufferedOutputStream(
            new FileOutputStream(imgFile));
        for (int j; (j = in.read()) != -1; ) {
          out.write(j);
        }
        counterDto.addSuccess();
      } catch (Exception e) {
        counterDto.addFailed();
        imgThreadState = STATE_ERROR;
      }
    }
    if (in != null && out != null) {
      try {
        in.close();
        out.close();
      } catch (IOException ignored) {
      }
    }
  }


  private static String getFilenameFromUrl(String url) {
    String filename = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    int lastSlash = filename.lastIndexOf("/");
    if (lastSlash != -1) {
      filename = filename.substring(lastSlash + 1)
          .replaceAll("[^a-zA-Z0-9\\\\.\\-]", "_");
    } else {
      filename = filename.replaceAll("[^a-zA-Z0-9\\\\.\\-]", "_");
    }
    return filename;
  }

  private static File getFileToWriteImg(String filename, String folderPath) throws IOException {
    int lastDotIndex = filename.lastIndexOf(".");
    String filenameWithoutExtension =
        lastDotIndex != -1 ? filename.substring(0, lastDotIndex) : filename;
    String extension = lastDotIndex != -1 ? filename.substring(lastDotIndex) : "";

    File imgFile = new File(folderPath + "/" + filename);
    int counter = 1;
    while (!imgFile.createNewFile()) {
      filename = filenameWithoutExtension + "(" + counter + ")"
          + extension;
      imgFile = new File(folderPath + "/" + filename);
      counter++;
    }
    return imgFile;
  }
}
