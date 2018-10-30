package com.nativeside.api.rest.service;

import com.google.common.collect.Table;
import com.nativeside.api.rest.domain.Advertiser.Names;
import com.nativeside.api.rest.represantation.ApiRequest;
import com.nativeside.api.rest.represantation.OneSignalAppRequest;
import com.nativeside.api.rest.represantation.OneSignalAppResponse;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface OneSignalService {

  OneSignalAppResponse createApp(OneSignalAppRequest oneSignalAppRequest);

  String getUsers(ApiRequest apiRequest) throws IOException;

  String push(ApiRequest apiRequest);

  Table<Names, String, CompletableFuture<Integer>> getPushCompletableFutureTable();
}
