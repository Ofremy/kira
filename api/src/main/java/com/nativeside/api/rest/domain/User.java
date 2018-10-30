package com.nativeside.api.rest.domain;

import com.opencsv.bean.CsvBindByName;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User {

  @Id
  @Column
  @Size(max = 100)
  @CsvBindByName
  private String id;

  @Column
  @Size(max = 50)
  @CsvBindByName
  private String timezone;

  @Column
  @Size(max = 100)
  @CsvBindByName
  private String ip;

  @Column
  @Size(max = 10)
  @CsvBindByName
  private String country;

  @Column
  @CsvBindByName
  private String language;

  @ManyToMany(mappedBy = "users")
  private Set<Publisher> publishers;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return Objects.equals(getId(), user.getId()) &&
        Objects.equals(getTimezone(), user.getTimezone()) &&
        Objects.equals(getIp(), user.getIp()) &&
        Objects.equals(getCountry(), user.getCountry());
  }

  @Override
  public int hashCode() {

    return Objects.hash(getId(), getTimezone(), getIp(), getCountry());
  }

  @Override
  public String toString() {
    return "User{" +
        "id='" + id + '\'' +
        ", timezone='" + timezone + '\'' +
        ", ip='" + ip + '\'' +
        ", country='" + country + '\'' +
        '}';
  }
}
