/*
 * Copyright 2018 Vassilis Bekiaris
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vbekiaris;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.QueryCache;
import com.hazelcast.query.SqlPredicate;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class QueryCachePopulationBenchmark {

    // toggle this flag to compare initial population time when map is indexed or not
    // sample results:
    //  without index: (min, avg, max) = (2219.185, 2522.884, 2817.556), stdev = 206.807
    //  with index:    (min, avg, max) = (0.001, 0.002, 0.003), stdev = 0.001
    private static final boolean MAP_WITH_INDEX = true;

    private HazelcastInstance hz;
    private IMap<Integer, Person> map;

    @Setup(Level.Iteration)
    public void setup() {
        hz = Hazelcast.newHazelcastInstance();

        map = hz.getMap("personById");
        if (MAP_WITH_INDEX) {
            // add an ordered index on age attribute
            map.addIndex("age", true);
        }
        for (int i = 0; i < 1_000_000; i++) {
            map.put(i, new Person((double) i % 100));
        }
    }

    @TearDown(Level.Trial)
    public void tearDownTrial() {
        try {
            QueryCache<Integer, Person> queryCache = map.getQueryCache("toddlersById", new SqlPredicate("age <= 5"), true);
            queryCache.destroy();
        } catch (Exception e) {
            // ignore
        }
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        hz.shutdown();
    }

    @Benchmark
    public void createQueryCache() {
        QueryCache<Integer, Person> queryCache = map.getQueryCache("toddlersById", new SqlPredicate("age <= 5"), true);
    }

    public static class Person implements Serializable {
        double age;

        public Person(double age) {
            this.age = age;
        }

        public double getAge() {
            return age;
        }

        public void setAge(double age) {
            this.age = age;
        }
    }

}
