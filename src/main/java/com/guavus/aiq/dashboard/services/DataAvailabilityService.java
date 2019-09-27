package com.guavus.aiq.dashboard.services;


import com.guavus.aiq.dashboard.models.BucketModel;
import com.guavus.aiq.dashboard.models.Interval;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Data availability interface definition.
 *
 * @author Eugene Simon
 * @since June 23th, 2019
 */
public interface DataAvailabilityService {

    /**
     * Returns a stream of bucket data availability intervals from the shared {@link BucketModel} flux.
     * Interval emissions are triggered as new bucket events arrive.
     * The interval is built by combining timestamps of the 1st and the most recent events.
     *
     * @return a stream of data availability intervals
     */
    Flux<Interval> getBucketDataAvailability();
}
