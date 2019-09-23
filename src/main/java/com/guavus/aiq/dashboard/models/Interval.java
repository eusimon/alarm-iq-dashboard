package com.guavus.aiq.dashboard.models;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Interval model definition.
 *
 * @author Eugene Simon
 * @since Sep 11th, 2019
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"fromDate", "toDate" })
public class Interval {

    @JsonIgnore
    private LocalDateTime start;
    @JsonIgnore
    private LocalDateTime end;

    @JsonCreator
    public Interval(@JsonProperty("fromDate") Long start,
                    @JsonProperty("toDate") Long end) {
        this.start = LocalDateTime.ofInstant(Instant.ofEpochSecond(start), ZoneOffset.UTC);
        this.end = LocalDateTime.ofInstant(Instant.ofEpochSecond(end), ZoneOffset.UTC);
    }

    public Interval(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    @JsonIgnore
    public LocalDateTime getStart() {
        return start;
    }

    @JsonProperty("fromDate")
    public Long getFrom() {
        return start.toInstant(ZoneOffset.UTC).getEpochSecond();
    }

    @JsonIgnore
    public LocalDateTime getEnd() {
        return end;
    }

    @JsonProperty("toDate")
    public Long getTo() {
        return end.toInstant(ZoneOffset.UTC).getEpochSecond();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Interval)) return false;

        Interval interval = (Interval) o;

        return new EqualsBuilder()
                .append(getStart(), interval.getStart())
                .append(getEnd(), interval.getEnd())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getStart())
                .append(getEnd())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("start", start)
                .append("end", end)
                .toString();
    }
}
