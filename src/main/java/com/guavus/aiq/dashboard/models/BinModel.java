package com.guavus.aiq.dashboard.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.guavus.aiq.dashboard.utils.Validator;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;

/**
 * BinModel definition.
 *
 * @author Eugene Simon
 * @since Sep 11th, 2019
 */
public class BinModel {

    @JsonProperty("bin")
    @JsonPropertyDescription(value = "Alarm event counts by type")
    private final Map<String, Long> bin;

    @JsonProperty("interval")
    @JsonPropertyDescription(value = "Bin interval (size)")
    private final Interval interval;

    public BinModel(Map<String, Long> values, Interval interval) {
        Validator.assertNotNull(interval, "interval");
        this.bin = values;
        this.interval = interval;
    }

    public Map<String, Long> getBin() {
        return bin;
    }

    public Interval getInterval() {
        return interval;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof BinModel)) return false;

        BinModel binModel = (BinModel) o;

        return new EqualsBuilder()
                .append(getBin(), binModel.getBin())
                .append(getInterval(), binModel.getInterval())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getBin())
                .append(getInterval())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("bin", bin)
                .append("interval", interval)
                .toString();
    }
}
