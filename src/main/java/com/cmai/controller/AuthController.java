package com.cmai.controller;

import com.cmai.dto.CoinRequest;
import com.cmai.dto.CoinResponse;
import com.cmai.dto.LoginRequest;
import com.cmai.dto.LoginResponse;
import com.cmai.dto.SessionUser;
import com.cmai.dto.UserDto;
import com.cmai.entity.User;
import com.cmai.service.AuthService;
import com.cmai.service.CoinService;
import com.cmai.service.SessionService;
import com.cmai.service.UserService;
import com.cmai.web.AuthInterceptor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;
    private final SessionService sessions;
    private final CoinService coins;
    private final UserService userService;

    public AuthController(
            AuthService authService, SessionService sessions, CoinService coins, UserService userService) {
        this.authService = authService;
        this.sessions = sessions;
        this.coins = coins;
        this.userService = userService;
    }

    /** Public: exchanges a Firebase (Google) ID token for a session cookie. */
    @PostMapping("/api/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        User user = authService.loginWithFirebaseToken(request.token());
        String sessionId = sessions.create(new SessionUser(user.getId(), user.getUsername(), user.getEmail()));
        response.addHeader(HttpHeaders.SET_COOKIE, sessions.cookie(sessionId).toString());
        return new LoginResponse(true, "Google login successful", UserDto.from(user));
    }

    @PostMapping("/api/auth/logout")
    public Map<String, Object> logout(
            @RequestAttribute(AuthInterceptor.ATTR_SESSION_ID) String sessionId, HttpServletResponse response) {
        sessions.delete(sessionId);
        response.addHeader(HttpHeaders.SET_COOKIE, sessions.clearingCookie().toString());
        return Map.of("success", true, "message", "Logged out successfully");
    }

    /**
     * Spends coins from the caller's own balance. Coins can only ever go DOWN through this API;
     * they are added solely by a verified Razorpay payment (the old /add-coins endpoint let anyone
     * mint free coins, so it was removed).
     */
    @PostMapping("/api/auth/use-coins")
    public CoinResponse useCoins(
            @RequestAttribute(AuthInterceptor.ATTR_USER) SessionUser user, @Valid @RequestBody CoinRequest request) {
        int balance = coins.deduct(user.userId(), request.coins());
        return new CoinResponse(true, "Coin updated successfully", balance, request.action());
    }

    @GetMapping("/api/me")
    public Map<String, Object> me(@RequestAttribute(AuthInterceptor.ATTR_USER) SessionUser user) {
        UserDto u = userService.get(user.userId());
        // Same shape the Node API returned (the Redis session object), plus fresh coins.
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("userId", u.id());
        out.put("_id", u.id());
        out.put("id", u.id());
        out.put("username", u.username());
        out.put("email", u.email());
        out.put("interviewCoin", u.interviewCoin());
        return Map.of("success", true, "user", out);
    }

    /**
     * Kept so existing clients do not break, but it no longer adds anything: coins are credited by the
     * server when a Razorpay payment is verified. (The old version let any user mint free coins.)
     */
    @PostMapping("/api/auth/add-coins")
    public Map<String, Object> addCoins(@RequestAttribute(AuthInterceptor.ATTR_USER) SessionUser user) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("message", "Coins are credited automatically after payment verification");
        out.put("interviewCoin", coins.balance(user.userId()));
        return out;
    }
}
