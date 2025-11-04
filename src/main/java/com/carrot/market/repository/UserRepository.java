package com.carrot.market.repository;

import com.carrot.market.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository는 사용자 관련 데이터를 관리하는 JPA Repository입니다.
 * 아래 메서드는 모두 jpa 기본 제공 메서드입니다.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> { // <엔티티, Id>
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
