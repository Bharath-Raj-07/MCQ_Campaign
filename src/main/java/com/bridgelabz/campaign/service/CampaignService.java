package com.bridgelabz.campaign.service;

import com.bridgelabz.campaign.model.Campaign;
import com.bridgelabz.campaign.utility.Response;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CampaignService {

    Mono<ResponseEntity<Response>> create(Campaign campaign);
    Mono<ResponseEntity<Response>> getAll();
    Mono<ResponseEntity<Response>> getOne(int campaignId);
    Mono<ResponseEntity<Response>> update(int campaignId, Campaign updatedCampaign);
    Mono<ResponseEntity<Response>> deleteById(int campaignId);
    Mono<ResponseEntity<Response>> deleteByName(String campaignName);

}
