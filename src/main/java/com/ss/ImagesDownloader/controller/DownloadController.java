package com.ss.ImagesDownloader.controller;

import com.ss.ImagesDownloader.dto.DtoDownloadForm;
import com.ss.ImagesDownloader.service.DownloadService;
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

  @GetMapping("download")
  public String downloadSubmit(Model model) {
    DtoDownloadForm form = new DtoDownloadForm();
    model.addAttribute("dtoDownloadForm", form);
    return "download";
  }

  @PostMapping("download/save")
  public String download(Model model, @ModelAttribute("dtoDownloadForm") DtoDownloadForm form) {
    model.addAttribute("dtoDownloadForm", form);
    return "download-success";
  }
}
