package com.nativeside.api.rest.service;

import com.nativeside.api.rest.domain.Advertisement;
import java.sql.SQLException;
import java.util.List;

public interface JdbcService {

  void createTempTable(String publisher, String advertiser);

  void batchInsert(String publisher, String advertiser,
      List<Advertisement> advertisementList);

  boolean checkIfTableExist(String tableName) throws SQLException;

  List<Advertisement> findAllAdvertisements(String publisher, String advertiser);

  void clearTable(String publisher, String advertiser);

  String getPubAndAdvTableName(String publisher, String advertiser);

  Advertisement findFirst(String publisher, String advertiser);
}
