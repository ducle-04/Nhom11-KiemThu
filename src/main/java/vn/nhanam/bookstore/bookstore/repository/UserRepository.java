package vn.nhanam.bookstore.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.nhanam.bookstore.bookstore.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Login bằng username
    Optional<User> findByUsername(String username);

    // Login bằng email
    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
