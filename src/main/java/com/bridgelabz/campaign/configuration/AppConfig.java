package com.bridgelabz.campaign.configuration;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class AppConfig {
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("message"); // Set the base name of your message properties file
        messageSource.setDefaultEncoding("UTF-8"); // Set the default encoding for message properties files
        return messageSource;
    }
}
