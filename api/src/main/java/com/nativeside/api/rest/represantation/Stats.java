package com.nativeside.api.rest.represantation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Stats {

  private String application;

  private String advertiser;

  private int totalUsersCount;

  private int processedCollectionUsersCount;

  private int collectionAdsCount;

  private int successfullyPushedAdsCount;

  private Status status;

  private double collectionFillRate;


  public enum Status{
    COLLECTING_IN_PROGRESS,
    READY_FOR_PUSH,
    PUSH_IN_PROGRESS,
    PUSH_FINISHED
  }

}
