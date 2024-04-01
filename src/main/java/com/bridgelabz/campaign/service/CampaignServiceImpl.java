package com.bridgelabz.campaign.service;

import com.bridgelabz.campaign.model.Campaign;
import com.bridgelabz.campaign.repository.CampaignRepository;
import com.bridgelabz.campaign.utility.ResponseMessage;
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
    public Mono<ResponseEntity<ResponseMessage>> create(Campaign campaign) {
        return campaignRepository.save(campaign)
                .flatMap(savedCampaign -> {
                    int code = 200;
                    String message = "Campaign created successfully";
                    HttpStatus status = HttpStatus.OK;
                    return Mono.just(new ResponseEntity<>(new ResponseMessage(code, message, savedCampaign), status));
                })
                .onErrorResume(e -> {
                    int code = 500;
                    String errorMessage = "Internal Server Error";
                    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
                    return Mono.just(new ResponseEntity<>(new ResponseMessage(code, errorMessage, null), status));
                });
    }

    @Override
    public Mono<ResponseEntity<ResponseMessage>> getAllCampaigns() {
        logger.debug("Attempting to retrieve all campaigns");
        return campaignRepository.findAll()
                .collectList()
                .flatMap(campaignList -> {
                    int code;
                    String message;
                    HttpStatus status;
                    if (!campaignList.isEmpty()) {
                        code = 200;
                        message = "Campaigns retrieved successfully";
                        status = HttpStatus.OK;
                    } else {
                        code = 404;
                        message = "Campaigns not found";
                        status = HttpStatus.NOT_FOUND;
                    }
                    logger.info("All Campaigns successfully retrieved");
                    return Mono.just(new ResponseEntity<>(new ResponseMessage(code, message, campaignList), status));
                })
                .switchIfEmpty(Mono.just(new ResponseEntity<>(new ResponseMessage(404, "Campaigns not found", null), HttpStatus.NOT_FOUND)))
                .onErrorResume(e -> {
                    logger.error("Error occurred while retrieving campaigns", e);
                    int code = 500;
                    String errorMessage = "Internal Server Error";
                    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
                    return Mono.just(new ResponseEntity<>(new ResponseMessage(code, errorMessage, null), status));
                });
    }
    @Override
    public Mono<ResponseEntity<ResponseMessage>> getCampaign(int campaignId) {
        logger.debug("Attempting to retrieve campaign with ID: {} ", campaignId);
        return campaignRepository.findByCampaignId(campaignId)
                .map(campaign -> {
                    int code = 200;
                    String message = "Campaign retrieved successfully.";
                    HttpStatus status = HttpStatus.OK;
                    logger.info("Campaign {} retrieved successfully.",campaignId);
                    return new ResponseEntity<>(new ResponseMessage(code, message, campaign), status);
                })
                .defaultIfEmpty(new ResponseEntity<>(new ResponseMessage(404, "Campaign not found", null), HttpStatus.NOT_FOUND))
                .onErrorResume(e -> {
                    int code = 500;
                    String errorMessage = "Internal Server Error";
                    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
                    logger.error("Error occurred while retrieving campaign with ID {}: {}",campaignId,e.getMessage());
                    return Mono.just(new ResponseEntity<>(new ResponseMessage(code, errorMessage, null), status));
                });
    }
    @Override
    public Mono<ResponseEntity<ResponseMessage>> update(int campaignId, Campaign updatedCampaign) {
        logger.debug("Updating campaign with ID: {}", campaignId);
        return campaignRepository.findByCampaignId(campaignId)
                .flatMap(existingCampaign -> {
                    if (updatedCampaign.getCampaignName() != null) {
                        existingCampaign.setCampaignName(updatedCampaign.getCampaignName());
                    }
                    if (updatedCampaign.getCampaignDescription() != null) {
                        existingCampaign.setCampaignDescription(updatedCampaign.getCampaignDescription());
                    }
                    if (updatedCampaign.getShortName() != null) {
                        existingCampaign.setShortName(updatedCampaign.getShortName());
                    }
                    if (updatedCampaign.getStartDate() != null && updatedCampaign.getStartDate().isAfter(existingCampaign.getStartDate())) {
                        existingCampaign.setStartDate(updatedCampaign.getStartDate());
                    }
                    if (updatedCampaign.getEndDate() != null && updatedCampaign.getEndDate().isAfter(existingCampaign.getEndDate())) {
                        existingCampaign.setEndDate(updatedCampaign.getEndDate());
                    }
                    if (updatedCampaign.getMaxAttempts() != null) {
                        existingCampaign.setMaxAttempts(updatedCampaign.getMaxAttempts());
                    }
                    if (updatedCampaign.getPassPercentage() != null) {
                        existingCampaign.setPassPercentage(updatedCampaign.getPassPercentage());
                    }
                    existingCampaign.setActive(updatedCampaign.isActive());
                    existingCampaign.setArchive(updatedCampaign.isArchive());

                    return campaignRepository.save(existingCampaign)
                            .map(updated -> {
                                int code = 200;
                                String message = "Campaign updated successfully.";
                                HttpStatus status = HttpStatus.OK;
                                logger.info("Campaign updated successfully.");
                                return new ResponseEntity<>(new ResponseMessage(code, message, existingCampaign), status);
                            });
                })
                .defaultIfEmpty(new ResponseEntity<>(new ResponseMessage(404, "Campaign not found", null), HttpStatus.NOT_FOUND))
                .onErrorResume(e -> {
                    int code = 500;
                    String errorMessage = "Internal Server Error";
                    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
                    logger.error("Error occurred while updating campaign with ID {}: {}",campaignId,e.getMessage());
                    return Mono.just(new ResponseEntity<>(new ResponseMessage(code, errorMessage, null), status));
                });
    }
    @Override
    public Mono<ResponseEntity<ResponseMessage>> deleteById(int campaignId) {
        logger.debug("Attempting to delete campaign with ID: {}", campaignId);
        return campaignRepository.existsByCampaignId(campaignId)
                .flatMap(exists -> {
                    int code;
                    String message;
                    HttpStatus status;
                    if (exists) {
                        code = 200;
                        message = "Campaign deleted successfully.";
                        status = HttpStatus.OK;
                        return campaignRepository.deleteByCampaignId(campaignId)
                                .then(Mono.just(new ResponseEntity<>(new ResponseMessage(code, message, null), status)))
                                .doOnSuccess(success -> logger.info("Campaign with ID {} deleted successfully", campaignId));
                    } else {
                        code = 404;
                        message = "Campaign not found";
                        status = HttpStatus.NOT_FOUND;
                        logger.warn("Campaign with ID {} not found", campaignId);
                        return Mono.just(new ResponseEntity<>(new ResponseMessage(code, message, null), status));
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Error occurred while deleting campaign with ID {}: {}", campaignId, e.getMessage());
                    int code = 500;
                    String errorMessage = "Internal Server Error";
                    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
                    return Mono.just(new ResponseEntity<>(new ResponseMessage(code, errorMessage, null), status));
                });
    }

    @Override
    public Mono<ResponseEntity<ResponseMessage>> deleteByName(String campaignName) {
        logger.debug("Attempting to delete campaign by name: {}", campaignName);
        return campaignRepository.existsByCampaignName(campaignName)
                .flatMap(exists -> {
                    int code;
                    String message;
                    HttpStatus status;
                    if (exists) {
                        code = 200;
                        message = "Campaign deleted successfully.";
                        status = HttpStatus.OK;
                        logger.info("Campaign deleted successfully.");
                        return campaignRepository.deleteByCampaignName(campaignName)
                                .then(Mono.just(new ResponseEntity<>(new ResponseMessage(code, message, null), status)))
                                .doOnSuccess(success -> logger.info("Campaign with name '{}' deleted successfully", campaignName));
                    } else {
                        code = 404;
                        message = "Campaign not found.";
                        status = HttpStatus.NOT_FOUND;
                        logger.warn("Campaign with name '{}' not found", campaignName);
                        return Mono.just(new ResponseEntity<>(new ResponseMessage(code, message, null), status));
                    }
                })
                .defaultIfEmpty(new ResponseEntity<>(new ResponseMessage(404, "Campaign not found", null), HttpStatus.NOT_FOUND))
                .onErrorResume(e -> {
                    logger.error("Error occurred while deleting campaign by name '{}': {}", campaignName, e.getMessage());
                    int code = 500;
                    String errorMessage = "Internal Server Error";
                    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
                    return Mono.just(new ResponseEntity<>(new ResponseMessage(code, errorMessage, null), status));
                });
    }

}
