package com.nativeside.api.rest.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "publisher")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Publisher {

  @Id
  @Column
  @Size(max = 100)
  private String id;

  @Column(name = "basic_auth_key")
  @Size(max = 100)
  @NotNull
  private String basicAuthKey;

  @Column(name = "chrome_web_origin")
  private String chromeWebOrigin;

  @Column
  @Size(max = 100)
  private String name;

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "publisher_user",
      joinColumns = {@JoinColumn(name = "publisher_id")},
      inverseJoinColumns = {@JoinColumn(name = "user_id")})
  private Set<User> users;

  public Publisher(String id, String basicAuthKey, String name, String chromeWebOrigin) {
    this.id = id;
    this.basicAuthKey = basicAuthKey;
    this.name = name;
    this.chromeWebOrigin = chromeWebOrigin;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Publisher publisher = (Publisher) o;
    return Objects.equals(getId(), publisher.getId()) &&
        Objects.equals(getBasicAuthKey(), publisher.getBasicAuthKey());
  }

  @Override
  public int hashCode() {

    return Objects.hash(getId(), getBasicAuthKey());
  }

  @Override
  public String toString() {
    return "Publisher{" +
        "id='" + id + '\'' +
        ", basicAuthKey='" + basicAuthKey + '\'' +
        '}';
  }
}
