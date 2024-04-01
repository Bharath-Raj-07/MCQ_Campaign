package com.bridgelabz.campaign.controller;

import com.bridgelabz.campaign.model.Campaign;
import com.bridgelabz.campaign.service.CampaignServiceImpl;
import com.bridgelabz.campaign.utility.ResponseMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("campaign")
public class CampaignController {

    public final CampaignServiceImpl campaignService;
    public CampaignController(CampaignServiceImpl campaignService) {
        this.campaignService = campaignService;
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<ResponseMessage>> createCampaign(@RequestBody Campaign campaign) {
        return campaignService.createCampaign(campaign);
    }

    @GetMapping("/get")
    public Mono<ResponseEntity<ResponseMessage>> getAllCampaigns() {
        return campaignService.getAllCampaigns();
    }

    @GetMapping("/get/{campaignId}")
    Mono<ResponseEntity<ResponseMessage>>getCampaignById(@PathVariable int campaignId) {
        return campaignService.getCampaign(campaignId);
    }

    @PutMapping("/update/{campaignId}")
    public Mono<ResponseEntity<ResponseMessage>> updateCampaign(@PathVariable int campaignId, @RequestBody Campaign updatedCampaign) {
        return campaignService.update(campaignId, updatedCampaign);
    }

    @DeleteMapping("/delete/campaignId/{campaignId}")
    public Mono<ResponseEntity<ResponseMessage>> deleteCampaignById(@PathVariable int campaignId) {
        return campaignService.deleteById(campaignId);
    }

    @DeleteMapping("/delete/campaignName/{campaignName}")
    public Mono<ResponseEntity<ResponseMessage>> deleteCampaignByName(@PathVariable String campaignName) {
        return campaignService.deleteByName(campaignName);
    }

}
