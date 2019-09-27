package com.guavus.aiq.dashboard.config;

import com.guavus.aiq.dashboard.utils.ModelConverter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverPartition;
import reactor.kafka.receiver.ReceiverRecord;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumerConfig.class.getSimpleName());

    @Autowired
    private KafkaProperties kafkaProps;

    /**
     * Creates and returns a {@link KafkaReceiver} with specified {@link ReceiverOptions}.
     *
     * @param options kafka receiver options
     * @return configured {@link KafkaReceiver}
     */
    @Bean
    public KafkaReceiver<String, String> kafkaReceiver(ReceiverOptions<String, String> options) {
        return KafkaReceiver.create(options);
    }

    /**
     * AIQ buckets {@link ReceiverOptions} configuration.
     * Initializes receiver options from the consumer configuration properties map.
     * Upon application restart, the receiver is configured to consume records from the earliest offset in a partition.
     *
     * @return {@link ReceiverOptions} configuration
     */
    @Bean
    public ReceiverOptions<String, String> receiverOptions() {
        ReceiverOptions<String, String> options = ReceiverOptions.create(consumerConfig());
        return options.addAssignListener(partitions -> partitions.forEach(ReceiverPartition::seekToBeginning))
                      .subscription(Collections.singleton(kafkaProps.getTopicName()));
    }

    @Bean
    public Map<String, Object> consumerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProps.getHost() + ":" + kafkaProps.getPort());
        config.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaProps.getClientId());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProps.getGroupId());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return config;
    }

    /**
     * Builds and returns a shared flux (stream) of bucket events.
     * The events are cached for configurable time interval, so that any subsequent subscriber receives
     * the events accumulated during the configured retention period.
     *
     * @param receiver configured {@link KafkaReceiver}
     * @return a shared cached flux of raw bucket events
     */
    @Bean
    public Flux<ReceiverRecord<String, String>> bucketReceiver(KafkaReceiver<String, String> receiver) {
        return receiver.receive()
                       .retryBackoff(Integer.parseInt(kafkaProps.getRetries()),
                                     Duration.ofSeconds(Integer.parseInt(kafkaProps.getBackoff())))
                       .doOnNext(msg -> msg.receiverOffset().acknowledge())
                       .share()
                       .cache(Duration.ofSeconds(60L));
    }
}