package com.lucperkins.dropwizard.riak;

import com.basho.riak.client.api.RiakClient;
import com.lucperkins.dropwizard.riak.dao.RiakResourceDriver;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.LinkedList;

@Path("/{people}")
public class PersonResource {
    private RiakResourceDriver<Person> driver;
    private static final String BUCKET = "people";
    private static final String BUCKET_TYPE = "siblings";

    public PersonResource(RiakClient client) {
        this.driver = new RiakResourceDriver<>(client, Person.class);
    }

    @GET
    @Path("/{id}")
    public Person get(@PathParam("id") final String id) {
        return driver.get(BUCKET, id, BUCKET_TYPE);
    }

    @POST
    public Response post(@FormParam("name") final String name,
                         @FormParam("age") final int age) {
        if (name == null || age == 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("You must supply a name and age")
                    .build();
        }

        Person person = new Person(name, age, new HashSet<String>());
        return driver.post(person, person.getKey());
    }
}
