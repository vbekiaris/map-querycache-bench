# Query Cache population benchmark

A simple benchmark to measure time taken for initial population of `QueryCache`. Toggle `QueryCachePopulationBenchmark.MAP_WITH_INDEX` to compare time taken when the backing `IMap` is indexed vs when no index exists.

## Build and run

Build this module with maven:

```bash
mvn clean package
```

Then run the output uber-jar as follows:

```bash
java -jar target/benchmarks.jar
```

This benchmark is built on [JMH](http://openjdk.java.net/projects/code-tools/jmh/); for a list of options run:

```bash
java -jar target/benchmarks.jar -h
``` 
