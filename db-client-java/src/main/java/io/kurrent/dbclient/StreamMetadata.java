package io.kurrent.dbclient;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Map;
import java.util.Objects;

/**
 * Represents stream metadata with strongly typed properties for system values and a dictionary-like interface for
 * custom values.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StreamMetadata {
    @JsonProperty("$maxAge")
    private Long maxAge;

    @JsonProperty("$tb")
    private Long truncateBefore;

    @JsonProperty("$cacheControl")
    private Long cacheControl;

    @JsonProperty("$acl")
    @JsonSerialize(using = CustomAclCodec.Serializer.class)
    @JsonDeserialize(using = CustomAclCodec.Deserializer.class)
    private Acl acl;

    @JsonProperty("$maxCount")
    private Long maxCount;

    @JsonAnySetter
    private Map<String, Object> customProperties;

    /**
     * The maximum age of events allowed in the stream.
     */
    public Long getMaxAge() {
        return maxAge;
    }

    /**
     * The maximum age of events allowed in the stream.
     */
    public void setMaxAge(Long maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * The event number from which previous events can be scavenged. This is used to implement deletion of
     * streams.
     */
    public Long getTruncateBefore() {
        return truncateBefore;
    }

    /**
     * The event number from which previous events can be scavenged. This is used to implement deletion of
     * streams.
     */
    public void setTruncateBefore(Long truncateBefore) {
        this.truncateBefore = truncateBefore;
    }

    /**
     * The amount of time for which the stream head is cacheable (in seconds).
     */
    public Long getCacheControl() {
        return cacheControl;
    }

    /**
     * The amount of time for which the stream head is cacheable (in seconds).
     */
    public void setCacheControl(Long cacheControl) {
        this.cacheControl = cacheControl;
    }

    /**
     * The Access Control List of the stream (ACL).
     */
    public Acl getAcl() {
        return acl;
    }

    /**
     * The Access Control List of the stream (ACL).
     */
    public void setAcl(Acl acl) {
        this.acl = acl;
    }

    /**
     * The maximum number of events allowed in the stream.
     */
    public Long getMaxCount() {
        return maxCount;
    }

    /**
     * The maximum number of events allowed in the stream.
     */
    public void setMaxCount(Long maxCount) {
        this.maxCount = maxCount;
    }

    /**
     * An enumerable of key-value pairs of keys to JSON text for user-provider metadata.
     */
    @JsonAnyGetter
    public Map<String, Object> getCustomProperties() {
        return customProperties;
    }

    /**
     * An enumerable of key-value pairs of keys to JSON text for user-provider metadata.
     */
    public void setCustomProperties(Map<String, Object> customProperties) {
        this.customProperties = customProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StreamMetadata that = (StreamMetadata) o;
        return Objects.equals(maxAge, that.maxAge) && Objects.equals(truncateBefore, that.truncateBefore) && Objects.equals(cacheControl, that.cacheControl) && Objects.equals(acl, that.acl) && Objects.equals(maxCount, that.maxCount) && Objects.equals(customProperties, that.customProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxAge, truncateBefore, cacheControl, acl, maxCount, customProperties);
    }

    @Override
    public String toString() {
        return "StreamMetadata{" +
                "maxAge=" + maxAge +
                ", truncateBefore=" + truncateBefore +
                ", cacheControl=" + cacheControl +
                ", acl=" + acl +
                ", maxCount=" + maxCount +
                ", customProperties=" + customProperties +
                '}';
    }
}
