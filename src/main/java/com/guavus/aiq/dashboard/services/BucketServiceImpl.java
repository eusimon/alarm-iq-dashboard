package com.guavus.aiq.dashboard.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guavus.aiq.dashboard.enums.AlarmType;
import com.guavus.aiq.dashboard.models.BinModel;
import com.guavus.aiq.dashboard.models.BucketModel;
import com.guavus.aiq.dashboard.models.BucketState;
import com.guavus.aiq.dashboard.models.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverRecord;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bucket service implementation.
 *
 * @author Eugene Simon
 * @since Sep 11th, 2019
 */
@Service
public class BucketServiceImpl implements BucketService {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final Flux<ReceiverRecord<String, String>> bucketReceiver;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public BucketServiceImpl(Flux<ReceiverRecord<String, String>> bucketReceiver) {
        this.bucketReceiver = bucketReceiver;
    }

    public Flux<BucketModel> getBucketsStream() {
        return Flux.from(bucketReceiver)
                   .doOnError(ex -> LOG.error("Kafka error, message: {}", ex.getMessage()))
                   .map(rec -> convert(rec.value()));
    }

    public Flux<BinModel> getBinsStream(int binSize) {
        return Flux.from(bucketReceiver)
                   .doOnError(ex -> LOG.error("Kafka error, message: {}", ex.getMessage()))
                   .map(rec -> convert(rec.value()))
                   .scan(getInitialState(), (state, current) -> getBucketState(state, current, binSize))
                   .filter(state -> AlarmType.getSupportedTypes().contains(state.getBucket().getType()))
                   .bufferUntil(BucketState::hasDelimiter, true)
                   .map(this::toBinModel)
                   .log();
    }

    private BucketModel convert(String payload) {
        try {
            return mapper.readValue(payload, BucketModel.class);
        } catch (IOException ex) {
            LOG.error("Failed to convert payload [{}]. Error: {}", payload, ex.getMessage());
            return new BucketModel(0L, "dummy", 0L);
        }
    }

    private BucketState getInitialState() {
        return new BucketState(new BucketModel(0L, AlarmType.DUMMY.getUiType(), 0L), 0, true, 0L, 0L);
    }

    private BucketState getBucketState(BucketState state, BucketModel current, int binSize) {
        long acc = state.getAccumulator();
        long start = state.getStartTime();
        long end = state.getEndTime();
        if (AlarmType.DUMMY.equals(state.getBucket().getType())) {
            start = current.getTimestamp();
            end = start + binSize;
        }
        if (!AlarmType.DUMMY.equals(state.getBucket().getType())) {
            acc += current.getTimestamp() - state.getBucket().getTimestamp();
        }
        if (acc > binSize && binSize != 0) {
            acc = acc % binSize;
            start = current.getTimestamp() - acc;
            end = start + binSize;
            return new BucketState(current, acc, true, start, end);
        }
        return new BucketState(current, acc, false, start, end);
    }

    private BinModel toBinModel(List<BucketState> buckets) {
        Long min = buckets.stream().mapToLong(BucketState::getStartTime).findFirst().orElse(0);
        Long max = buckets.stream().mapToLong(BucketState::getEndTime).findFirst().orElse(0);
        Map<String, Long> bin = new HashMap<>();
        buckets.forEach(bucket -> bin.merge(bucket.getBucket().getType().getUiType(),
                                            bucket.getBucket().getCount(),
                                            (previous, current) -> previous + current));
        return new BinModel(bin, new Interval(min, max));
    }
}
