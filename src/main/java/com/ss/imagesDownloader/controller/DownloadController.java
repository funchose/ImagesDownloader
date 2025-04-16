package com.ss.imagesDownloader.controller;

import com.ss.imagesDownloader.dto.DownloadFormDto;
import com.ss.imagesDownloader.dto.DownloadResponseDto;
import com.ss.imagesDownloader.exceptions.ExceptionForUser;
import com.ss.imagesDownloader.service.DownloadService;
import com.ss.imagesDownloader.service.HtmlThread;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class DownloadController {
  private final DownloadService downloadService;

  public DownloadController(DownloadService downloadService) {
    this.downloadService = downloadService;
  }

  @GetMapping("/download")
  public String downloadSubmit(Model model) {
    DownloadFormDto form = new DownloadFormDto();
    model.addAttribute("downloadFormDto", form);
    DownloadResponseDto response = new DownloadResponseDto();
    model.addAttribute("downloadResponseDto", response);
    return "download";
  }

  @GetMapping("/status/{id}")
  public String getStatus(@PathVariable long id, Model model) {
    HtmlThread thread = downloadService.getThread(id);
    if (thread == null) {
      model.addAttribute("errorMessage",
          "Поток с id: " + id + " не найден");
      return "error";
    }
    if (thread.isError()) {
      model.addAttribute("errorMessage", thread.getErrorMessage());
      return "error";
    }
    model.addAttribute("thread", thread);
    return "status";
  }

  @PostMapping("/download")
  public String download(Model model, @ModelAttribute("downloadFormDto") DownloadFormDto form) {
    long response = 0;
    try {
      response = downloadService.downloadImagesFromUrl(form);
    } catch (ExceptionForUser exception) {
      model.addAttribute("errorMessage", exception.getMessage());
      return "error";
    }
    return "redirect:/status/" + response;
  }
}
