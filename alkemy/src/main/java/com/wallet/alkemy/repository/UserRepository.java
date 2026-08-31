
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

    public Optional<tableUser> findByEmail(String email) {
        return userJpaRepository.findByEmail(email);
    }

    public Optional<tableUser> findById(Long id) {
        return userJpaRepository.findById(id);
    }

    public tableUser save(tableUser user) {
        return userJpaRepository.save(user);
    }

    public List<tableUser> findAll() {
        return userJpaRepository.findAll();
    }

    public void deleteById(Long id) {
        userJpaRepository.deleteById(id);
    }
}