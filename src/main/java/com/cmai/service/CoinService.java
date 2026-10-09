package com.cmai.service;

import com.cmai.exception.ApiException;
import com.cmai.repository.UserRepository;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** All coin movements go through single atomic UPDATE statements, so concurrent requests cannot overspend. */
@Service
public class CoinService {

    private final UserRepository users;

    public CoinService(UserRepository users) {
        this.users = users;
    }

    /** @return the balance after deducting. @throws ApiException 403 when the balance is too low. */
    @Transactional
    public int deduct(Long userId, int amount) {
        int updated = users.deductCoins(userId, amount);
        if (updated == 0) {
            int balance = users.findById(userId)
                    .orElseThrow(() -> new ApiException(404, "User not found"))
                    .getInterviewCoin();
            throw new ApiException(403, "Insufficient coins", Map.of("interviewCoin", balance));
        }
        return balance(userId);
    }

    /** @return the balance after crediting. */
    @Transactional
    public int credit(Long userId, int amount) {
        if (users.addCoins(userId, amount) == 0) {
            throw new ApiException(404, "User not found");
        }
        return balance(userId);
    }

    @Transactional(readOnly = true)
    public int balance(Long userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ApiException(404, "User not found"))
                .getInterviewCoin();
    }
}
