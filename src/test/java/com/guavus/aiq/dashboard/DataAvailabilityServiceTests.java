package com.guavus.aiq.dashboard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guavus.aiq.dashboard.enums.AlarmType;
import com.guavus.aiq.dashboard.model.DummyOffset;
import com.guavus.aiq.dashboard.model.Message;
import com.guavus.aiq.dashboard.models.BinModel;
import com.guavus.aiq.dashboard.models.BucketModel;
import com.guavus.aiq.dashboard.models.Interval;
import com.guavus.aiq.dashboard.services.BucketService;
import com.guavus.aiq.dashboard.services.DataAvailabilityService;
import com.guavus.aiq.dashboard.services.impl.BucketDataAvailabilityServiceImpl;
import com.guavus.aiq.dashboard.services.impl.BucketServiceImpl;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
public class DataAvailabilityServiceTests {
    private static final Logger LOG = LoggerFactory.getLogger(DataAvailabilityServiceTests.class);

    @Value("${kafkaconfig.topic}")
    private String topic;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /*
    - generate receiver flux with duplicates
    - inject into the service
    - verify accumulated size
    - verify intervals

     */


    @Test
    public void testDataAvailabilityStream() {
        // Prepare data
        LocalDateTime now = LocalDateTime.now();
        List<LocalDateTime> units = Stream.of(300, 250, 200, 200, 150, 150, 100)
                                          .map(now::minusSeconds)
                                          .collect(Collectors.toList());

        List<Message> messages = generate(units);
        // Inject and get
        DataAvailabilityService service = new BucketDataAvailabilityServiceImpl(generateReceiverFlux(messages));
        // Verify
        List<Interval> expected = units.stream()
                                       .map(ts -> new Interval(now.minusSeconds(300), ts))
                                       .collect(Collectors.toList());
        StepVerifier.create(service.getBucketDataAvailability())
                    .expectNextSequence(expected)
                    .expectComplete()
                    .verify();
    }

    @Test
    public void testGetBucketStream_wrongAlarmType() {
        // Prepare data
        List<Pair<String, Long>> source = Arrays.asList(Pair.of(AlarmType.ALARM_PREDICTED.getUiType(), 1L),
                                                        Pair.of("wrong-type", 2L),
                                                        Pair.of(AlarmType.ALARM_DISCARDED.getUiType(), 3L),
                                                        Pair.of(AlarmType.DUMMY.getUiType(), 4L));
        List<Message> messages = generateMessages(source);
        // Inject and get
        BucketService service = new BucketServiceImpl(generateReceiverFlux(messages));

        // Verify
        StepVerifier.create(service.getBucketsStream().map(bucket -> bucket.getType().getUiType()))
                    .expectNext("predicted", "dummy", "discarded", "dummy")
                    .expectComplete()
                    .verify();
    }


    private String convertToString(Object message) {
        try {
            return mapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

    private List<Message> generateMessages(List<Pair<String, Long>> typesAndCounts) {
        return typesAndCounts.stream()
                             .map(pair -> new Message(Instant.now().getEpochSecond(), pair.getLeft(), pair.getRight()))
                             .collect(Collectors.toList());
    }

    private List<Message> generate(List<LocalDateTime> units) {
        return units.stream()
                    .map(unit -> new Message(unit.toInstant(ZoneOffset.UTC).getEpochSecond(), AlarmType.ALARM_PREDICTED.getUiType(), 1L))
                             .collect(Collectors.toList());
    }

    private List<Message> generate(LocalDateTime start, int increment, int count) {
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            LocalDateTime ts = start.plusSeconds(i * increment);
            List<Message> msgs = Arrays.stream(AlarmType.values())
                                       .filter(type -> !AlarmType.DUMMY.equals(type))
                                       .map(type -> new Message(ts.toEpochSecond(ZoneOffset.UTC), type.getUiType(), 1L))
                                       .collect(Collectors.toList());
            messages.addAll(msgs);
        }
        return messages;
    }

    private Flux<ReceiverRecord<String, String>> generateReceiverFlux(List<Message> messages) {
        // Prepare data
        List<ConsumerRecord<String, String>> records = messages.stream()
                                                               .map(msg -> new ConsumerRecord<>(topic, 1, 1L, msg.getTimestamp().toString(), convertToString(msg)))
                                                               .collect(Collectors.toList());
        return Flux.fromStream(records.stream()
                                      .map(rec -> new ReceiverRecord<>(rec, new DummyOffset()))
                                      .collect(Collectors.toList()).stream())
                   .log();
    }
}
