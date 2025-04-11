package com.ss.ImagesDownloader.service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
public class DownloadService {

  public List<String> getImgUrlsFormHtml(String urlString) {
    BufferedReader reader = null; //TODO
    HttpURLConnection con = null; //TODO
    try {
      URL url = new URL(urlString);
      con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      int responseCode = con.getResponseCode();
      reader = new BufferedReader(
          new InputStreamReader(con.getInputStream()));
    } catch (IOException e) {
      System.out.println("Отсутствует интернет-соединение или URL введен неверно: " + urlString);
    }
    StringBuilder response = new StringBuilder();
    if (reader != null) {
      String inputLine;
      try {
        while ((inputLine = reader.readLine()) != null) {
          response.append(inputLine);
        }
        reader.close();
      } catch (IOException e) {
        System.err.println("Ошибка чтения HTML по URL: " + urlString);
      }
    }
    Document doc = Jsoup.parse(response.toString());
    ArrayList<Element> images = doc.select("img");
    List<String> urls = images.stream().map(el -> el.attr("src"))
        .map(el -> el.startsWith("//") ? "https:" + el : el)
        .map(el -> el.startsWith("/") ? urlString + el : el).toList();
    if (con != null) {
      con.disconnect();
    }
    return urls;
  }

  public void downloadImages(List<String> urls, String path) {
    for (int i = 0; i < urls.size(); i++) {
      var url = urls.get(i);
      int lastSlash = url.lastIndexOf("/");
      String filepath = path + url.substring(lastSlash);
      //TODO разобраться с файлами без расширения
      try (InputStream in = new BufferedInputStream(new URL(url).openStream());
           OutputStream out = new BufferedOutputStream(new FileOutputStream(filepath))) {
        for (int j; (j = in.read()) != -1; ) {
          out.write(j);
        }
      } catch (IOException e) {
        System.err.println("Ошибка скачивания картинки с URL: " + url
            + " по причине: " + e.getMessage());
      }
    }
  }
}
