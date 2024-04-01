package com.bridgelabz.campaign.service;

import com.bridgelabz.campaign.model.Campaign;
import com.bridgelabz.campaign.utility.ResponseMessage;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface CampaignService {

    Mono<ResponseEntity<ResponseMessage>> createCampaign(Campaign campaign);
    Mono<ResponseEntity<ResponseMessage>> getAllCampaigns();
    Mono<ResponseEntity<ResponseMessage>> getCampaign(int campaignId);
    Mono<ResponseEntity<ResponseMessage>> update(int campaignId, Campaign updatedCampaign);
    Mono<ResponseEntity<ResponseMessage>> deleteById(int campaignId);
    Mono<ResponseEntity<ResponseMessage>> deleteByName(String campaignName);

}
