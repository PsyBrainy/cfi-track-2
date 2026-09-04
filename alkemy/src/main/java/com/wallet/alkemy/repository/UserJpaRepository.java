package com.wallet.alkemy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wallet.alkemy.models.tableUser;

@Repository 
public interface UserJpaRepository extends JpaRepository<tableUser, Long> {
    /** Finds a user by email address. */
    Optional<tableUser> findByEmail(String email);

    /** Finds a user by primary key. */
    Optional<tableUser> findById(Long id);

    /** Returns all users. */
    List<tableUser> findAll();
}
