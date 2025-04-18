package com.ss.imagesDownloader.controller;

import com.ss.imagesDownloader.dto.DownloadFormDto;
import com.ss.imagesDownloader.dto.DownloadResponseDto;
import com.ss.imagesDownloader.exceptions.ExceptionForUser;
import com.ss.imagesDownloader.service.DownloadService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

  @PostMapping("/download")
  public String download(Model model, @ModelAttribute("downloadFormDto") DownloadFormDto form) {
    DownloadResponseDto response = new DownloadResponseDto();
    try {
      response = downloadService.downloadImagesFromUrl(form);
    } catch (ExceptionForUser exception) {
      response.setErrorMessage(exception.getMessage());
    }
    model.addAttribute("downloadFormDto", form);
    model.addAttribute("downloadResponseDto", response);
    model.addAttribute("downloadedImagesAmount", response.getDownloadedImagesAmount());
    model.addAttribute("downloadErrorsAmount", response.getDownloadErrorsAmount());
    model.addAttribute("errorMessage", response.getErrorMessage());
    return "download";
  }
}
