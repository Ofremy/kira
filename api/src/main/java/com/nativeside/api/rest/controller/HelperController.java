package com.nativeside.api.rest.controller;

import com.nativeside.api.rest.domain.Advertisement;
import com.nativeside.api.rest.domain.Advertiser;
import com.nativeside.api.rest.domain.Publisher;
import com.nativeside.api.rest.represantation.Stats;
import com.nativeside.api.rest.service.HelperService;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class HelperController implements BaseController {

  @Autowired
  private HelperService helperService;

  @GetMapping(path = "/advertisers")
  public List<Advertiser> getAdvertisers() {
    return helperService.getAdvertisers();
  }

  @GetMapping(path = "/stats/{id}")
  public List<Stats> getStats(@PathVariable String id)
      throws ExecutionException, InterruptedException {
    return helperService.getStatisticByPublisherId(id);
  }

  @GetMapping(path = "/preview")
  public Advertisement preview(@RequestParam String publisher, @RequestParam String advertiser) {
    return helperService.preview(publisher, advertiser);
  }

  @GetMapping(path = "/clear")
  public String clear(@RequestParam String publisher, @RequestParam String advertiser) {
    return helperService.clearStats(publisher, advertiser);
  }

  @GetMapping(path = "/clear/{publisher}")
  public String clearAll(@PathVariable String publisher) {
    return helperService.clearAllStats(publisher);
  }

  @GetMapping(path = "/sites")
  public List<Publisher> getPublishers() {
    List<Publisher> publisherList = helperService.getPublishers();
    for (Publisher publisher : publisherList) {
      publisher.setUsers(null);
      publisher.setBasicAuthKey(null);
      publisher.setChromeWebOrigin(null);
    }
    return publisherList;
  }
}
