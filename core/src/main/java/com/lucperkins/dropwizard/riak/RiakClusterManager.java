package com.lucperkins.dropwizard.riak;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.RiakNode;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RiakClusterManager implements Managed {
    private static final Logger log = LoggerFactory.getLogger(RiakClusterManager.class);
    private final RiakCluster cluster;

    public RiakClusterManager(RiakCluster cluster) {
        this.cluster = cluster;
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

    public static RiakClient buildClient(List<String> hosts, int port) throws UnknownHostException {
        RiakNode.Builder nodeBuilder = new RiakNode.Builder()
                .withRemotePort(port);
        List<RiakNode> nodes = RiakNode.Builder.buildNodes(nodeBuilder, hosts);
        RiakCluster cluster = RiakCluster.builder(nodes).build();
        cluster.start();
        log.info("Riak cluster has been successfully built and started");
        return new RiakClient(cluster);
    }
}