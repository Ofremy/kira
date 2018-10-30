package com.nativeside.api.rest.repository;

import com.nativeside.api.rest.domain.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, String> {
  Publisher findByName(String name);
}
