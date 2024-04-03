package com.bridgelabz.campaign.service;

import com.bridgelabz.campaign.model.Campaign;
import com.bridgelabz.campaign.model.CampaignDto;
import com.bridgelabz.campaign.repository.CampaignRepository;
import com.bridgelabz.campaign.utility.CampaignMapper;
import com.bridgelabz.campaign.utility.CampaignValidator;
import com.bridgelabz.campaign.utility.ResponseMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@PropertySource("classpath:message.properties")
public class CampaignServiceImpl implements CampaignService{

    private final CampaignRepository campaignRepository;
    private final CampaignValidator campaignValidator;
    private final Environment env;
    private final CampaignMapper campaignMapper;

    @Autowired
    public CampaignServiceImpl(CampaignRepository campaignRepository, CampaignValidator campaignValidator, Environment env, CampaignMapper campaignMapper) {
        this.campaignRepository = campaignRepository;
        this.campaignValidator = campaignValidator;
        this.env = env;
        this.campaignMapper = campaignMapper;
    }
    private static final Logger logger = LogManager.getLogger(CampaignServiceImpl.class);

    @Override
    public Mono<ResponseEntity<ResponseMessage>> createCampaign(CampaignDto campaignDto) {
        logger.debug("Attempting to create a new campaign");
        Campaign campaign = campaignMapper.convertToEntity(campaignDto);
        try {
            campaignValidator.validate(campaign); // Validate general campaign constraints
            campaignValidator.validateCampaignDates(campaign); // Validate campaign dates
            campaignValidator.validateActivityStatus(campaign); // Validate activity status
        } catch (IllegalArgumentException e) {
            int code = HttpStatus.BAD_REQUEST.value();
            String errorMessage = env.getProperty("error.validationFailed") + ": " + e.getMessage();
            logger.warn(errorMessage);
            return Mono.just(new ResponseEntity<>(new ResponseMessage(code, errorMessage, null), HttpStatus.BAD_REQUEST));
        }
        logger.debug("Validation successful. Proceeding to save the campaign.");
        return campaignRepository.save(campaign)
                .flatMap(savedCampaign -> {
                    int code = HttpStatus.OK.value();
                    String message = env.getProperty("success.campaignCreated");
                    HttpStatus status = HttpStatus.OK;
                    logger.info("Campaign created successfully. ID: {}", savedCampaign.getCampaignId());
                    return Mono.just(new ResponseEntity<>(new ResponseMessage(code, message, savedCampaign), status));
                })
                .onErrorResume(e -> {
                    int code = HttpStatus.INTERNAL_SERVER_ERROR.value();
                    String errorMessage = env.getProperty("error.internalServerError");
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
                        code = HttpStatus.OK.value();
                        message = env.getProperty("success.campaignsRetrieved");
                        status = HttpStatus.OK;
                    } else {
                        code = HttpStatus.NOT_FOUND.value();
                        message = env.getProperty("error.campaignsNotFound");
                        status = HttpStatus.NOT_FOUND;
                    }
                    logger.info("All Campaigns successfully retrieved");
                    return Mono.just(new ResponseEntity<>(new ResponseMessage(code, message, campaignList), status));
                })
                .switchIfEmpty(Mono.just(new ResponseEntity<>(new ResponseMessage(HttpStatus.NOT_FOUND.value(), env.getProperty("error.campaignsNotFound"), null), HttpStatus.NOT_FOUND)))
                .onErrorResume(e -> {
                    logger.error("Error occurred while retrieving campaigns", e);
                    int code = HttpStatus.INTERNAL_SERVER_ERROR.value();
                    String errorMessage = env.getProperty("error.internalServerError");
                    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
                    return Mono.just(new ResponseEntity<>(new ResponseMessage(code, errorMessage, null), status));
                });
    }
    @Override
    public Mono<ResponseEntity<ResponseMessage>> getCampaign(int campaignId) {
        logger.debug("Attempting to retrieve campaign with ID: {} ", campaignId);
        return campaignRepository.findByCampaignId(campaignId)
                .map(campaign -> {
                    int code = HttpStatus.OK.value();
                    String message = env.getProperty("success.campaignRetrieved");
                    HttpStatus status = HttpStatus.OK;
                    logger.info("Campaign {} retrieved successfully.", campaignId);
                    return new ResponseEntity<>(new ResponseMessage(code, message, campaign), status);
                })
                .defaultIfEmpty(new ResponseEntity<>(new ResponseMessage(HttpStatus.NOT_FOUND.value(), env.getProperty("error.campaignNotFound"), null), HttpStatus.NOT_FOUND))
                .onErrorResume(e -> {
                    logger.error("Error occurred while retrieving campaign with ID {}: {}", campaignId, e.getMessage());
                    int code = HttpStatus.INTERNAL_SERVER_ERROR.value();
                    String errorMessage = env.getProperty("error.internalServerError");
                    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
                    return Mono.just(new ResponseEntity<>(new ResponseMessage(code, errorMessage, null), status));
                });
    }
    @Override
    public Mono<ResponseEntity<ResponseMessage>> update(int campaignId, CampaignDto updatedCampaignDto) {
        logger.debug("Updating campaign with ID: {}", campaignId);
        Campaign campaign = campaignMapper.convertToEntity(updatedCampaignDto);
        return campaignRepository.findByCampaignId(campaignId)
                .flatMap(existingCampaign -> {
                    // Validate updated campaign
                    try {
                        campaignValidator.validate(campaign); // Validate general campaign constraints
                        campaignValidator.validateCampaignDates(campaign); // Validate campaign dates
                        campaignValidator.validateActivityStatus(campaign); // Validate activity status
                        logger.debug("Campaign validation successful for update");
                    } catch (IllegalArgumentException e) {
                        // Return BAD_REQUEST response if validation fails
                        int code = HttpStatus.BAD_REQUEST.value();
                        String errorMessage = env.getProperty("error.validationFailed") + ": " + e.getMessage();
                        logger.warn(errorMessage);
                        return Mono.just(new ResponseEntity<>(new ResponseMessage(code, errorMessage, null), HttpStatus.BAD_REQUEST));
                    }
                    // Update campaign attributes
                    existingCampaign.setCampaignName(campaign.getCampaignName());
                    existingCampaign.setCampaignDescription(campaign.getCampaignDescription());
                    existingCampaign.setShortName(campaign.getShortName());
                    existingCampaign.setStartDate(campaign.getStartDate());
                    existingCampaign.setEndDate(campaign.getEndDate());
                    existingCampaign.setMaxAttempts(campaign.getMaxAttempts());
                    existingCampaign.setPassPercentage(campaign.getPassPercentage());
                    existingCampaign.setActive(campaign.isActive());
                    existingCampaign.setArchive(campaign.isArchive());
                    // Save the updated campaign

                    return campaignRepository.save(existingCampaign)
                            .map(updated -> {
                                int code = HttpStatus.OK.value();
                                String message = env.getProperty("success.campaignUpdated");
                                HttpStatus status = HttpStatus.OK;
                                logger.info("Campaign updated successfully. ID: {}", existingCampaign.getCampaignId());
                                return new ResponseEntity<>(new ResponseMessage(code, message, existingCampaign), status);
                            });
                })
                .defaultIfEmpty(new ResponseEntity<>(new ResponseMessage(HttpStatus.NOT_FOUND.value(), env.getProperty("error.campaignNotFound"), null), HttpStatus.NOT_FOUND))
                .onErrorResume(e -> {
                    // Return INTERNAL_SERVER_ERROR response if an error occurs
                    int code = HttpStatus.INTERNAL_SERVER_ERROR.value();
                    String errorMessage = env.getProperty("error.internalServerError");
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
                        code = HttpStatus.OK.value();
                        message = env.getProperty("success.campaignDeleted");
                        status = HttpStatus.OK;
                        return campaignRepository.deleteByCampaignId(campaignId)
                                .then(Mono.just(new ResponseEntity<>(new ResponseMessage(code, message, null), status)))
                                .doOnSuccess(success -> logger.info("Campaign with ID {} deleted successfully", campaignId));
                    } else {
                        code = HttpStatus.NOT_FOUND.value();
                        message = env.getProperty("error.campaignNotFound");
                        status = HttpStatus.NOT_FOUND;
                        logger.warn("Campaign with ID {} not found", campaignId);
                        return Mono.just(new ResponseEntity<>(new ResponseMessage(code, message, null), status));
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Error occurred while deleting campaign with ID {}: {}", campaignId, e.getMessage());
                    int code = HttpStatus.INTERNAL_SERVER_ERROR.value();
                    String errorMessage = env.getProperty("error.internalServerError");
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
                        code = HttpStatus.OK.value();
                        message = env.getProperty("success.campaignDeleted");
                        status = HttpStatus.OK;
                        logger.info("Campaign deleted successfully.");
                        return campaignRepository.deleteByCampaignName(campaignName)
                                .then(Mono.just(new ResponseEntity<>(new ResponseMessage(code, message, null), status)))
                                .doOnSuccess(success -> logger.info("Campaign with name '{}' deleted successfully", campaignName));
                    } else {
                        code = HttpStatus.NOT_FOUND.value();
                        message = env.getProperty("error.campaignNotFound");
                        status = HttpStatus.NOT_FOUND;
                        logger.warn("Campaign with name '{}' not found", campaignName);
                        return Mono.just(new ResponseEntity<>(new ResponseMessage(code, message, null), status));
                    }
                })
                .defaultIfEmpty(new ResponseEntity<>(new ResponseMessage(404, env.getProperty("error.campaignNotFound"), null), HttpStatus.NOT_FOUND))
                .onErrorResume(e -> {
                    logger.error("Error occurred while deleting campaign by name '{}': {}", campaignName, e.getMessage());
                    int code = HttpStatus.INTERNAL_SERVER_ERROR.value();
                    String errorMessage = env.getProperty("error.internalServerError");
                    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
                    return Mono.just(new ResponseEntity<>(new ResponseMessage(code, errorMessage, null), status));
                });
    }

    @Scheduled(fixedRate = 3600000) // Run every hour
    public void scheduleCampaignActivation() {
        campaignRepository.findAll()
                .flatMap(campaign -> {
                    try {
                        Instant currentDate = Instant.now();
                        boolean isActive = currentDate.isAfter(campaign.getStartDate()) && currentDate.isBefore(campaign.getEndDate());
                        campaign.setActive(isActive);
                        logger.info("Updated activity status of " + campaign.getCampaignName());
                    } catch (Exception e) {
                        logger.warn("An unexpected error occurred while processing campaign: " + campaign.getCampaignName() + ", Error: " + e.getMessage());
                    }
                    return Mono.just(campaign);
                })
                .collectList()
                .flatMapMany(campaigns -> {
                    // Save all campaigns in one batch
                    return Flux.fromIterable(campaigns)
                            .flatMap(campaignRepository::save)
                            .onErrorResume(error -> {
                                logger.error("Error occurred while saving campaigns: " + error.getMessage());
                                return Mono.empty(); // Return an empty Mono to continue processing other campaigns
                            });
                })
                .subscribe(savedCampaign -> {
                    logger.info("Campaign " + savedCampaign.getCampaignName() + " updated");
                });
    }
}
