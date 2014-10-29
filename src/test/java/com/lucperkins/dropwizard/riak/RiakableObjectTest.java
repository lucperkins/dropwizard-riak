package com.lucperkins.dropwizard.riak;

import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.lucperkins.dropwizard.riak.dao.RiakableObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class RiakableObjectTest {
    private static class Pojo extends RiakableObject {
        private String word;
        private int number;

        private Pojo(String word, int number) {
            this.word = word; this.number = number;
        }
    }

    private final Pojo testPojo = new Pojo("leet", 1337);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testLocationInfo() {
        testPojo.setBucket("pojos");
        testPojo.setKey("leet");
        testPojo.setBucketType("siblings");
        assertEquals(testPojo.getBucket(), "pojos");
        assertEquals(testPojo.getKey(), "leet");
        assertEquals(testPojo.getBucketType(), "siblings");
        assertEquals(testPojo.getLocation(), new Location(new Namespace("siblings", "pojos"), "leet"));
    }

    @Test
    public void testFalseLocationInfo() {
        Pojo testPojo2 = new Pojo("nine-ninety-nine", 999);
        assertNull(testPojo2.getBucket());
        assertNull(testPojo2.getKey());
        assertNotNull(testPojo2.getBucketType());
        assertEquals(testPojo2.getBucketType(), "default");

        thrown.expect(IllegalArgumentException.class);
        assertNull(testPojo2.getLocation());
    }
}
