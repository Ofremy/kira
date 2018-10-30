package com.nativeside.api.rest.service;

import com.google.common.collect.Table;
import com.nativeside.api.rest.domain.Advertiser.Names;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.persistence.criteria.CriteriaBuilder.In;

public interface AdvertiserService {

  Table<Names, String, CompletableFuture<Integer>> getCollectCompletableFutureTable();

  String collectByPubAndAdv(String publisher, String advertiser) throws IOException, SQLException;

}
