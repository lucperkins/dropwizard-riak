package com.lucperkins.dropwizard.riak;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.RiakException;
import com.basho.riak.client.api.cap.ConflictResolver;
import com.basho.riak.client.api.cap.ConflictResolverFactory;
import com.basho.riak.client.api.commands.kv.UpdateValue;
import com.basho.riak.client.core.RiakCluster;
import com.lucperkins.dropwizard.riak.dao.RiakDAO;
import com.lucperkins.dropwizard.riak.dao.RiakableObject;
import com.lucperkins.dropwizard.riak.operations.HostAndPort;
import com.lucperkins.dropwizard.riak.operations.RiakClusterManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.UnknownHostException;
import java.util.*;

import static org.junit.Assert.*;

public class RiakDAOTest {
    public static class Person extends RiakableObject {
        private String name;
        private int age;
        private Set<String> hobbies;

        public Person(String name, int age, Set<String> hobbies) {
            this.name = name; this.age = age; this.hobbies = hobbies;

            setBucket("people");
            setKey(name.toLowerCase());
            setBucketType("siblings");
        }

        public void setAge(int age) { this.age = age; }

        public String getName() { return name; }
        public int getAge() { return age; }
        public Set<String> getHobbies() { return hobbies; }

        public static class Resolver implements ConflictResolver<Person> {
            @Override
            public Person resolve(List<Person> siblings) {
                if (siblings.size() == 0) {
                    return null;
                } else {
                    return siblings.get(0);
                }
            }
        }

        public static class AgeByOneYear extends UpdateValue.Update<Person> {
            @Override
            public Person apply(Person original) {
                int newAge = original.getAge() + 1;
                original.setAge(newAge);
                return original;
            }
        }

        public Person() { /* Jackson JSON deserialization */ }
    }

    private Person luc;
    private Person cindy;
    private RiakDAO<Person> riak;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws UnknownHostException {
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

        RiakClient client = new RiakClient(cluster);
        manager.registerConflictResolver(Person.class, new Person.Resolver());
        cluster.start();



        riak = new RiakDAO<>(client, Person.class);

        Set<String> lucHobbies = new HashSet<String>();
        lucHobbies.add("computering");
        lucHobbies.add("guitar");
        lucHobbies.add("singing");
        lucHobbies.add("a bunch of other stuff");
        luc = new Person("Luc", 32, lucHobbies);


    }

    @Test
    public void testStoreAndFetch() throws RiakException {
        assertTrue(riak.store(luc));
        Person fetchedPerson = riak.fetch(luc.getLocation());
        assertNotNull(fetchedPerson);
        assertEquals(fetchedPerson.getName(), luc.getName());
        assertEquals(fetchedPerson.getAge(), luc.getAge());
        assertEquals(fetchedPerson.getHobbies(), luc.getHobbies());
    }

    @Test
    public void testDelete() throws RiakException {
        riak.delete(luc);

        thrown.expect(RiakException.class);
        Person fetchedPerson2 = riak.fetch(luc.getLocation());
    }

    @Test
    public void testUpdate() throws RiakException {
        Set<String> cindyHobbies = new HashSet<>();
        cindyHobbies.add("cycling");
        cindyHobbies.add("philosophy");
        cindy = new Person("Cindy", 75, cindyHobbies);
        riak.store(cindy);
        riak.update(cindy.getLocation(), new Person.AgeByOneYear());
        Person fetchedPerson = riak.fetch(cindy.getLocation());
        assertEquals(fetchedPerson.getAge(), cindy.getAge() + 1);
    }

    @After
    public void tearDown() {
    }
}