package com.bridgelabz.campaign.service;

import com.bridgelabz.campaign.model.Campaign;
import com.bridgelabz.campaign.repository.CampaignRepository;
import com.bridgelabz.campaign.utility.CampaignValidator;
import com.bridgelabz.campaign.utility.ResponseMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CampaignServiceImpl implements CampaignService{

    private final CampaignRepository campaignRepository;
    private final CampaignValidator campaignValidator;
    @Autowired
    public CampaignServiceImpl(CampaignRepository campaignRepository, CampaignValidator campaignValidator) {
        this.campaignRepository = campaignRepository;
        this.campaignValidator = campaignValidator;
    }

    private static final Logger logger = LogManager.getLogger(CampaignServiceImpl.class);

    @Override
    public Mono<ResponseEntity<ResponseMessage>> createCampaign(Campaign campaign) {
        logger.debug("Attempting to create a new campaign");
        try {
            campaignValidator.validate(campaign); // Validate general campaign constraints
            campaignValidator.validateCampaignDates(campaign); // Validate campaign dates
        } catch (IllegalArgumentException e) {
            int code = HttpStatus.BAD_REQUEST.value();
            String errorMessage = "Validation failed: " + e.getMessage();
            logger.warn(errorMessage);
            return Mono.just(new ResponseEntity<>(new ResponseMessage(code, errorMessage, null), HttpStatus.BAD_REQUEST));
        }
        logger.debug("Validation successful. Proceeding to save the campaign.");
        return campaignRepository.save(campaign)
                .flatMap(savedCampaign -> {
                    int code = HttpStatus.OK.value();
                    String message = "Campaign created successfully";
                    HttpStatus status = HttpStatus.OK;
                    logger.info("Campaign created successfully. ID: {}", savedCampaign.getCampaignId());
                    return Mono.just(new ResponseEntity<>(new ResponseMessage(code, message, savedCampaign), status));
                })
                .onErrorResume(e -> {
                    int code = HttpStatus.INTERNAL_SERVER_ERROR.value();
                    String errorMessage = "Internal Server Error";
                    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
                    logger.error(errorMessage, e);
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
                    // Validate updated campaign
                    try {
                        campaignValidator.validate(updatedCampaign); // Validate general campaign constraints
                        campaignValidator.validateCampaignDates(updatedCampaign); // Validate campaign dates
                        logger.debug("Campaign validation successful for update");
                    } catch (IllegalArgumentException e) {
                        // Return BAD_REQUEST response if validation fails
                        int code = HttpStatus.BAD_REQUEST.value();
                        String errorMessage = "Validation failed: " + e.getMessage();
                        logger.warn(errorMessage);
                        return Mono.just(new ResponseEntity<>(new ResponseMessage(code, errorMessage, null), HttpStatus.BAD_REQUEST));
                    }

                    // Update campaign attributes
                    existingCampaign.setCampaignName(updatedCampaign.getCampaignName());
                    existingCampaign.setCampaignDescription(updatedCampaign.getCampaignDescription());
                    existingCampaign.setShortName(updatedCampaign.getShortName());
                    if (updatedCampaign.getStartDate().isAfter(existingCampaign.getStartDate())) {
                        existingCampaign.setStartDate(updatedCampaign.getStartDate());
                    }
                    if (updatedCampaign.getEndDate().isAfter(existingCampaign.getEndDate())) {
                        existingCampaign.setEndDate(updatedCampaign.getEndDate());
                    }
                    existingCampaign.setMaxAttempts(updatedCampaign.getMaxAttempts());
                    existingCampaign.setPassPercentage(updatedCampaign.getPassPercentage());
                    existingCampaign.setActive(updatedCampaign.isActive());
                    existingCampaign.setArchive(updatedCampaign.isArchive());

                    // Save the updated campaign
                    return campaignRepository.save(existingCampaign)
                            .map(updated -> {
                                int code = HttpStatus.OK.value();
                                String message = "Campaign updated successfully.";
                                HttpStatus status = HttpStatus.OK;
                                logger.info("Campaign updated successfully. ID: {}", existingCampaign.getCampaignId());
                                return new ResponseEntity<>(new ResponseMessage(code, message, existingCampaign), status);
                            });
                })
                .defaultIfEmpty(new ResponseEntity<>(new ResponseMessage(404, "Campaign not found", null), HttpStatus.NOT_FOUND))
                .onErrorResume(e -> {
                    // Return INTERNAL_SERVER_ERROR response if an error occurs
                    int code = HttpStatus.INTERNAL_SERVER_ERROR.value();
                    String errorMessage = "Internal Server Error";
                    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
                    logger.error("Error occurred while updating campaign with ID {}: {}", campaignId, e.getMessage());
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
