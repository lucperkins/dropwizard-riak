Riak Plugin for Dropwizard
==========================

This library provides a small set of tools for those wanting to use Riak
in conjunction with Dropwizard, including a `RiakClusterManager` class
that implements Dropwizard's `Managed` interface, a [data access
object](http://en.wikipedia.org/wiki/Data_access_object) \(DAO) for
type-specific interactions with Riak, a Riak health checker, and more.

This plugin is specific to Riak 2.0+ and Java 1.8.

## Cluster Manager

The `RiakClusterManager` class provides you an easy way to build
clusters, start and stop your cluster (which Dropwizard needs to be able
to do in the background, using the `Managed` interface), and register
conflict resolvers. Here's an example setup:

```java
String HOST = "127.0.0.1";
List<HostAndPort> nodes = new LinkedList<>();
HostAndPort node1 = new HostAndPort(HOST, 10017);
nodes.add(node1);
HostAndPort node2 = new HostAndPort(HOST, 10027);
nodes.add(node2);
HostAndPort node3 = new HostAndPort(HOST, 10037);
nodes.add(node3);

RiakCluster cluster = RiakClusterManager.buildCluster(nodes);
cluster.start();
RiakClusterManager manager = new RiakClusterManager(cluster);
RiakClient client = new RiakClient(cluster);
manager.registerConflictResolver(MyPojo.class, new MyPojo.Resolver());
```

You can also build a cluster by specifying a list of hosts and a single
port:

```java
int port = 8087;
List<String> hosts = new LinkedList<>();
hosts.add("101.0.0.1");
hosts.add("101.0.0.2");
// etc.
RiakCluster cluster = RiakClusterManager.buildCluster(hosts, port);
```

## Riak DAO

The `RiakDAO` class provides you with a convenient means of creating
data access objects (DAOs) for type-specific interactions with Riak.
Let's say that you have a normal POJO class `BlogPost` and would like to
read, modify, and write `BlogPost` objects stored in Riak. First, create
an instance:

```java
// Assuming that you're using the "client" object created in the example
// above:

RiakDAO<BlogPost> riak = new RiakDAO<>(client, BlogPost.class);
```

Now, you can easily interact with `BlogPost` objects:

```java
// This operation will return null if the object is either not found
// or cannot be serialized to a BlogPost object:
BlogPost post = riak.fetch("bucket", "key");

// Or if you're using bucket types:
BlogPost post = riak.fetch("bucket", "key", "bucketType");

boolean deleted = riak.delete("bucket", "key");
// or:
boolean deleted = riak.delete("bucket", "key", "bucketType");

BlogPost post = new BlogPost(...);
boolean stored = riak.store(post, "bucket", "key"); // or with bucket type
```

Object updates are a bit more tricky. Documentation can be found
[here](http://docs.basho.com/riak/latest/dev/using/updates/#Java-Client-Example).
When updating an object, you need to specify an update by extending the
`UpdateValue.Update` abstract class in the Riak Java client. You can
find an example in the
[tests](https://github.com/lucperkins/dropwizard-riak/blob/master/src/test/java/com/lucperkins/dropwizard/riak/RiakDAOTest.java).

## RiakableObjects

Another way of interacting with Riak is to use classes that extend the
`RiakableObject` class. Any class that does that needs to specify a
bucket, key, and bucket type for each object of that class. If no bucket
type is specified, `default` will be used. You will also need to create
an empty Jackson JSON constructor. Below is an example:

```java
public class Person extends RiakableObject {
    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;

        setBucket("people");
        setKey(name.toLowerCase());
        setBucketType("some_bucket_type");
    }

    // Jackson JSON constructor
    public Person() {}

    // Include setters and getters for "name" and "age"
}
```

If you use the `RiakableObject` class, you can use the `RiakDAO` in a
different way. If we use the `BlogPost` class the way we used the
`Person` class in the example above, we can simply create a `BlogPost`
object and store it directly, because the bucket/key/bucket type
information is already included with the object:

```java
BlogPost post = new BlogPost(...);
boolean stored = riak.store(post);
```

## Riak Resource Driver

The real meat of this library is that it enables you to easily get your
Dropwizard resources interacting with Riak. The `RiakResourceDriver`
class enables you to easily transform HTTP requests into Riak
interactions and then back into HTTP responses. Below is an example
resource that does that:

```java
@Path("/people")
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
                    .entity("You must include a name and age")
                    .build();
        }
        Person person = new Person(name, age);
        return driver.post(person, person.getKey());
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") final String id) {
        return driver.delete(BUCKET, id, BUCKET_TYPE);
    }
}
```

## Health Checker

The `RiakHealthCheck` extends Dropwizard's `HealthCheck` class. This
utility periodically pings Riak to check for a response. When
instantiating a Riak health checker, you can either pass in just a
`RiakCluster` object:

```java
RiakHealthCheck riakHealthCheck = new RiakHealthCheck(cluster);
```

Or you can pass in two messages, one for when Riak is healthy and one
for when it isn't:

```java
RiakHealthCheck riakHealthCheck = new RiakHealthCheck(cluster, "Everything is peachy", "Oh shit!");
```

If you don't pass in messages, the defaults are `Riak is healthy` and
`Riak is down`.

## Setting Up the Plugin

You should set up the various elements of the Riak plugin in your main
`run` function. Here's an example setup:

```java
import io.dropwizard.Application;

public class BadassApplication extends Application<MyConfiguration> {
    @Override
    public void run(MyConfiguration config, Environment env) throws Exception {
        // Connection info for a 3-node cluster running on localhost
        List<HostAndPort> riakConnectionInfo = new LinkedList<>();
        riakConnectionInfo.add(new HostAndPort("127.0.0.1", 10017));
        riakConnectionInfo.add(new HostAndPort("127.0.0.1", 10027));
        riakConnectionInfo.add(new HostAndPort("127.0.0.1", 10037));

        // Build some Riak-related stuff
        RiakCluster cluster = RiakClusterManager.buildCluster(riakConnectionInfo);
        RiakClusterManager manager = new RiakClusterManager(cluster);
        RiakClient client = new RiakClient(cluster);

        // Register the cluster manager with Dropwizard
        env.lifecycle().manage(riakClusterManager);

        // Register the health checker
        env.healthChecks().register("riak", new RiakHealthCheck(riakCluster, "YEY!", "WOMP WOMP"));
    }
}
```

## TODOs

* The `ResourceDriver` isn't yet fully RESTful. Do not use it.
* Test coverage is decent but could use some work
* At some point, I'd like to create an abstract resource factory that
  enables you to more easily create Jersey resources
