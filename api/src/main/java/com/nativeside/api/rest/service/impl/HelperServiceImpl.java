package com.nativeside.api.rest.service.impl;

import com.nativeside.api.rest.domain.Advertisement;
import com.nativeside.api.rest.domain.Advertiser;
import com.nativeside.api.rest.domain.Advertiser.Names;
import com.nativeside.api.rest.domain.Publisher;
import com.nativeside.api.rest.repository.AdvertiserRepository;
import com.nativeside.api.rest.repository.PublisherRepository;
import com.nativeside.api.rest.repository.UserRepository;
import com.nativeside.api.rest.represantation.Stats;
import com.nativeside.api.rest.represantation.Stats.Status;
import com.nativeside.api.rest.service.AdvertiserService;
import com.nativeside.api.rest.service.HelperService;
import com.nativeside.api.rest.service.JdbcService;
import com.nativeside.api.rest.service.OneSignalService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HelperServiceImpl implements HelperService {

  @Autowired
  private AdvertiserRepository advertiserRepository;

  @Autowired
  private PublisherRepository publisherRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private AdvertiserService advertiserService;

  @Autowired
  private JdbcService jdbcService;

  @Autowired
  private OneSignalService oneSignalService;

  @Override
  public List<Advertiser> getAdvertisers() {
    return advertiserRepository.findAll();
  }

  @Override
  public List<Publisher> getPublishers() {
    return publisherRepository.findAll();
  }


  @Override
  public List<Stats> getStatisticByPublisherId(String id)
      throws ExecutionException, InterruptedException {
    String publisherName = publisherRepository.getOne(id).getName();
    int totalUsersCount = userRepository.findByPublishersId(id).size();
    List<Stats> statsList = new ArrayList<>();

    for (Names advName : Advertiser.Names.values()) {
      CompletableFuture<Integer> collectCompletableFuture = advertiserService
          .getCollectCompletableFutureTable().get(advName, publisherName);
      CompletableFuture<Integer> pushCompletableFuture = oneSignalService
          .getPushCompletableFutureTable().get(advName, publisherName);

      if (collectCompletableFuture != null) {

        if (collectCompletableFuture.isCompletedExceptionally()) {
          try {
            collectCompletableFuture.join();
          } catch (Exception ex){
            throw new RuntimeException(ex.getMessage());
          }
        }

        Stats stats = new Stats();
        stats.setTotalUsersCount(totalUsersCount);
        stats.setAdvertiser(advName.name());
        stats.setApplication(publisherName);
        if (advName.name().equals("MGID")) {
          stats.setProcessedCollectionUsersCount(AdvertiserServiceImpl.COUNT);
        }else stats.setProcessedCollectionUsersCount(AdvertiserServiceImpl.COUNT2);
        if (collectCompletableFuture.isDone()) {
          int collectionAdsCount = collectCompletableFuture.get();
          stats.setCollectionAdsCount(collectionAdsCount);
          stats.setCollectionFillRate((double) collectionAdsCount / totalUsersCount);
          stats.setStatus(Status.READY_FOR_PUSH);
        } else {
          stats.setStatus(Status.COLLECTING_IN_PROGRESS);
        }

        if (pushCompletableFuture != null) {
          if (pushCompletableFuture.isCompletedExceptionally()) {
            try {
              collectCompletableFuture.join();
            } catch (Exception ex){
              throw new RuntimeException(ex.getMessage());
            }
          }

          if (pushCompletableFuture.isDone()) {
            int successfulPush = pushCompletableFuture.get();
            stats.setSuccessfullyPushedAdsCount(successfulPush);
            stats.setStatus(Status.PUSH_FINISHED);
          } else {
            stats.setStatus(Status.PUSH_IN_PROGRESS);
          }
        }

        statsList.add(stats);
      }
    }
    return statsList;
  }

  @Override
  public Advertisement preview(String publisher, String advertiser) {
    return jdbcService.findFirst(publisher, advertiser);
  }

  @Override
  public String clearStats(String publisher, String advertiser) {
    advertiserService.getCollectCompletableFutureTable()
        .remove(Advertiser.Names.valueOf(advertiser.toUpperCase()), publisher);
    jdbcService.clearTable(publisher, advertiser);
    return publisher + "_" + advertiser;
  }

  @Override
  public String clearAllStats(String publisher) {
    for (Names names : advertiserService.getCollectCompletableFutureTable().rowKeySet()) {
      jdbcService.clearTable(publisher, names.name().toLowerCase());
    }
    advertiserService.getCollectCompletableFutureTable().clear();
    return "success";
  }
}
