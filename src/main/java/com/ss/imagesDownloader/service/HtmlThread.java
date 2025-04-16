package com.ss.imagesDownloader.service;

import com.ss.imagesDownloader.dto.CounterDto;
import com.ss.imagesDownloader.exceptions.ExceptionForUser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HtmlThread extends Thread {
  public static final String STATE_NEW = "New";
  public static final String STATE_RUNNING = "Running";
  public static final String STATE_FINISHED = "Finished";
  public static final String STATE_ERROR = "Error";
  private static long ID = 0;
  private String html;
  private String url;
  private String stateVl;
  private String errorMessage;
  private String folderPath;
  private long idVl = ID++;
  private ArrayList<String> imgUrls = new ArrayList<>();
  private ExecutionService__I executionService;
  private CounterDto counterDto = new CounterDto();
  private String timestamp;


  public HtmlThread(String url, String folderPath, ExecutionService__I executionService) {
    this.url = url;
    this.folderPath = folderPath;
    this.stateVl = STATE_NEW;
    this.executionService = executionService;
    this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        .format(Calendar.getInstance().getTime());
  }

  public String getStateVl() {
    return stateVl;
  }

  public String getErrorMessage() {
    return errorMessage;
  }


  public long getIdVl() {
    return idVl;
  }

  public ArrayList<String> getImgUrls() {
    return imgUrls;
  }

  public boolean isFinished() {
    return STATE_FINISHED.equals(stateVl);
  }

  public boolean isError() {
    return STATE_ERROR.equals(stateVl);
  }

  public CounterDto getCounterDto() {
    return counterDto;
  }

  public String getTimestamp() {
    return timestamp;
  }

  @Override
  public void run() {
    stateVl = STATE_RUNNING;
    try {
      html = getHtml(url);
    } catch (ExceptionForUser e) {
      stateVl = STATE_ERROR;
      errorMessage = e.getMessage();
      return;
    }
    imgUrls.addAll(parseImgUrls(url, html));
    counterDto.setTotal(imgUrls.size());
    for (String imgUrl : imgUrls) {
      ImageThread imgThread = new ImageThread(counterDto, folderPath, imgUrl, executionService);
      executionService.submit(imgThread);
    }
    stateVl = STATE_FINISHED;
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
    } catch (MalformedURLException | ProtocolException e) {
      throw new ExceptionForUser("URL введен некорректно: " + urlString); //
    } catch (IOException e) {
      throw new ExceptionForUser("Невозможно подключиться к сайту: " + urlString); //
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

}
