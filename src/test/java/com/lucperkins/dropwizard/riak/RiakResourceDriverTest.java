package com.lucperkins.dropwizard.riak;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.RiakException;
import com.basho.riak.client.core.RiakCluster;
import com.lucperkins.dropwizard.riak.dao.RiakDAO;
import com.lucperkins.dropwizard.riak.dao.RiakResourceDriver;
import com.lucperkins.dropwizard.riak.operations.HostAndPort;
import com.lucperkins.dropwizard.riak.operations.RiakClusterManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class RiakResourceDriverTest {
    private RiakClient client;
    private RiakResourceDriver<Person> driver;
    private Person luc;
    private RiakDAO<Person> riak;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws RiakException, UnknownHostException {
        String HOST = "127.0.0.1";
        List<HostAndPort> nodes = new LinkedList<>();

        HostAndPort node1 = new HostAndPort(HOST, 10017);
        nodes.add(node1);
        HostAndPort node2 = new HostAndPort(HOST, 10027);
        nodes.add(node2);
        HostAndPort node3 = new HostAndPort(HOST, 10037);
        nodes.add(node3);

        RiakCluster cluster = RiakClusterManager.buildCluster(nodes);
        RiakClusterManager manager = new RiakClusterManager(cluster);
        cluster.start();

        client = new RiakClient(cluster);
        manager.registerConflictResolver(Person.class, new Person.Resolver());

        driver = new RiakResourceDriver<>(client, Person.class);

        Set<String> lucHobbies = new HashSet<>();
        lucHobbies.add("computering");
        lucHobbies.add("guitar");
        lucHobbies.add("singing");
        lucHobbies.add("a bunch of other stuff");
        luc = new Person("Luc", 32, lucHobbies);

        riak = new RiakDAO<>(client, Person.class);
    }

    @Test
    public void testPost() throws RiakException {
        Response res = driver.post(luc, luc.getKey());
        assertEquals(res.getStatus(), 201);
        assertNull(res.getEntity());
    }

    @Test(expected = WebApplicationException.class)
    public void testFailedGet() throws RiakException {
        Person fetchedPerson = driver.get(luc.getLocation());

    }

    @After
    public void tearDown() throws RiakException {
        riak.delete(luc);
    }
}
