package com.guavus.aiq.dashboard.config;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("kafka")
@PropertySource("classpath:kafka.properties")
public
class KafkaProperties {
    private String host;
    private String port;
    private String clientId;
    private String groupId;
    private String topicName;
    private String retries;
    private String backoff;
    private Integer retention;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getRetries() {
        return retries;
    }

    public void setRetries(String retries) {
        this.retries = retries;
    }

    public String getBackoff() {
        return backoff;
    }

    public void setBackoff(String backoff) {
        this.backoff = backoff;
    }

    public Integer getRetention() {
        return retention;
    }

    public void setRetention(Integer retention) {
        this.retention = retention;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("host", host)
                .append("port", port)
                .append("clientId", clientId)
                .append("groupId", groupId)
                .append("topicName", topicName)
                .append("retries", retries)
                .append("backoff", backoff)
                .append("retention", retention)
                .toString();
    }
}