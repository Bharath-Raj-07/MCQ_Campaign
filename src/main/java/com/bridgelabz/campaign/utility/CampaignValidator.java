package com.bridgelabz.campaign.utility;

import com.bridgelabz.campaign.model.Campaign;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Component
public class CampaignValidator {

    private final Validator validator;

    public CampaignValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    public void validate(Object target) {
        Set<ConstraintViolation<Object>> violations = validator.validate(target);

        for (ConstraintViolation<Object> violation : violations) {
            throw new IllegalArgumentException(violation.getMessage());
        }
    }
    public void validateCampaignDates(Campaign campaign) {
        Instant startDate = campaign.getStartDate();
        Instant endDate = campaign.getEndDate();
        Instant currentInstant = Instant.now();

        // Check if start date is in the past
        if (startDate.isBefore(currentInstant)) {
            throw new IllegalArgumentException("Start date must be greater than current date");
        }

        // Check if end date is at least 3 months after start date
        Instant threeMonthsAfterStart = startDate.plus(Duration.ofDays(90)); // Assuming 3 months = 90 days
        if (endDate.isBefore(threeMonthsAfterStart)) {
            throw new IllegalArgumentException("End date must be at least 3 months after start date");
        }
    }

}
