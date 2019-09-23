package com.guavus.aiq.dashboard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guavus.aiq.dashboard.enums.AlarmType;
import com.guavus.aiq.dashboard.model.DummyOffset;
import com.guavus.aiq.dashboard.model.Message;
import com.guavus.aiq.dashboard.models.BinModel;
import com.guavus.aiq.dashboard.models.BucketModel;
import com.guavus.aiq.dashboard.services.BucketService;
import com.guavus.aiq.dashboard.services.BucketServiceImpl;
import org.apache.commons.lang3.StringUtils;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {KafkaTestDashboardConfig.class}, properties = {"spring.main.allow-bean-definition-overriding=true"})
public class BucketServiceTests {
    private static final Logger LOG = LoggerFactory.getLogger(BucketServiceTests.class);

    @Value("${kafkaconfig.topic}")
    private String topic;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetBucketStream_valid() {
        // Prepare data
        List<Pair<String, Long>> source = Arrays.asList(Pair.of(AlarmType.ALARM_PREDICTED.getUiType(), 1L),
                                                        Pair.of(AlarmType.ALARM_INGESTED.getUiType(), 2L),
                                                        Pair.of(AlarmType.ALARM_DISCARDED.getUiType(), 3L),
                                                        Pair.of(AlarmType.DUMMY.getUiType(), 4L));
        List<Message> messages = generate(source);
        // Inject and get
        BucketService service = new BucketServiceImpl(generateReceiverFlux(messages));
        // Verify
        StepVerifier.create(service.getBucketsStream().map(BucketModel::getType))
                    .expectNextSequence(messages.stream()
                                                .map(msg -> AlarmType.fromUiType(msg.getAlarmType()))
                                                .collect(Collectors.toList()))
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
        List<Message> messages = generate(source);
        // Inject and get
        BucketService service = new BucketServiceImpl(generateReceiverFlux(messages));

        // Verify
        StepVerifier.create(service.getBucketsStream().map(bucket -> bucket.getType().getUiType()))
                    .expectNext("predicted", "dummy", "discarded", "dummy")
                    .expectComplete()
                    .verify();
    }

    @Test
    public void testGetBins_binSize_25() {
        Integer binSize = 25; int count = 10;
        LocalDateTime ts = LocalDateTime.parse("2019-09-15T10:00:00.000");
        List<Message> messages = generate(ts, 10, count);

        // Inject and get
        BucketService service = new BucketServiceImpl(generateReceiverFlux(messages));
        List<BinModel> bins = service.getBinsStream(binSize).collectList().block();

        // verify non-empty
        assertTrue(bins != null && !bins.isEmpty());
        // verify interval sizes
        bins.forEach(bin -> assertEquals(binSize.longValue(), bin.getInterval().getTo() - bin.getInterval().getFrom()));
        // verify bin map sizes
        assertEquals(12, bins.stream().map(bin -> bin.getBin().size()).reduce(0, Integer::sum).intValue());
        // verify event counts for each event type
        String countsPattern = StringUtils.join(Arrays.asList(3, 3, 2, 2), ",");
        assertEquals(countsPattern, StringUtils.join(getEventCounts(bins, AlarmType.ALARM_DISCARDED), ","));
        assertEquals(countsPattern, StringUtils.join(getEventCounts(bins, AlarmType.ALARM_PREDICTED), ","));
        assertEquals(countsPattern, StringUtils.join(getEventCounts(bins, AlarmType.ALARM_INGESTED), ","));
    }

    @Test
    public void testGetBins_binSize_35() {
        Integer binSize = 35; int count = 10;
        LocalDateTime ts = LocalDateTime.parse("2019-09-15T10:00:00.000");
        List<Message> messages = generate(ts, 10, count);
        // Inject and get
        BucketService service = new BucketServiceImpl(generateReceiverFlux(messages));
        List<BinModel> bins = service.getBinsStream(binSize).collectList().block();

        // verify non-empty
        assertTrue(bins != null && !bins.isEmpty());
        // verify interval sizes
        bins.forEach(bin -> assertEquals(binSize.longValue(), bin.getInterval().getTo() - bin.getInterval().getFrom()));
        // verify bin map sizes
        assertEquals(9, bins.stream().map(bin -> bin.getBin().size()).reduce(0, Integer::sum).intValue());
        // verify event counts for each event type
        String countsPattern = StringUtils.join(Arrays.asList(4, 4, 2), ",");
        assertEquals(countsPattern, StringUtils.join(getEventCounts(bins, AlarmType.ALARM_DISCARDED), ","));
        assertEquals(countsPattern, StringUtils.join(getEventCounts(bins, AlarmType.ALARM_PREDICTED), ","));
        assertEquals(countsPattern, StringUtils.join(getEventCounts(bins, AlarmType.ALARM_INGESTED), ","));
    }

    private String convertToString(Object message) {
        try {
            return mapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

    private List<Message> generate(List<Pair<String, Long>> typesAndCounts) {
        return typesAndCounts.stream()
                             .map(pair -> new Message(Instant.now().getEpochSecond(), pair.getLeft(), pair.getRight()))
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

    private List<Long> getEventCounts(List<BinModel> bins, AlarmType type) {
        return bins.stream()
                   .map(bin -> bin.getBin().get(type.getUiType()))
                   .collect(Collectors.toList());
    }
}
