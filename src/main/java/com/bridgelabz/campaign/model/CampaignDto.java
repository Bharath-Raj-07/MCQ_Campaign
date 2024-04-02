package com.bridgelabz.campaign.model;

import lombok.Data;

import java.time.Instant;
import java.util.Objects;

@Data
public class CampaignDto {
    private String campaignName;
    private String shortName;
    private String campaignDescription;
    private Instant startDate;
    private Instant endDate;
    private Integer maxAttempts;
    private Integer passPercentage;
    private boolean isActive;
    private boolean isArchive;

    public CampaignDto(String campaignName, String shortName, String campaignDescription, Instant startDate, Instant endDate, Integer maxAttempts, Integer passPercentage, boolean isActive, boolean isArchive) {
        this.campaignName = campaignName;
        this.shortName = shortName;
        this.campaignDescription = campaignDescription;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxAttempts = maxAttempts;
        this.passPercentage = passPercentage;
        this.isActive = isActive;
        this.isArchive = isArchive;
    }
}


