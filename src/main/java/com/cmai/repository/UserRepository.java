package com.cmai.repository;

import com.cmai.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByFirebaseUid(String firebaseUid);

    Optional<User> findByEmail(String email);

    /** Atomic: only deducts when the balance is sufficient. Returns the number of rows changed (0 or 1). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update User u set u.interviewCoin = u.interviewCoin - :amount "
            + "where u.id = :id and u.interviewCoin >= :amount")
    int deductCoins(@Param("id") Long id, @Param("amount") int amount);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update User u set u.interviewCoin = u.interviewCoin + :amount where u.id = :id")
    int addCoins(@Param("id") Long id, @Param("amount") int amount);
}
