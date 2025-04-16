package com.ss.imagesDownloader.service;

import com.ss.imagesDownloader.ConfigLoader;
import com.ss.imagesDownloader.dto.DownloadFormDto;
import com.ss.imagesDownloader.exceptions.ExceptionForUser;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.stereotype.Service;

@Service
public class DownloadService implements ExecutionService__I {
  private final String basePath;
  private String message;
  private final ExecutorService executorService;
  public static final long NO_ID = 0;
  private HashMap<Long, HtmlThread> htmlThreads = new HashMap<>();

  public DownloadService(ConfigLoader configLoader) {
    basePath = configLoader.getPath();
    int threadsNumber = configLoader.getThreadsNumber();
    this.executorService = Executors.newFixedThreadPool(threadsNumber);
  }

  public HtmlThread getThread(long id) {
    return htmlThreads.get(id);
  }

  public long downloadImagesFromUrl(DownloadFormDto form) {
    //url validation
    String htmlUrl = form.getUrl();
    if (htmlUrl.isEmpty() || form.getFolderName().isEmpty()) {
      if (htmlUrl.isEmpty()) {
        throw new ExceptionForUser("Url не должен быть пустым");
        //form.setIncorrectUrlMessage(true); //как обновлять страницу и выводить эту ошибку пользователю?
      }
      if (form.getFolderName().isEmpty()) {
        throw new ExceptionForUser("Название папки не должно быть пустым");
        //form.setEmptyNameMessage(true); //как обновлять страницу и выводить эту ошибку пользователю?
      }
      message = "";
      return NO_ID;
    }

    //html downloading

    String folderPath = basePath + "/" + form.getFolderName() + "/";
    File folder = new File(folderPath);
    if (!folder.exists()) {
      folder.mkdirs();
    }
    HtmlThread thread = new HtmlThread(htmlUrl, folderPath, this);
    executorService.submit(thread);
    htmlThreads.put(thread.getIdVl(), thread);
    return thread.getIdVl();
  }


  @Override
  public void submit(Thread thread) {
    executorService.submit(thread);
  }
}
