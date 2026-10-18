package com.syos.domain.repository;

import com.syos.domain.entity.User;
import java.util.Optional;

public interface UserRepository {

      // authenticates a user against the database records
      //returns an Optional containing the User if credentials match
    Optional<User> authenticate(String username, String password);
}