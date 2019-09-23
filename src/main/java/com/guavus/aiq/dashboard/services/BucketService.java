package com.guavus.aiq.dashboard.services;


import com.guavus.aiq.dashboard.models.BinModel;
import com.guavus.aiq.dashboard.models.BucketModel;
import reactor.core.publisher.Flux;

/**
 * Ria tuples repository interface definition.
 *
 * @author Eugene Simon
 * @since June 19th, 2019
 */
public interface BucketService {

    /**
     * Returns a flux (live stream) of bucket events mapped to {@link BucketModel}s from the shared hot flux
     * of kafka receiver records.
     * The flux concatenates cached and live events streams returning all events currently available in the kafka topic.
     *
     * Returned events format: data:{"count":4,"timestamp":1568396941,"alarmType":"ingested"}
     * @return a flux of bucket events
     */
    Flux<BucketModel> getBucketsStream();

    /**
     * It is assumed that the stream doesn't have interval "gaps".
     * The gaps, if any should be represented by zero counts for corresponding AlarmType(s).
     * TODO: gracefully handle errors in the pipeline, ensure only-once-delivery
     *
     * @param binSize desired bin size
     */
    Flux<BinModel> getBinsStream(int binSize);
}
