package com.nativeside.api.rest.controller;

import com.nativeside.api.rest.represantation.ApiRequest;
import com.nativeside.api.rest.represantation.OneSignalAppRequest;
import com.nativeside.api.rest.represantation.OneSignalAppResponse;
import com.nativeside.api.rest.service.OneSignalService;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class OneSignalController implements BaseController {

  @Autowired
  private OneSignalService oneSignalService;

  @PostMapping(path = "/create_app")
  public OneSignalAppResponse createApp(@RequestBody OneSignalAppRequest oneSignalAppRequest) {
    return oneSignalService.createApp(oneSignalAppRequest);
  }

  @PostMapping(path = "/users")
  public String exportCSV(@RequestBody ApiRequest apiRequest) throws IOException {
    return oneSignalService.getUsers(apiRequest);
  }

  @PostMapping(path = "/push")
  public String push(@RequestBody ApiRequest apiRequest) {
    return oneSignalService.push(apiRequest);
  }

}
