package com.lucperkins.dropwizard.riak.dao;

import com.basho.riak.client.api.annotations.RiakBucketName;
import com.basho.riak.client.api.annotations.RiakBucketType;
import com.basho.riak.client.api.annotations.RiakKey;
import com.basho.riak.client.api.annotations.RiakVClock;
import com.basho.riak.client.api.cap.VClock;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class RiakableObject {
    @RiakKey
    private String key;

    @RiakBucketName
    private String bucket;

    @RiakBucketType
    private String bucketType;

    public RiakableObject() {
        this.bucketType = bucketType == null ? "default" : bucketType;
    }

    @JsonIgnore
    public Location getLocation() {
        return new Location(new Namespace(bucketType, bucket), key);
    }

    @JsonIgnore
    public String getBucket() { return bucket; }

    @JsonIgnore
    public String getKey() { return key; }

    @JsonIgnore
    public String getBucketType() { return bucketType; }

    @RiakVClock
    private VClock vClock;
}
