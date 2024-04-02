package com.bridgelabz.campaign.utility;

import com.bridgelabz.campaign.model.Campaign;
import com.bridgelabz.campaign.model.CampaignDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CampaignMapper {
    private final ModelMapper modelMapper;
    @Autowired
    public CampaignMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }
    public CampaignDto convertToDto(Campaign campaign) {
        return modelMapper.map(campaign, CampaignDto.class);
    }

    public Campaign convertToEntity(CampaignDto campaignDto) {
        return modelMapper.map(campaignDto, Campaign.class);
    }
}
