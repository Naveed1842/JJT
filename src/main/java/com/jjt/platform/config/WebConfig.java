package com.jjt.platform.config;

import org.springframework.context.annotation.Configuration;

// CORS is configured centrally in SecurityConfig.corsConfigurationSource().
// Do not add CORS mappings here — they conflict with the Security filter chain.
@Configuration
public class WebConfig {
}
