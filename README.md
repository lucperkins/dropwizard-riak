Riak Plugin for Dropwizard
==========================

This library provides a small set of tools for those wanting to use Riak
in conjunction with Dropwizard, including a `RiakClusterManager` class
that implements Dropwizard's `Managed` interface, a [data access
object](http://en.wikipedia.org/wiki/Data_access_object) \(DAO) for
type-specific interactions with Riak, a Riak health checker, and more.

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
