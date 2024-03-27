package com.bridgelabz.campaign.repository;

import com.bridgelabz.campaign.model.Campaign;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CampaignRepository extends R2dbcRepository<Campaign, Integer> {
    <T> Mono<T> deleteByCampaignName(String campaignName);
    Mono<Campaign> findByCampaignId(int campaignId);
    Mono<Boolean> existsByCampaignId(int campaignId);
    Mono<Object> deleteByCampaignId(int campaignId);
    Mono<Boolean> existsByCampaignName(String campaignName);

}
