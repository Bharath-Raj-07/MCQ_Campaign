package com.bridgelabz.campaign.controller;

import com.bridgelabz.campaign.model.Campaign;
import com.bridgelabz.campaign.service.CampaignServiceImpl;

import com.bridgelabz.campaign.utility.Response;
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
    public Mono<ResponseEntity<Response>> createCampaign(@RequestBody Campaign campaign) {
        return campaignService.create(campaign);
    }

    @GetMapping("/get")
    public Mono<ResponseEntity<Response>> getAllCampaigns() {
        return campaignService.getAll();
    }

    @GetMapping("/get/{campaignId}")
    public Mono<ResponseEntity<Response>> getCampaignById(@PathVariable int campaignId) {
        return campaignService.getOne(campaignId);
    }

    @PutMapping("/update/{campaignId}")
    public Mono<ResponseEntity<Response>> updateCampaign(@PathVariable int campaignId, @RequestBody Campaign updatedCampaign) {
        return campaignService.update(campaignId, updatedCampaign);
    }

    @DeleteMapping("/delete/campaignId/{campaignId}")
    public Mono<ResponseEntity<Response>> deleteCampaignById(@PathVariable int campaignId) {
        return campaignService.deleteById(campaignId);
    }

    @DeleteMapping("/delete/campaignName/{campaignName}")
    public Mono<ResponseEntity<Response>> deleteCampaignByName(@PathVariable String campaignName) {
        return campaignService.deleteByName(campaignName);
    }

}
