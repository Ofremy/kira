package com.nativeside.api.rest.service.impl;

import com.nativeside.api.rest.domain.Advertisement;
import com.nativeside.api.rest.service.JdbcService;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcServiceImpl implements JdbcService {

  @Autowired
  private JdbcTemplate jdbcTemplate;


  @Autowired
  private HikariDataSource nativeSideDataSource;

  @Override
  public void createTempTable(String publisher, String advertiser) {
    String sqlCreateTable =
        "CREATE table if not EXISTS "
            + getPubAndAdvTableName(publisher, advertiser)
            + " (id VARCHAR(225), ip varchar(100), url TEXT, headline TEXT, image TEXT, icon TEXT, description TEXT, PRIMARY KEY (id))";
    jdbcTemplate.execute(sqlCreateTable);
  }

  @Override
  public void batchInsert(String publisher, String advertiser,
      List<Advertisement> advertisementList) {
    String sql = "insert into " + getPubAndAdvTableName(publisher, advertiser)
        + " (id, ip, url, headline, image, icon, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
    System.out.println(advertisementList.size() + " tyt");
    try {
      int[][] ints = jdbcTemplate.batchUpdate(sql, advertisementList, advertisementList.size(),
          (preparedStatement, advertisement) -> {
            preparedStatement.setString(1, advertisement.getId());
            preparedStatement.setString(2, advertisement.getIp());
            preparedStatement.setString(3, advertisement.getUrl());
            preparedStatement.setString(4, advertisement.getHeadline());
            preparedStatement.setString(5, advertisement.getImage());
            preparedStatement.setString(6, advertisement.getIcon());
            preparedStatement.setString(7, advertisement.getDescription());
          });
    } catch (Exception ex) {
      System.out.println(advertisementList.size());
      ex.printStackTrace();
    }
  }

  @Override
  public boolean checkIfTableExist(String tableName) throws SQLException {
    DatabaseMetaData databaseMetaData = nativeSideDataSource.getConnection().getMetaData();
    ResultSet resultSet = databaseMetaData.getTables(null, null, tableName, new String[]{"TABLE"});
    return resultSet.next();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Advertisement> findAllAdvertisements(String publisher, String advertiser) {
    return jdbcTemplate.query("SELECT * FROM " + getPubAndAdvTableName(publisher, advertiser),
        new BeanPropertyRowMapper(Advertisement.class));
  }

  @Override
  public void clearTable(String publisher, String advertiser) {
    jdbcTemplate.execute("TRUNCATE TABLE " + getPubAndAdvTableName(publisher, advertiser));
  }

  @Override
  public Advertisement findFirst(String publisher, String advertiser) {
    return jdbcTemplate.queryForObject(
        "SELECT * FROM " + getPubAndAdvTableName(publisher, advertiser) + " LIMIT 1",
        new BeanPropertyRowMapper<>(Advertisement.class));
  }

  @Override
  public String getPubAndAdvTableName(String publisher, String advertiser) {
    return publisher.toLowerCase() + "_" + advertiser.toLowerCase();
  }
}
