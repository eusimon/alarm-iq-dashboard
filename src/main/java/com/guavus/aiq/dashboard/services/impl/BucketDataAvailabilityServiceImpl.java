package com.guavus.aiq.dashboard.services.impl;

import com.guavus.aiq.dashboard.models.Interval;
import com.guavus.aiq.dashboard.services.DataAvailabilityService;
import com.guavus.aiq.dashboard.utils.ModelConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverRecord;

import java.time.Duration;

/**
 * Data availability service implementation.
 *
 * @author Eugene Simon
 * @since Sep 11th, 2019
 */
@Service
public class BucketDataAvailabilityServiceImpl implements DataAvailabilityService {
    private static final Logger LOG = LoggerFactory.getLogger(BucketDataAvailabilityServiceImpl.class);
    private final Flux<ReceiverRecord<String, String>> bucketReceiver;

    @Autowired
    public BucketDataAvailabilityServiceImpl(Flux<ReceiverRecord<String, String>> bucketReceiver) {
        this.bucketReceiver = bucketReceiver;
    }

    @Override
    public Flux<Interval> getBucketDataAvailability() {
        Flux<Long> startTimestamps = getStartTimestamp();
        Flux<Long> endTimestamps = Flux.from(bucketReceiver)
                                    .map(rec -> ModelConverter.convertToBucket(rec.value()).getTimestamp());
        return Flux.combineLatest(startTimestamps, endTimestamps, Interval::new)
                   .distinctUntilChanged();     // filters out similar items, if any
    }

    /**
     * Creates a flux that periodically emits a timestamp of the oldest cached record
     * available in the shared kafka receiver flux.
     *
     * @return flux of oldest record's timestamps
     */
    private Flux<Long> getStartTimestamp() {
        return Flux.interval(Duration.ofSeconds(1))
                   .map(pulse -> Flux.from(bucketReceiver)
                                     .map(rec -> ModelConverter.convertToBucket(rec.value()).getTimestamp())
                                     .take(1))
                   .flatMap(item -> item)
                   .log();
    }
}
