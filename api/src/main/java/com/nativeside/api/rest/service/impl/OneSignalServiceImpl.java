package com.nativeside.api.rest.service.impl;

import static com.nativeside.api.rest.constants.Constants.CREATE_APP_URI;
import static com.nativeside.api.rest.constants.Constants.EXPORT_CSV_URI;
import static com.nativeside.api.rest.constants.Constants.FILE_NAME_CSV;
import static com.nativeside.api.rest.constants.Constants.PUSH_URI;
import static com.nativeside.api.rest.constants.Constants.THREAD_POOL_SIZE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mgnt.utils.StringUnicodeEncoderDecoder;
import com.nativeside.api.rest.domain.Advertisement;
import com.nativeside.api.rest.domain.Advertiser;
import com.nativeside.api.rest.domain.Advertiser.Names;
import com.nativeside.api.rest.domain.Publisher;
import com.nativeside.api.rest.domain.User;
import com.nativeside.api.rest.repository.PublisherRepository;
import com.nativeside.api.rest.repository.UserRepository;
import com.nativeside.api.rest.represantation.ApiRequest;
import com.nativeside.api.rest.represantation.OneSignalAppRequest;
import com.nativeside.api.rest.represantation.OneSignalAppResponse;
import com.nativeside.api.rest.represantation.OneSignalPushRequest;
import com.nativeside.api.rest.service.JdbcService;
import com.nativeside.api.rest.service.OneSignalService;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class OneSignalServiceImpl implements OneSignalService {

  private Table<Names, String, CompletableFuture<Integer>> pushCompletableFutureTable = HashBasedTable
      .create();

  @Value("${auth.key}")
  private String authKey;

  @Autowired
  private JdbcService jdbcService;

  @Autowired
  private PublisherRepository publisherRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ObjectMapper objectMapper;

  private final RestTemplate restTemplate = new RestTemplate();

  @Override
  public Table<Names, String, CompletableFuture<Integer>> getPushCompletableFutureTable() {
    return pushCompletableFutureTable;
  }


  @Override
  public OneSignalAppResponse createApp(OneSignalAppRequest oneSignalAppRequest) {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    httpHeaders.set("Authorization", "Basic " + authKey);

    HttpEntity<OneSignalAppRequest> entity = new HttpEntity<>(oneSignalAppRequest, httpHeaders);

    ResponseEntity<OneSignalAppResponse> appResponseResponseEntity = restTemplate
        .exchange(CREATE_APP_URI, HttpMethod.POST, entity, OneSignalAppResponse.class);

    OneSignalAppResponse oneSignalAppResponse = appResponseResponseEntity.getBody();

    if (oneSignalAppResponse != null) {
      publisherRepository.save(new Publisher(oneSignalAppResponse.getId(),
          oneSignalAppResponse.getBasicAuthKey(), oneSignalAppRequest.getName(),
          oneSignalAppRequest.getChromeWebOrigin()));
    }

    return oneSignalAppResponse;
  }

  @Override
  public String getUsers(ApiRequest apiRequest) throws IOException {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    httpHeaders.set("Authorization", "Basic " + apiRequest.getAppKey());
    String URI = EXPORT_CSV_URI + apiRequest.getAppId();

    HttpEntity<String> entity = new HttpEntity<>(extraFieldsInJson(), httpHeaders);

    ResponseEntity<Map<String, String>> responseEntity = restTemplate
        .exchange(URI, HttpMethod.POST, entity,
            new ParameterizedTypeReference<Map<String, String>>() {
            });

    Map<String, String> map = responseEntity.getBody();

    if (map != null) {
      CsvToBean<User> csvToBean = getCsvToBeanUsersFromCsvFile(
          responseEntity.getBody().get("csv_file_url"), FILE_NAME_CSV);
      log.info("Parsing CSV has finished");

      Set<User> userSet = new HashSet<>();
      for (User csvUser : csvToBean) {
        if (csvUser.getIp() != null && !csvUser.getIp().isEmpty()) {
          userSet.add(csvUser);
        }
      }


      userRepository.saveAll(userSet);

      Publisher publisher = publisherRepository.getOne(apiRequest.getAppId());
      publisher.setUsers(userSet);
      publisherRepository.save(publisher);
    } else {
      return "empty response";
    }

    return "hello";
  }


  @Override
  @SuppressWarnings("unchecked")
  public String push(ApiRequest apiRequest) {
    List<Advertisement> list = jdbcService.findAllAdvertisements(apiRequest.getPublisher(),
        apiRequest.getAdvertisement().name().toLowerCase());

    if (list.size() == 0) {
      return "nothing to send";
    }

    restTemplate.getMessageConverters()
        .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));

    //String key = "NGU4ZmM3NzgtNWJlNy00YTRiLWJiMDctMDRkMzg5YjY1NTM3";
    //String id = "16d19559-b9ae-418f-a71a-44ca2f748fd4";

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    httpHeaders.set("Authorization", "Basic " + apiRequest.getAppKey());

    CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
      ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

      AtomicInteger atomicInteger = new AtomicInteger();
      for (Advertisement advertisement : list) {

        executor.submit(() -> {

          Map<String, String> headlineMap = new HashMap<>();
          Map<String, String> content = new HashMap<>();

          headlineMap.put("en", StringUnicodeEncoderDecoder
              .decodeUnicodeSequenceToString(advertisement.getHeadline()));
          content.put("en", StringUnicodeEncoderDecoder
              .decodeUnicodeSequenceToString(advertisement.getDescription()));

          OneSignalPushRequest oneSignalPushRequest = new OneSignalPushRequest(apiRequest.getAppId(),
              Collections.singletonList(advertisement.getId()), headlineMap, content,
              advertisement.getImage(), advertisement.getIcon(), advertisement.getUrl());

          try {
            HttpEntity<String> entity = new HttpEntity<>(
                objectMapper.writeValueAsString(oneSignalPushRequest), httpHeaders);
            restTemplate.exchange(PUSH_URI, HttpMethod.POST, entity, String.class);
            atomicInteger.incrementAndGet();
          } catch (JsonProcessingException e) {
            throw new CompletionException(e);
          } catch (RuntimeException e) {
            log.error("{} didn't send. Reason: {}", oneSignalPushRequest, e.getMessage());
          }
        });

      }
      executor.shutdown();
      try {
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      return atomicInteger.get();
    });

    pushCompletableFutureTable
        .put(Advertiser.Names.valueOf(apiRequest.getAdvertisement().name()), apiRequest.getPublisher(), completableFuture);

    return apiRequest.getPublisher() + "_" + apiRequest.getAdvertisement().name();
  }

  private String extraFieldsInJson() {
    return "{\"extra_fields\": [\"location\", \"ip\", \"country\"]}";
  }

  private CsvToBean<User> getCsvToBeanUsersFromCsvFile(String fileUrl, String fileName)
      throws IOException {
    Reader reader = new FileReader(Files.write(Paths.get(fileName),
        IOUtils.toByteArray(
            new GZIPInputStream(new ByteArrayInputStream(getCsvGzipFileInBytes(fileUrl)))))
        .toFile());

    return new CsvToBeanBuilder<User>(reader)
        .withType(User.class)
        .withIgnoreLeadingWhiteSpace(true)
        .build();
  }

  private byte[] getCsvGzipFileInBytes(String CSV_URI) {
    try {
      byte[] csvFileInBytes = restTemplate.getForObject(CSV_URI, byte[].class);
      log.info("Csv file is ready = {}", CSV_URI);
      return csvFileInBytes;
    } catch (HttpClientErrorException ex) {
      log.error(ex.getMessage());
      return getCsvGzipFileInBytes(CSV_URI);
    }
  }

}
