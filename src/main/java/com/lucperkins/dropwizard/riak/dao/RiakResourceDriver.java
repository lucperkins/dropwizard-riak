package com.lucperkins.dropwizard.riak.dao;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.RiakException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.URI;

public class RiakResourceDriver<T> {
    private final RiakDAO<T> riak;

    RiakResourceDriver(RiakClient client, Class<T> clazz) {
        this.riak = new RiakDAO<>(client, clazz);
    }

    public T get(String bucket, String key) {
        try {
            return riak.fetch(bucket, key);
        } catch (RiakException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    public Response post(RiakableObject obj, String uriString) {
        URI uri = URI.create(uriString);
        try {
            if (riak.update(obj)) {
                return Response.created(uri).build();
            } else {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
        } catch (RiakException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }
}
