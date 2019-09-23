package com.guavus.aiq.dashboard.controllers;

import com.guavus.aiq.dashboard.models.BinModel;
import com.guavus.aiq.dashboard.models.BucketModel;
import com.guavus.aiq.dashboard.services.BucketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Bucket controller implementation.
 *
 * @author Eugene Simon
 * @since Sep 11th, 2019
 */
@RestController
public class BucketController {
    private static final Logger LOG = LoggerFactory.getLogger(BucketController.class.getSimpleName());

    private final BucketService bucketService;

    @Autowired
    public BucketController(BucketService bucketService) {
        this.bucketService = bucketService;
    }

    @GetMapping(value = "buckets", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BucketModel> subscribeToBuckets() {
        LOG.info("Subscribing to buckets stream ...");
        return bucketService.getBucketsStream();
    }

    // TODO: convert to post request, pass bin size and interval in a JSON, validate
    @GetMapping(value = "bins", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BinModel> subscribeToBins() {
        LOG.info("Subscribing to bins stream ...");
        return bucketService.getBinsStream(30);
    }

    // TODO: implement data availability
    // TODO: integrate OAuth
    // TODO: add health/version endpoints
    // TODO: configure swagger
    // TODO: wrap into a container
    // TODO: externalize configuration (K8s configmap or git-based config server)
    // TODO: integrate metrics
}
