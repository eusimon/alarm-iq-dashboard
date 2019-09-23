package com.guavus.aiq.dashboard.model;


import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.StringJoiner;

/**
 * Message model definition.
 * Should be used for testing purposes only, since it doesn't validate passed data.
 */
public class Message {
    private Long timestamp;
    private String alarmType;
    private Long count;

    public Message(Long timestamp, String alarmType, Long count) {
        this.timestamp = timestamp;
        this.alarmType = alarmType;
        this.count = count;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public Long getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Message)) return false;

        Message message = (Message) o;

        return new EqualsBuilder()
                .append(getTimestamp(), message.getTimestamp())
                .append(getAlarmType(), message.getAlarmType())
                .append(getCount(), message.getCount())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getTimestamp())
                .append(getAlarmType())
                .append(getCount())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Message.class.getSimpleName() + "[", "]")
                .add("timestamp=" + timestamp)
                .add("alarmType=" + alarmType)
                .add("count=" + count)
                .toString();
    }
}
