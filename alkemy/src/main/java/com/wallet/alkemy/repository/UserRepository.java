
package com.wallet.alkemy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.wallet.alkemy.models.tableUser;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final UserJpaRepository userJpaRepository;

    /** Finds a user by email address. */
    public Optional<tableUser> findByEmail(String email) {
        return userJpaRepository.findByEmail(email);
    }

    /** Finds a user by primary key. */
    public Optional<tableUser> findById(Long id) {
        return userJpaRepository.findById(id);
    }

    /** Persists a user and returns the saved entity. */
    public tableUser save(tableUser user) {
        return userJpaRepository.save(user);
    }

    /** Returns all users. */
    public List<tableUser> findAll() {
        return userJpaRepository.findAll();
    }

    /** Deletes a user by primary key. */
    public void deleteById(Long id) {
        userJpaRepository.deleteById(id);
    }
}