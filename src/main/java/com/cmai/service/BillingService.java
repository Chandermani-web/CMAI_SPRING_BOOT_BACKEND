package com.cmai.service;

import com.cmai.config.AppProperties;
import com.cmai.dto.PlanDto;
import com.cmai.dto.VerifyPaymentRequest;
import com.cmai.entity.Billing;
import com.cmai.exception.ApiException;
import com.cmai.repository.BillingRepository;
import com.cmai.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.HexFormat;

@Service
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);

    private static final Map<String, PlanDto> PLANS =
            Map.of("starter", new PlanDto("starter", "Starter", 199, "INR", 300));

    private final RazorpayClient razorpay;
    private final BillingRepository billings;
    private final UserRepository users;
    private final CoinService coins;
    private final TransactionTemplate tx;
    private final AppProperties props;

    public BillingService(
            RazorpayClient razorpay,
            BillingRepository billings,
            UserRepository users,
            CoinService coins,
            TransactionTemplate tx,
            AppProperties props) {
        this.razorpay = razorpay;
        this.billings = billings;
        this.users = users;
        this.coins = coins;
        this.tx = tx;
        this.props = props;
    }

    public List<PlanDto> plans() {
        return List.copyOf(PLANS.values());
    }

    public int interviewCost() {
        return props.coins().interviewCost();
    }

    /** Creates a Razorpay order and a matching "created" billing record. */
    public Map<String, Object> createOrder(Long userId, String planId) {
        PlanDto plan = PLANS.get(planId);
        if (plan == null) {
            throw new ApiException(400, "Invalid plan ID");
        }

        JSONObject request = new JSONObject();
        request.put("amount", plan.amount() * 100); // paise
        request.put("currency", plan.currency());
        request.put("receipt", "rcpt_" + userId + "_" + System.currentTimeMillis());
        request.put("payment_capture", 1);

        Order order;
        try {
            order = razorpay.orders.create(request);
        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new ApiException(502, "Could not create the payment order. Please try again.");
        }

        String orderId = order.get("id");
        if (orderId == null || orderId.isBlank()) {
            throw new ApiException(502, "Could not create the payment order. Please try again.");
        }

        tx.executeWithoutResult(status -> {
            Billing billing = new Billing();
            billing.setUserId(userId);
            billing.setAmount(plan.amount());
            billing.setInterviewCoin(plan.interviewCoin());
            billing.setRazorpayOrderId(orderId);
            billing.setStatus(Billing.STATUS_CREATED);
            billings.save(billing);
        });

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("order", order.toJson().toMap());
        result.put("keyId", props.razorpay().keyId());
        result.put("plan", plan);
        return result;
    }

    public record VerifyOutcome(boolean valid, boolean alreadyVerified, int interviewCoin) {}

    /**
     * Checks the Razorpay signature and credits the coins exactly once. The billing row is locked
     * for the duration of the transaction, so a double submit cannot credit twice.
     */
    public VerifyOutcome verifyPayment(Long userId, VerifyPaymentRequest req) {
        return Objects.requireNonNull(tx.execute(status -> {
            Billing billing = billings.findForUpdate(req.razorpayOrderId(), userId)
                    .orElseThrow(() -> new ApiException(404, "Billing record not found"));

            if (Billing.STATUS_PAID.equals(billing.getStatus())) {
                return new VerifyOutcome(true, true, coins.balance(userId));
            }

            String expected = hmacSha256Hex(req.razorpayOrderId() + "|" + req.razorpayPaymentId());
            boolean valid = MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    req.razorpaySignature().trim().getBytes(StandardCharsets.UTF_8));

            if (!valid) {
                billing.setStatus(Billing.STATUS_FAILED);
                billings.save(billing);
                return new VerifyOutcome(false, false, coins.balance(userId));
            }

            billing.setStatus(Billing.STATUS_PAID);
            billing.setRazorpayPaymentId(req.razorpayPaymentId());
            billing.setRazorpaySignature(req.razorpaySignature().trim());
            billings.save(billing);

            users.addCoins(userId, billing.getInterviewCoin());
            return new VerifyOutcome(true, false, coins.balance(userId));
        }));
    }

    private String hmacSha256Hex(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(props.razorpay().keySecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 unavailable", e);
        }
    }
}
