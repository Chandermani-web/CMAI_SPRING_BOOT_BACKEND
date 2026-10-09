package com.cmai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "billings", indexes = @Index(name = "idx_billings_user", columnList = "user_id"))
public class Billing extends BaseEntity {

    public static final String STATUS_CREATED = "created";
    public static final String STATUS_PAID = "paid";
    public static final String STATUS_FAILED = "failed";

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Amount in INR (rupees). */
    @Column(nullable = false)
    private int amount;

    @Column(name = "interview_coin", nullable = false)
    private int interviewCoin;

    @Column(name = "razorpay_order_id", nullable = false, unique = true, length = 64)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 64)
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature", length = 128)
    private String razorpaySignature;

    @Column(nullable = false, length = 20)
    private String status = STATUS_CREATED;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public int getInterviewCoin() { return interviewCoin; }
    public void setInterviewCoin(int interviewCoin) { this.interviewCoin = interviewCoin; }
    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }
    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }
    public String getRazorpaySignature() { return razorpaySignature; }
    public void setRazorpaySignature(String razorpaySignature) { this.razorpaySignature = razorpaySignature; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
