package com.nativeside.api.rest.service.impl;

import static com.nativeside.api.rest.constants.Constants.COLLECT_MGID_URI;
import static com.nativeside.api.rest.constants.Constants.COLLECT_REVCONTENT_URI;
import static com.nativeside.api.rest.constants.Constants.THREAD_POOL_SIZE;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mgnt.utils.StringUnicodeEncoderDecoder;
import com.nativeside.api.rest.domain.Advertisement;
import com.nativeside.api.rest.domain.Advertiser;
import com.nativeside.api.rest.domain.Advertiser.Names;
import com.nativeside.api.rest.domain.User;
import com.nativeside.api.rest.repository.PublisherRepository;
import com.nativeside.api.rest.repository.UserRepository;
import com.nativeside.api.rest.represantation.Content;
import com.nativeside.api.rest.represantation.RevcontentResponseWrapper;
import com.nativeside.api.rest.service.AdvertiserService;
import com.nativeside.api.rest.service.JdbcService;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class AdvertiserServiceImpl implements AdvertiserService {

  public static int COUNT;

  public static int COUNT2;

  private Table<Names, String, CompletableFuture<Integer>> collectCompletableFutureTable = HashBasedTable
      .create();

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private JdbcService jdbcService;

  @Autowired
  private PublisherRepository publisherRepository;

  private final RestTemplate restTemplate = new RestTemplate();


  @Override
  public Table<Names, String, CompletableFuture<Integer>> getCollectCompletableFutureTable() {
    return collectCompletableFutureTable;
  }

  @Override
  public String collectByPubAndAdv(String publisher, String advertiser) throws SQLException {
    boolean isTableExist = jdbcService.checkIfTableExist(jdbcService.getPubAndAdvTableName(publisher,advertiser));
    if (isTableExist) {
      List<Advertisement> list = jdbcService.findAllAdvertisements(publisher, advertiser);
      if (list != null && list.size() != 0) {
        return "Collect had already done by " + advertiser + " for " + publisher + " ";
      }
    }
    List<User> userList = userRepository
        .findByPublishersId(publisherRepository.findByName(publisher).getId());

    HttpHeaders httpHeaders = new HttpHeaders();

    CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
    switch (Advertiser.Names.valueOf(advertiser.toUpperCase())) {
      case REVCONTENT:
        completableFuture = collectByRevcontent(userList, httpHeaders, publisher, advertiser);
        break;
      case MGID:
        completableFuture = collectByMgid(userList, httpHeaders, publisher, advertiser);
        break;
    }

    collectCompletableFutureTable
        .put(Advertiser.Names.valueOf(advertiser.toUpperCase()), publisher, completableFuture);
    return publisher + "_" + advertiser;
  }


  private CompletableFuture<Integer> collectByRevcontent(List<User> userList,
      HttpHeaders httpHeaders, String publisher, String advertiser) {

    return CompletableFuture.supplyAsync(() -> {
      List<Advertisement> advertisementList = Collections.synchronizedList(new ArrayList<>());
      ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

      for (User user : userList) {
        httpHeaders.setAcceptLanguage(LanguageRange.parse(user.getLanguage()));

        executor.submit(() -> {
          ResponseEntity<RevcontentResponseWrapper> revcontentResponseWrapperResponseEntity;
          try {
            revcontentResponseWrapperResponseEntity = restTemplate
                .exchange(COLLECT_REVCONTENT_URI + user.getIp(), HttpMethod.GET,
                    new HttpEntity<>(httpHeaders), RevcontentResponseWrapper.class);

            RevcontentResponseWrapper revcontentResponseWrapper = revcontentResponseWrapperResponseEntity
                .getBody();

            if (revcontentResponseWrapper != null
                && revcontentResponseWrapper.getContent().size() != 0) {

              Content content = revcontentResponseWrapperResponseEntity.getBody().getContent()
                  .get(0);

              String description = content.getDescription();

              if (description.isEmpty()) {
                description = "Click to read!";
              }

              description = StringUnicodeEncoderDecoder.encodeStringToUnicodeSequence(description);
              String headline = StringUnicodeEncoderDecoder
                  .encodeStringToUnicodeSequence(content.getHeadline());

              advertisementList.add(new Advertisement(user.getId(), user.getIp(),
                  "https:" + content.getUrl(), headline, "https:" + content.getImage(),
                  "https:" + content.getImage(), description));

              COUNT2 = advertisementList.size();
            }
          } catch (Exception ex) {
            log.error(ex.getMessage());
          }
        });
      }
      executor.shutdown();
      try {
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      jdbcService.createTempTable(publisher, advertiser);
      jdbcService.batchInsert(publisher, advertiser, advertisementList);

      log.info("Collect for " + publisher + " by " + advertiser + " success. Collected size = "
          + advertisementList.size());

      return advertisementList.size();
    });

  }

  private CompletableFuture<Integer> collectByMgid(List<User> userList,
      HttpHeaders httpHeaders, String publisher, String advertiser) {

    return CompletableFuture.supplyAsync(() -> {
      List<Advertisement> advertisementList = Collections.synchronizedList(new ArrayList<>());
      ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
      for (User user : userList) {
        httpHeaders.setAcceptLanguage(LanguageRange.parse(user.getLanguage()));

        executor.submit(() -> {
          ResponseEntity<Content[]> mgidResponseWrapperResponseEntity;
          try {
            mgidResponseWrapperResponseEntity = restTemplate
                .exchange(COLLECT_MGID_URI + user.getIp(), HttpMethod.GET,
                    new HttpEntity<>(httpHeaders),
                    Content[].class);

            Content[] body = mgidResponseWrapperResponseEntity.getBody();

            if (body != null && body.length != 0) {

              Content content = mgidResponseWrapperResponseEntity.getBody()[0];

              String description = content.getDescription();

              if (description.isEmpty()) {
                description = "Click to read!";
              }

              description = StringUnicodeEncoderDecoder.encodeStringToUnicodeSequence(description);
              String headline = StringUnicodeEncoderDecoder
                  .encodeStringToUnicodeSequence(content.getHeadline());

              advertisementList
                  .add(new Advertisement(user.getId(), user.getIp(), "https:" + content.getUrl(),
                      headline, content.getImage(), content.getIcon(),
                      description));

              COUNT = advertisementList.size();
            }
          } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
          }
        });
      }

      executor.shutdown();
      try {
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      jdbcService.createTempTable(publisher, advertiser);
      jdbcService.batchInsert(publisher, advertiser, advertisementList);

      log.info("Collect for " + publisher + " by " + advertiser + " success. Collected size = "
          + advertisementList.size());

      return advertisementList.size();
    });

  }


}
