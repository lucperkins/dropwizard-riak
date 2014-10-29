package com.lucperkins.dropwizard.riak.dao;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.RiakException;
import com.basho.riak.client.api.commands.kv.DeleteValue;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.api.commands.kv.UpdateValue;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public class RiakDAO<T> {
    private static final Logger log = LoggerFactory.getLogger(RiakDAO.class);
    Class<T> clazz;
    private RiakClient client;

    public RiakDAO(RiakClient client, Class<T> clazz) {
        this.clazz = clazz;
        this.client = client;
    }

    public T fetch(Location loc) throws RiakException {
        return fetchByLocation(loc);
    }

    public T fetch(String bucket, String key) throws RiakException {
        Location loc = makeLocation(bucket, key);
        return fetchByLocation(loc);
    }

    public T fetch(String bucket, String key, String bucketType) throws RiakException {
        Location loc = makeLocation(bucket, key, bucketType);
        return fetchByLocation(loc);
    }

    public boolean store(RiakableObject obj) throws RiakException {
        Location loc = obj.getLocation();
        StoreValue.Builder storeOp = new StoreValue.Builder(obj)
                .withOption(StoreValue.Option.RETURN_BODY, true);
        try {
            client.execute(storeOp.build());
            return found(loc);
        } catch (ExecutionException | InterruptedException e) {
            throw new RiakException(e);
        }
    }

    public boolean store(T t, String bucket, String key) throws RiakException {
        Location loc = makeLocation(bucket, key);
        StoreValue.Builder storeOp = new StoreValue.Builder(t)
                .withLocation(loc);
        try {
            client.execute(storeOp.build());
            return found(loc);
        } catch (ExecutionException | InterruptedException e) {
            throw new RiakException(e);
        }
    }

    public boolean store(T t, String bucket, String key, String bucketType) throws RiakException {
        Location loc = makeLocation(bucket, key, bucketType);
        StoreValue.Builder storeOp = new StoreValue.Builder(t)
                .withOption(StoreValue.Option.RETURN_BODY, true)
                .withLocation(loc);
        try {
            client.execute(storeOp.build());
            return found(loc);
        } catch (ExecutionException | InterruptedException e) {
            throw new RiakException(e);
        }
    }

    public boolean update(Location loc, UpdateValue.Update update) throws RiakException {
        UpdateValue.Builder updateOp = new UpdateValue.Builder(loc)
                .withFetchOption(FetchValue.Option.DELETED_VCLOCK, true)
                .withUpdate(update);

        try {
            UpdateValue.Response res = client.execute(updateOp.build());
            return res.wasUpdated();
        } catch (ExecutionException | InterruptedException e) {
            throw new RiakException(e);
        }

    }

    public boolean update(T t, String bucket, String key) throws RiakException {
        Location loc = new Location(new Namespace(bucket), key);
        UpdateValue.Builder updateOp = new UpdateValue.Builder(loc)
                .withFetchOption(FetchValue.Option.DELETED_VCLOCK, true)
                .withUpdate(UpdateValue.Update.clobberUpdate(t));

        try {
            UpdateValue.Response res = client.execute(updateOp.build());
            return res.wasUpdated();
        } catch (ExecutionException | InterruptedException e) {
            throw new RiakException(e);
        }
    }

    public boolean update(T t, String bucket, String key, String bucketType) throws RiakException {
        Location loc = new Location(new Namespace(bucketType, bucket), key);
        UpdateValue.Builder updateOp = new UpdateValue.Builder(loc)
                .withFetchOption(FetchValue.Option.DELETED_VCLOCK, true)
                .withUpdate(UpdateValue.Update.clobberUpdate(t));

        try {
            UpdateValue.Response res = client.execute(updateOp.build());
            return res.wasUpdated();
        } catch (ExecutionException | InterruptedException e) {
            throw new RiakException(e);
        }
    }

    public boolean update(RiakableObject obj) throws RiakException {
        Location loc = obj.getLocation();
        UpdateValue.Builder updateOp = new UpdateValue.Builder(loc)
                .withFetchOption(FetchValue.Option.DELETED_VCLOCK, true)
                .withUpdate(UpdateValue.Update.clobberUpdate(obj));

        try {
            UpdateValue.Response res = client.execute(updateOp.build());
            return res.wasUpdated();
        } catch (ExecutionException | InterruptedException e) {
            throw new RiakException(e);
        }
    }

    public boolean delete(Location loc) throws RiakException {
        DeleteValue.Builder deleteOp = new DeleteValue.Builder(loc);
        try {
            client.execute(deleteOp.build());
            return found(loc);
        } catch (ExecutionException | InterruptedException e) {
            throw new RiakException(e);
        }
    }

    public boolean delete(RiakableObject obj) throws RiakException {
        Location loc = obj.getLocation();
        DeleteValue.Builder deleteOp = new DeleteValue.Builder(loc);
        try {
            client.execute(deleteOp.build());
            return found(loc);
        } catch (ExecutionException | InterruptedException e) {
            throw new RiakException(e);
        }
    }

    public boolean delete(String bucket, String key) throws RiakException {
        Location loc = makeLocation(bucket, key);
        DeleteValue.Builder deleteOp = new DeleteValue.Builder(loc);
        try {
            client.execute(deleteOp.build());
            return found(loc);
        } catch (ExecutionException | InterruptedException e) {
            throw new RiakException(e);
        }
    }

    public boolean delete(String bucket, String key, String bucketType) throws RiakException {
        Location loc = makeLocation(bucket, key, bucketType);
        DeleteValue.Builder deleteOp = new DeleteValue.Builder(loc);
        try {
            client.execute(deleteOp.build());
            return found(loc);
        } catch (ExecutionException | InterruptedException e) {
            throw new RiakException(e);
        }
    }

    private static Location makeLocation(String bucket, String key) {
        return new Location(new Namespace(bucket), key);
    }

    private static Location makeLocation(String bucket, String key, String bucketType) {
        return new Location(new Namespace(bucketType, bucket), key);
    }

    private T fetchByLocation(Location loc) throws RiakException {
        FetchValue.Builder fetchOp = new FetchValue.Builder(loc);
        try {
            T t = client.execute(fetchOp.build()).getValue(clazz);

            if (t == null) {
                throw new RiakException("Object is null");
            }

            return t;
        } catch (ExecutionException | InterruptedException e) {
            throw new RiakException(e);
        }
    }

    private boolean found(Location loc) throws RiakException {
        FetchValue.Builder fetchOp = new FetchValue.Builder(loc);
        try {
            FetchValue.Response res = client.execute(fetchOp.build());
            return !res.isNotFound();
        } catch (ExecutionException | InterruptedException e) {
            throw new RiakException(e);
        }
    }
}
