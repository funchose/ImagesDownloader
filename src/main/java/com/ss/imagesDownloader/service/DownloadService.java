package com.ss.imagesDownloader.service;

import com.ss.imagesDownloader.ConfigLoader;
import com.ss.imagesDownloader.dto.DownloadFormDto;
import com.ss.imagesDownloader.dto.DownloadResponseDto;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {
  @NonNull
  private final static String BASE_PATH = new ConfigLoader().getPath();
  private String message;
  private int downloadedImagesAmount;
  private int imgFailedToDownloadAmount;

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
    downloadImagesIntoFolder(imgUrls, folderName.getName());
    return new DownloadResponseDto(downloadedImagesAmount, imgFailedToDownloadAmount, message);
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
      throw new RuntimeException("URL введен некорректно: " + urlString);
    } catch (IOException e) {
      throw new RuntimeException("Невозможно подключиться к сайту: " + urlString);
    }
    StringBuilder htmlCode = new StringBuilder();
    String inputLine;
    try {
      while ((inputLine = reader.readLine()) != null) {
        htmlCode.append(inputLine);
      }
      reader.close();
    } catch (IOException e) {
      throw new RuntimeException("Ошибка чтения HTML по URL: " + urlString);
    }
    try {
      con.disconnect();
    } catch (Exception exception) {
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
    imgFailedToDownloadAmount = 0;
    String folderPath = BASE_PATH + "/" + folderName + "/";
    File folder = new File(folderPath);
    if (!folder.exists()) {
      folder.mkdirs();  //TODO ask about folder/folder/folder
    }
    for (String url : urls) {
      URL imgUrl;
      try {
        imgUrl = new URL(url);
      } catch (MalformedURLException exception) {
        imgFailedToDownloadAmount++;
        continue;
      }
      String filename;
      int lastSlash = url.lastIndexOf("/");
      if (lastSlash != -1) {
        filename = url.substring(lastSlash + 1)
            .replaceAll("[^a-zA-Z0-9\\\\.\\-]", "_");
      } else {
        filename = url.replaceAll("[^a-zA-Z0-9\\\\.\\-]", "_");
      }
      int lastDotIndex = filename.lastIndexOf(".");
      String filenameWithoutExtension =
          lastDotIndex != -1 ? filename.substring(0, lastDotIndex) : filename;
      String extension = lastDotIndex != -1 ? filename.substring(lastDotIndex) : "";
      long filesWithSameNameAmount =
          Stream.of(Objects.requireNonNull(new File(folderPath).listFiles()))
              .map(File::getName)
              .filter(name -> name.startsWith(filenameWithoutExtension))
              .count();
      if (filesWithSameNameAmount > 0) {
        filename = filenameWithoutExtension + "(" + filesWithSameNameAmount + ")"
            + extension;
      }
      InputStream in = null;
      OutputStream out = null;
      try {
        in = new BufferedInputStream(imgUrl.openStream());
        out = new BufferedOutputStream(
            new FileOutputStream(folderPath + "/" + filename));
        for (int j; (j = in.read()) != -1; ) {
          out.write(j);
        }
      } catch (IOException e) {
        imgFailedToDownloadAmount++;
      }
      if (in != null && out != null) {
        try {
          in.close();
          out.close();
        } catch (IOException exception) {
        }
      }
    }
    downloadedImagesAmount = urls.size() - imgFailedToDownloadAmount;
  }
}
