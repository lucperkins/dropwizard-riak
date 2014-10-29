package com.lucperkins.dropwizard.riak.dao;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.RiakException;
import com.basho.riak.client.core.query.Location;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.URI;

public class RiakResourceDriver<T> {
    private final RiakDAO<T> riak;

    public RiakResourceDriver(RiakClient client, Class<T> clazz) {
        this.riak = new RiakDAO<>(client, clazz);
    }

    public T get(Location loc) {
        try {
            return riak.fetch(loc);
        } catch (RiakException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    public T get(String bucket, String key) {
        try {
            return riak.fetch(bucket, key);
        } catch (RiakException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    public T get(String bucket, String key, String bucketType) {
        try {
            return riak.fetch(bucket, key, bucketType);
        } catch (RiakException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    public Response post(RiakableObject obj, String uriString) {
        URI uri = URI.create(uriString);
        try {
            if (riak.store(obj)) {
                return Response.created(uri).build();
            } else {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
        } catch (RiakException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    public Response delete(Location loc) {
        try {
            if (riak.delete(loc)) {
                return Response.status(202).build();
            } else {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
        } catch (RiakException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }
}
