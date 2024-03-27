package com.bridgelabz.campaign.service;

import com.bridgelabz.campaign.model.Campaign;
import com.bridgelabz.campaign.repository.CampaignRepository;
import com.bridgelabz.campaign.utility.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CampaignServiceImpl implements CampaignService{

    public final CampaignRepository campaignRepository;
    public CampaignServiceImpl(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }
    private static final Logger logger = LogManager.getLogger(CampaignServiceImpl.class);
    @Override
    public Mono<ResponseEntity<Response>> create(Campaign campaign) {
        logger.info("Campaign Created Successfully");
        return campaignRepository.save(campaign)
                .map(savedCampaign -> new ResponseEntity<>(new Response(200,"Campaign created Successfully",savedCampaign), HttpStatus.OK))
                .onErrorResume(e->Mono.just(new ResponseEntity<>(new Response(500,"Internal Server Error"),HttpStatus.INTERNAL_SERVER_ERROR)));
    }
    @Override
    public Mono<ResponseEntity<Response>> getAll() {
        logger.debug("Attempting to retrieve all campaigns");
        return campaignRepository.findAll()
                .collectList()
                .flatMap(campaignList -> {
                    if (!campaignList.isEmpty()) {
                        logger.info("Retrieved {} campaigns successfully", campaignList.size());
                        return Mono.just(new ResponseEntity<>(new Response(200, "Campaigns retrieved successfully", campaignList), HttpStatus.OK));
                    } else {
                        logger.warn("No campaigns found");
                        return Mono.just(new ResponseEntity<>(new Response(404, "Campaigns not found"), HttpStatus.NOT_FOUND));
                    }
                })
                .switchIfEmpty(Mono.just(new ResponseEntity<>(new Response(404, "Campaigns not found"), HttpStatus.NOT_FOUND)))
                .onErrorResume(e -> {
                    logger.error("Error occurred while retrieving campaigns: {}", e.getMessage());
                    return Mono.just(new ResponseEntity<>(new Response(500, "Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }
    @Override
    public Mono<ResponseEntity<Response>> getOne(int campaignId) {
        logger.debug("Attempting to retrieve campaign with ID: {} ", campaignId);
        return campaignRepository.findByCampaignId(campaignId)
                .map(campaign -> {
                    logger.info("Campaign retrieved successfully: {}", campaign);
                    return new ResponseEntity<>(new Response(200, "Campaign retrieved successfully", campaign), HttpStatus.OK);
                })
                .defaultIfEmpty(new ResponseEntity<>(new Response(404, "Campaign not found"), HttpStatus.NOT_FOUND))
                .onErrorResume(e -> {
                    logger.error("Error occurred while retrieving campaign with ID {}: {}", campaignId, e.getMessage());
                    return Mono.just(new ResponseEntity<>(new Response(500, "Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }
    @Override
    public Mono<ResponseEntity<Response>> update(int campaignId, Campaign updatedCampaign) {
        logger.debug("Updating campaign with ID: {}", campaignId);
        return campaignRepository.findByCampaignId(campaignId)
                .flatMap(existingCampaign -> {
                    // Update the existing campaign with the new details
                    if (updatedCampaign.getCampaignName() != null) {
                        existingCampaign.setCampaignName(updatedCampaign.getCampaignName());
                    }
                    if (updatedCampaign.getCampaignDescription() != null) {
                        existingCampaign.setCampaignDescription(updatedCampaign.getCampaignDescription());
                    }
                    if (updatedCampaign.getStartDate() != null) {
                        existingCampaign.setStartDate(updatedCampaign.getStartDate());
                    }
                    if (updatedCampaign.getEndDate() != null) {
                        existingCampaign.setEndDate(updatedCampaign.getEndDate());
                    }
                    if (updatedCampaign.getMaxAttempts() != null) {
                        existingCampaign.setMaxAttempts(updatedCampaign.getMaxAttempts());
                    }
                    if (updatedCampaign.getPassPercentage() != null) {
                        existingCampaign.setPassPercentage(updatedCampaign.getPassPercentage());
                    }
                    if (updatedCampaign.getShortName() != null) {
                        existingCampaign.setShortName(updatedCampaign.getShortName());
                    }
                    return campaignRepository.save(existingCampaign)
                            .map(updated -> {
                                logger.info("Campaign updated successfully: {}", existingCampaign);
                                return new ResponseEntity<>(new Response(200, "Campaign updated successfully", existingCampaign), HttpStatus.OK);
                            });
                })
                .defaultIfEmpty(new ResponseEntity<>(new Response(404, "Campaign not found"), HttpStatus.NOT_FOUND))
                .onErrorResume(e -> {
                    logger.error("Error occurred while updating campaign with ID {}: {}", campaignId, e.getMessage());
                    return Mono.just(new ResponseEntity<>(new Response(500, "Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }
    @Override
    public Mono<ResponseEntity<Response>> deleteById(int campaignId) {
        logger.debug("Attempting to delete campaign with ID: {}", campaignId);
        return campaignRepository.existsByCampaignId(campaignId)
                .flatMap(exists -> {
                    if (exists) {
                        return campaignRepository.deleteByCampaignId(campaignId)
                                .then(Mono.just(new ResponseEntity<>(new Response(200, "Campaign deleted successfully"), HttpStatus.OK)))
                                .doOnSuccess(success -> logger.info("Campaign with ID {} deleted successfully", campaignId));
                    } else {
                        logger.warn("Campaign with ID {} not found", campaignId);
                        return Mono.just(new ResponseEntity<>(new Response(404, "Campaign not found"), HttpStatus.NOT_FOUND));
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Error occurred while deleting campaign with ID {}: {}", campaignId, e.getMessage());
                    return Mono.just(new ResponseEntity<>(new Response(500, "Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }
    @Override
    public Mono<ResponseEntity<Response>> deleteByName(String campaignName) {
        logger.debug("Attempting to delete campaign by name: {}", campaignName);
        return campaignRepository.existsByCampaignName(campaignName)
                .flatMap(exists -> {
                    if (exists) {
                        return campaignRepository.deleteByCampaignName(campaignName)
                                .then(Mono.just(new ResponseEntity<>(new Response(200, "Campaign deleted successfully"), HttpStatus.OK)))
                                .doOnSuccess(success -> logger.info("Campaign with name '{}' deleted successfully", campaignName));
                    } else {
                        logger.warn("Campaign with name '{}' not found", campaignName);
                        return Mono.just(new ResponseEntity<>(new Response(404, "Campaign not found"), HttpStatus.NOT_FOUND));
                    }
                })
                .defaultIfEmpty(new ResponseEntity<>(new Response(404, "Campaign not found"), HttpStatus.NOT_FOUND))
                .onErrorResume(e -> {
                    logger.error("Error occurred while deleting campaign by name '{}': {}", campaignName, e.getMessage());
                    return Mono.just(new ResponseEntity<>(new Response(500, "Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

}
