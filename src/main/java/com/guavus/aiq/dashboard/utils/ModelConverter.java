package com.guavus.aiq.dashboard.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guavus.aiq.dashboard.models.BucketModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class ModelConverter {
    private static final Logger LOG = LoggerFactory.getLogger(ModelConverter.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private ModelConverter() {}

    public static BucketModel convertToBucket(String payload) {
        try {
            return mapper.readValue(payload, BucketModel.class);
        } catch (IOException ex) {
            LOG.error("Failed to convert payload [{}]. Error: {}", payload, ex.getMessage());
            return new BucketModel(0L, "dummy", 0L);
        }
    }
}
