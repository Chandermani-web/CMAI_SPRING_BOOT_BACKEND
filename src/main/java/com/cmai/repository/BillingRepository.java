package com.cmai.repository;

import com.cmai.entity.Billing;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BillingRepository extends JpaRepository<Billing, Long> {

    /** Row-locks the billing record so a payment can only be credited once, even under concurrent calls. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Billing b where b.razorpayOrderId = :orderId and b.userId = :userId")
    Optional<Billing> findForUpdate(@Param("orderId") String orderId, @Param("userId") Long userId);
}
