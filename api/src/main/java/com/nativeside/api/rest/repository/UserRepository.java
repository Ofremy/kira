package com.nativeside.api.rest.repository;

import com.nativeside.api.rest.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
  List<User> findByPublishersId(String id);
}