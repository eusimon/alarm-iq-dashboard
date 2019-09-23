package com.guavus.aiq.dashboard.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.guavus.aiq.dashboard.enums.AlarmType;
import com.guavus.aiq.dashboard.utils.Validator;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.StringJoiner;

/**
 * BucketModel definition.
 *
 * @author Eugene Simon
 * @since Sep 11th, 2019
 */
public class BucketModel {
    @JsonProperty("timestamp")
    private final Long timestamp;
    @JsonProperty("alarmType")
    private final AlarmType type;
    @JsonProperty("count")
    private final Long count;

    @JsonCreator
    public BucketModel(@JsonProperty("timestamp") Long ts,
                       @JsonProperty("alarmType") String type,
                       @JsonProperty("count") Long count) {
        Validator.assertNotNull(ts, "Bucket timestamp");
        Validator.assertNotNull(type, "Alarm type");
        Validator.assertNotNull(count, "Bucket count");
        this.timestamp = ts;
        this.type = AlarmType.fromUiType(type);
        this.count = count;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public AlarmType getType() {
        return type;
    }

    public Long getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof BucketModel)) return false;

        BucketModel that = (BucketModel) o;

        return new EqualsBuilder()
                .append(getCount(), that.getCount())
                .append(getTimestamp(), that.getTimestamp())
                .append(getType(), that.getType())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getCount())
                .append(getTimestamp())
                .append(getType())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BucketModel.class.getSimpleName() + "[", "]")
                .add("count=" + count)
                .add("timestamp=" + timestamp)
                .add("type=" + type)
                .toString();
    }
}
