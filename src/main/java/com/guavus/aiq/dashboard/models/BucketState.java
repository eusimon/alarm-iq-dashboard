package com.guavus.aiq.dashboard.models;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * BucketState model definition.
 * Wraps immutable BucketModel with mutable state fields used to group buckets events into bins of specified size.
 *
 * @author Eugene Simon
 * @since Sep 11th, 2019
 */
public class BucketState {
    private final BucketModel bucket;
    private long accumulator;
    private boolean delimiter;
    private Long startTime;
    private Long endTime;

    public BucketState(BucketModel bucket, long acc, boolean delim, Long start, Long end) {
        this.bucket = bucket;
        this.accumulator = acc;
        this.delimiter = delim;
        this.startTime = start;
        this.endTime = end;
    }

    public BucketModel getBucket() {
        return bucket;
    }

    public long getAccumulator() {
        return accumulator;
    }

    public boolean hasDelimiter() {
        return delimiter;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof BucketState)) return false;

        BucketState that = (BucketState) o;

        return new EqualsBuilder()
                .append(getAccumulator(), that.getAccumulator())
                .append(hasDelimiter(), that.hasDelimiter())
                .append(getBucket(), that.getBucket())
                .append(getStartTime(), that.getStartTime())
                .append(getEndTime(), that.getEndTime())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getBucket())
                .append(getAccumulator())
                .append(hasDelimiter())
                .append(getStartTime())
                .append(getEndTime())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("bucket", bucket)
                .append("accumulator", accumulator)
                .append("delimiter", delimiter)
                .append("startTime", startTime)
                .append("endTime", endTime)
                .toString();
    }
}
