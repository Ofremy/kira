package com.nativeside.api.rest.service;

import com.nativeside.api.rest.domain.Advertisement;
import com.nativeside.api.rest.domain.Advertiser;
import com.nativeside.api.rest.domain.Publisher;
import com.nativeside.api.rest.represantation.Stats;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface HelperService {

  List<Advertiser> getAdvertisers();

  List<Publisher> getPublishers();

  List<Stats> getStatisticByPublisherId(String id) throws ExecutionException, InterruptedException;

  Advertisement preview(String publisher, String advertiser);

  String clearStats(String publisher, String advertiser);

  String clearAllStats(String publisher);

}
