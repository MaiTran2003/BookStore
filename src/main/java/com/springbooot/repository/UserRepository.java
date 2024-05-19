package com.springbooot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import com.springbooot.entities.Role;
import com.springbooot.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    User findByRole(Role role);

    Optional<User> findByVerificationToken(String verificationToken);

    @Query("SELECT u.loggedOutTokens FROM User u WHERE u.email = ?1")
    List<String> findLoggedOutTokensByEmail(String email);

    Optional<User> findById(Long id);

    @NonNull
    List<User> findAll();

    void deleteById(Long id);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);

    Page<User> findAll(Specification<User> spec, Pageable pageable);

}
