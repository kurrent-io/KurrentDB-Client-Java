package io.kurrent.dbclient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.*;

/**
 * Stream-related access control list (ACL).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StreamAcl implements Acl {
    @JsonProperty("$r")
    @JsonSerialize(using = CustomAclCodec.ListSerializer.class)
    @JsonDeserialize(using = CustomAclCodec.ListDeserializer.class)
    private List<String> readRoles;

    @JsonProperty("$w")
    @JsonSerialize(using = CustomAclCodec.ListSerializer.class)
    @JsonDeserialize(using = CustomAclCodec.ListDeserializer.class)
    private List<String> writeRoles;

    @JsonProperty("$d")
    @JsonSerialize(using = CustomAclCodec.ListSerializer.class)
    @JsonDeserialize(using = CustomAclCodec.ListDeserializer.class)
    private List<String> deleteRoles;

    @JsonProperty("$mr")
    @JsonSerialize(using = CustomAclCodec.ListSerializer.class)
    @JsonDeserialize(using = CustomAclCodec.ListDeserializer.class)
    private List<String> metaReadRoles;

    @JsonProperty("$mw")
    @JsonSerialize(using = CustomAclCodec.ListSerializer.class)
    @JsonDeserialize(using = CustomAclCodec.ListDeserializer.class)
    private List<String> metaWriteRoles;

    /**
     * Adds read roles.
     * @param roles
     */
    public StreamAcl addReadRoles(String... roles) {
        this.readRoles = this.readRoles == null ? new ArrayList<>() : this.readRoles;
        this.readRoles.addAll(Arrays.asList(roles));

        return this;
    }

    /**
     * Adds write roles.
     * @param roles
     */
    public StreamAcl addWriteRoles(String... roles) {
        this.writeRoles = this.writeRoles == null ? new ArrayList<>() : this.writeRoles;
        this.writeRoles.addAll(Arrays.asList(roles));

        return this;
    }

    /**
     * Adds delete roles.
     * @param roles
     */
    public StreamAcl addDeleteRoles(String... roles) {
        this.deleteRoles = this.deleteRoles == null ? new ArrayList<>() : this.deleteRoles;
        this.deleteRoles.addAll(Arrays.asList(roles));

        return this;
    }

    /**
     * Adds metadata read roles.
     * @param roles
     */
    public StreamAcl addMetaReadRoles(String... roles) {
        this.metaReadRoles = this.metaReadRoles == null ? new ArrayList<>() : this.metaReadRoles;
        this.metaReadRoles.addAll(Arrays.asList(roles));

        return this;
    }

    /**
     * Adds metadata write roles.
     * @param roles
     */
    public StreamAcl addMetaWriteRoles(String... roles) {
        this.metaWriteRoles = this.metaWriteRoles == null ? new ArrayList<>() : this.metaWriteRoles;
        this.metaWriteRoles.addAll(Arrays.asList(roles));

        return this;
    }

    /**
     * Returns read roles.
     */
    public List<String> getReadRoles() {
        return readRoles;
    }

    /**
     * Returns write roles.
     */
    public List<String> getWriteRoles() {
        return writeRoles;
    }

    /**
     * Returns delete roles.
     */
    public List<String> getDeleteRoles() {
        return deleteRoles;
    }

    /**
     * Return metadata read roles.
     */
    public List<String> getMetaReadRoles() {
        return metaReadRoles;
    }

    /**
     * Return metadata write roles.
     */
    public List<String> getMetaWriteRoles() {
        return metaWriteRoles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StreamAcl streamAcl = (StreamAcl) o;
        return Objects.equals(readRoles, streamAcl.readRoles) && Objects.equals(writeRoles, streamAcl.writeRoles) && Objects.equals(deleteRoles, streamAcl.deleteRoles) && Objects.equals(metaReadRoles, streamAcl.metaReadRoles) && Objects.equals(metaWriteRoles, streamAcl.metaWriteRoles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(readRoles, writeRoles, deleteRoles, metaReadRoles, metaWriteRoles);
    }

    @Override
    public String toString() {
        return "StreamAcl{" +
                "readRoles=" + readRoles +
                ", writeRoles=" + writeRoles +
                ", deleteRoles=" + deleteRoles +
                ", metaReadRoles=" + metaReadRoles +
                ", metaWriteRoles=" + metaWriteRoles +
                '}';
    }
}
