package com.ss.imagesDownloader.service;

import com.ss.imagesDownloader.ConfigLoader;
import com.ss.imagesDownloader.dto.DownloadFormDto;
import com.ss.imagesDownloader.dto.DownloadResponseDto;
import com.ss.imagesDownloader.exceptions.ExceptionForUser;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {
  private final String basePath;
  private String message;
  private final AtomicInteger imgFailedToDownloadAmount = new AtomicInteger(0);
  private final AtomicInteger downloadedImagesAmount = new AtomicInteger(0);
  private final ExecutorService executorService;

  public DownloadService(ConfigLoader configLoader) {
    basePath = configLoader.getPath();
    int threadsNumber = configLoader.getThreadsNumber();
    this.executorService = Executors.newFixedThreadPool(threadsNumber);
  }

  public DownloadResponseDto downloadImagesFromUrl(DownloadFormDto form) {
    String htmlUrl = form.getUrl();
    if (htmlUrl.isEmpty() || form.getFolderName().isEmpty()) {
      if (htmlUrl.isEmpty()) {
        form.setIncorrectUrlMessage("Введите корректный URL");
      }
      if (form.getFolderName().isEmpty()) {
        form.setEmptyNameMessage("Имя папки не может быть пустым");
      }
      message = "";
      return new DownloadResponseDto(0, 0, message);
    }
    var html = getHtml(htmlUrl);
    String truncatedUrl = htmlUrl.contains("?") ? htmlUrl.substring(htmlUrl.indexOf("?")) : htmlUrl;
    var imgUrls = parseImgUrls(truncatedUrl, html);
    var folderName = new File(form.getFolderName());
    downloadImagesIntoFolder(imgUrls, folderName.getPath());
    return new DownloadResponseDto(downloadedImagesAmount.get(), imgFailedToDownloadAmount.get(),
        message);
  }

  private String getHtml(String urlString) {
    BufferedReader reader;
    HttpURLConnection con;
    try {
      URL url = new URL(urlString);
      con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      reader = new BufferedReader(
          new InputStreamReader(con.getInputStream()));
    } catch (MalformedURLException | ProtocolException exception) {
      throw new ExceptionForUser("URL введен некорректно: " + urlString);
    } catch (IOException e) {
      throw new ExceptionForUser("Невозможно подключиться к сайту: " + urlString);
    }
    StringBuilder htmlCode = new StringBuilder();
    String inputLine;
    try {
      while ((inputLine = reader.readLine()) != null) {
        htmlCode.append(inputLine);
      }
      reader.close();
    } catch (IOException e) {
      throw new ExceptionForUser("Ошибка чтения HTML по URL: " + urlString);
    }
    try {
      con.disconnect();
    } catch (Exception ignored) {
    }
    return htmlCode.toString();
  }

  private static List<String> parseImgUrls(String url, String htmlCode) {
    Document doc = Jsoup.parse(htmlCode);
    ArrayList<Element> images = doc.select("img");
    return images.stream().map(el -> el.attr("src"))
        .map(el -> el.startsWith("//") ? "https:" + el : el)
        .map(el -> el.startsWith("/") ? url + el : el).toList();
  }

  private void downloadImagesIntoFolder(List<String> urls, String folderName) {
    downloadedImagesAmount.set(0);
    imgFailedToDownloadAmount.set(0);

    String folderPath = basePath + "/" + folderName + "/";
    File folder = new File(folderPath);
    if (!folder.exists()) {
      folder.mkdirs();
    }

    List<Future<?>> imgDownloadFutures = new ArrayList<>();
    for (String url : urls) {
      imgDownloadFutures.add(executorService.submit(() -> {
        URL imgUrl = null;
        try {
          imgUrl = new URL(url);
        } catch (MalformedURLException exception) {
          imgFailedToDownloadAmount.incrementAndGet();
        }
        String filename = getFilenameFromUrl(url);
        filename = getFilenameWithNumber(filename, folderPath);

        InputStream in = null;
        OutputStream out = null;
        if (imgUrl != null) {
          try {
            in = new BufferedInputStream(imgUrl.openStream());
            out = new BufferedOutputStream(
                new FileOutputStream(folderPath + "/" + filename));
            for (int j; (j = in.read()) != -1; ) {
              out.write(j);
            }
            downloadedImagesAmount.incrementAndGet();
          } catch (Exception e) {
            imgFailedToDownloadAmount.incrementAndGet();
          }
        }
        if (in != null && out != null) {
          try {
            in.close();
            out.close();
          } catch (IOException ignored) {
          }
        }
      }));
    }
    for (Future<?> future : imgDownloadFutures) {
      try {
        future.get();
      } catch (InterruptedException ignored) {
      } catch (ExecutionException exception) {
        imgFailedToDownloadAmount.incrementAndGet();
      }
    }

  }

  private static String getFilenameWithNumber(String filename, String folderPath) {
    int lastDotIndex = filename.lastIndexOf(".");
    String filenameWithoutExtension =
        lastDotIndex != -1 ? filename.substring(0, lastDotIndex) : filename;
    String extension = lastDotIndex != -1 ? filename.substring(lastDotIndex) : "";
    AtomicLong filesWithSameNameAmount =
        new AtomicLong(Stream.of(Objects.requireNonNull(new File(folderPath).listFiles()))
            .map(File::getName)
            .filter(name -> name.startsWith(filenameWithoutExtension))
            .count());
    if (filesWithSameNameAmount.get() > 0) {
      filename = filenameWithoutExtension + "(" + filesWithSameNameAmount + ")"
          + extension;
    }
    return filename;
  }

  private static String getFilenameFromUrl(String url) {
    String filename = url.endsWith("/") ? url.substring(0, url.length() - 2) : url;
    int lastSlash = filename.lastIndexOf("/");
    if (lastSlash != -1) {
      filename = filename.substring(lastSlash + 1)
          .replaceAll("[^a-zA-Z0-9\\\\.\\-]", "_");
    } else {
      filename = filename.replaceAll("[^a-zA-Z0-9\\\\.\\-]", "_");
    }
    return filename;
  }
}
