package com.nativeside.api.rest.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Advertisement {

  private String id;

  private String ip;

  private String url;

  private String headline;

  private String image;

  private String icon;

  private String description;

}
