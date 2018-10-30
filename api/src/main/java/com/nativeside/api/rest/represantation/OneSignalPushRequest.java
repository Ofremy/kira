package com.nativeside.api.rest.represantation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OneSignalPushRequest {

  @JsonProperty("app_id")
  private String appId;

  @JsonProperty("include_player_ids")
  private List<String> includePlayerIds;

  private Map<String, String> headings;

  private Map<String, String> contents;

  @JsonProperty("chrome_web_image")
  private String chromeWebImage;

  @JsonProperty("chrome_web_icon")
  private String chromeWebIcon;

  private String url;


}
