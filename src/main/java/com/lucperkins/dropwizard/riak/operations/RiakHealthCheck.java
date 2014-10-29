package com.lucperkins.dropwizard.riak.operations;

import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.operations.PingOperation;
import com.codahale.metrics.health.HealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RiakHealthCheck extends HealthCheck {
    private static final Logger log = LoggerFactory.getLogger(RiakHealthCheck.class);
    private final RiakCluster cluster;
    private final String healthyMessage;
    private final String unhealthyMessage;

    public RiakHealthCheck(RiakCluster cluster, String healthyMessage, String unhealthyMessage) {
        this.cluster = cluster;
        this.healthyMessage = healthyMessage;
        this.unhealthyMessage = unhealthyMessage;
    }

    @Override
    protected Result check() throws Exception {
        try {
            PingOperation ping = new PingOperation();
            cluster.execute(ping);
            ping.await();

            if (ping.isSuccess()) {
                log.info(healthyMessage);
                return Result.healthy(healthyMessage);
            } else {
                log.error(unhealthyMessage);
                return Result.unhealthy(unhealthyMessage);
            }
        } catch (InterruptedException e) {
            throw new Exception(e);
        }
    }
}