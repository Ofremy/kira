package com.nativeside.api.rest.controller;

import com.nativeside.api.rest.service.AdvertiserService;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdvertiserController implements BaseController {

  @Autowired
  private AdvertiserService advertiserService;

  @GetMapping(path = "/collect")
  public String collect(@RequestParam String publisher, @RequestParam String advertiser) throws Exception {
    return advertiserService.collectByPubAndAdv(publisher, advertiser);
  }

}
