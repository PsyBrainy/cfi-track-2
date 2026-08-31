package com.wallet.alkemy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wallet.alkemy.models.tableUser;

@Repository 
public interface UserJpaRepository extends JpaRepository<tableUser, Long> {
    Optional<tableUser> findByEmail(String email);
    Optional<tableUser> findById(Long id);
    List<tableUser> findAll();
}
