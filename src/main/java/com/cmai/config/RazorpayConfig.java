package com.cmai.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {

    @Bean
    public RazorpayClient razorpayClient(AppProperties props) throws RazorpayException {
        return new RazorpayClient(props.razorpay().keyId(), props.razorpay().keySecret());
    }
}
