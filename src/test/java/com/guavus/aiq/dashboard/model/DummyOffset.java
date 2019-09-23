package com.guavus.aiq.dashboard.model;


import org.apache.kafka.common.TopicPartition;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOffset;

public class DummyOffset implements ReceiverOffset {

    @Override
    public TopicPartition topicPartition() {
        return null;
    }

    @Override
    public long offset() {
        return 0;
    }

    @Override
    public void acknowledge() {

    }

    @Override
    public Mono<Void> commit() {
        return null;
    }
}