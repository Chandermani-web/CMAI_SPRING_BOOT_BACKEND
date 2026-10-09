package com.cmai.config;

import com.cmai.web.AuthInterceptor;
import com.cmai.web.ApiLoggingInterceptor;
import com.cmai.web.OriginCheckInterceptor;
import com.cmai.web.RateLimitInterceptor;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AppProperties props;
    private final ApiLoggingInterceptor apiLoggingInterceptor;
    private final AuthInterceptor authInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;
    private final OriginCheckInterceptor originCheckInterceptor;

    public WebConfig(
            AppProperties props,
            ApiLoggingInterceptor apiLoggingInterceptor,
            AuthInterceptor authInterceptor,
            RateLimitInterceptor rateLimitInterceptor,
            OriginCheckInterceptor originCheckInterceptor) {
        this.props = props;
        this.apiLoggingInterceptor = apiLoggingInterceptor;
        this.authInterceptor = authInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
        this.originCheckInterceptor = originCheckInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = props.cors().allowedOrigins();
        registry.addMapping("/**")
                .allowedOrigins(origins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiLoggingInterceptor).addPathPatterns("/**");
        registry.addInterceptor(originCheckInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
    }
}
