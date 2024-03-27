package com.bridgelabz.campaign.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.Instant;
@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Campaign")
public class Campaign {
    @Id
    @Column(name = "campaign_id")
    private int campaignId;

    @NotNull(message = "Campaign name is required")
    @Column(name = "campaign_name", nullable = false)
    private String campaignName;

    @Column(name = "short_name")
    private String shortName;

    @Column(name = "campaign_description", length = 1000)
    private String campaignDescription;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date",nullable = false)
    private Instant startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date",nullable = false)
    private Instant endDate;

    @NotNull(message = "Max attempts is required")
    @Min(value = 1, message = "Max attempts must be greater than or equal to 1")
    @Column(name = "max_attempts",nullable = false)
    private Integer maxAttempts; // min 1 or higher

    @NotNull(message = "Pass percentage is required")
    @Min(value = 0, message = "Pass percentage cannot be negative")
    @Max(value = 100, message = "Pass percentage cannot be greater than 100")
    @Column(name = "pass_percentage",nullable = false)
    private Integer passPercentage; // min % required to clear

    @Column(name = "is_active")
    private boolean isActive;  // default false

    @Column(name = "is_archive")
    private boolean isArchive; // default false

}