package com.guavus.aiq.dashboard.controllers;

import com.guavus.aiq.dashboard.models.Interval;
import com.guavus.aiq.dashboard.services.DataAvailabilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Bucket controller implementation.
 *
 * @author Eugene Simon
 * @since Sep 11th, 2019
 */
@RestController
public class DataAvailabilityController {
    private static final Logger LOG = LoggerFactory.getLogger(DataAvailabilityController.class.getSimpleName());

    private final DataAvailabilityService availabilityService;

    @Autowired
    public DataAvailabilityController(DataAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping(value = "availability", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Interval> subscribeToBuckets() {
        LOG.info("Subscribing to bucket data availability stream ...");
        return availabilityService.getBucketDataAvailability();
    }

    // TODO: implement data availability
    // TODO: integrate OAuth
    // TODO: add health/version endpoints
    // TODO: configure swagger
    // TODO: wrap into a container
    // TODO: externalize configuration (K8s configmap or git-based config server)
    // TODO: integrate metrics
}
