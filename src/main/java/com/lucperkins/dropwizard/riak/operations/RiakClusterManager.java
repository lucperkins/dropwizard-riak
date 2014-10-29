package com.lucperkins.dropwizard.riak.operations;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.cap.ConflictResolver;
import com.basho.riak.client.api.cap.ConflictResolverFactory;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.RiakNode;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

public class RiakClusterManager implements Managed {
    private static final Logger log = LoggerFactory.getLogger(RiakClusterManager.class);
    private ConflictResolverFactory factory;
    private RiakCluster cluster;

    public RiakClusterManager(RiakCluster cluster) {
        this.cluster = cluster;
        factory = ConflictResolverFactory.getInstance();
    }

    @Override
    public void start() {
        cluster.start();
        log.info("Riak cluster initialized successfully");
    }

    @Override
    public void stop() {
        cluster.shutdown();
        log.info("Riak cluster shut down successfully");
    }

    public static RiakCluster buildCluster(List<String> hosts, int port) throws UnknownHostException {
        RiakNode.Builder nodeBuilder = new RiakNode.Builder()
                .withRemotePort(port);
        List<RiakNode> nodes = RiakNode.Builder.buildNodes(nodeBuilder, hosts);
        RiakCluster cluster = RiakCluster.builder(nodes).build();
        return cluster;
    }

    public static RiakCluster buildCluster(List<HostAndPort> connectionInfo) throws UnknownHostException {
        RiakNode.Builder nodeBuilder = new RiakNode.Builder();
        List<RiakNode> nodes = new LinkedList<>();

        for (HostAndPort info : connectionInfo) {
            RiakNode node = nodeBuilder
                    .withRemoteAddress(info.host)
                    .withRemotePort(info.port)
                    .build();
            nodes.add(node);
        }

        RiakCluster cluster = RiakCluster.builder(nodes).build();
        return cluster;
    }

    public void registerConflictResolver(Class clazz, ConflictResolver resolver) {
        factory.registerConflictResolver(clazz, resolver);
    }
}