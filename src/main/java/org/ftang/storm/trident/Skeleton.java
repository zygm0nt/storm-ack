package org.ftang.storm.trident;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.LocalDRPC;
import backtype.storm.generated.StormTopology;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import org.apache.commons.collections.MapUtils;
import storm.trident.TridentTopology;
import storm.trident.operation.*;
import storm.trident.operation.builtin.Count;
import storm.trident.tuple.TridentTuple;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Skeleton {

    public static StormTopology buildTopology(LocalDRPC drpc) throws IOException {
        FakeTweetsBatchSpout spout = new FakeTweetsBatchSpout();

        TridentTopology topology = new TridentTopology();
/*        topology.newStream("spout", spout)
                .parallelismHint(2)
                .shuffle()
                .each(new Fields("actor", "text"), new PerActorTweetsFilter("dave"))
                .parallelismHint(5)
                .each(new Fields("text", "actor"), new UppercaseFunction(), new Fields("uppercased_text"))
                .each(new Fields("actor", "text", "uppercased_text"), new Utils.PrintFilter());*/

        /*topology.newStream("spout", spout)
                .aggregate(new Fields("location"), new LocationAggregator(), new Fields("location_counts"))
                .each(new Fields("location_counts"), new Utils.PrintFilter());*/

        topology.newStream("spout", spout)
                .groupBy(new Fields("location"))
                .aggregate(new Fields("location"), new Count(), new Fields("count"))
                .each(new Fields("location", "count"), new Utils.PrintFilter());

        return topology.build();
    }

    public static void main(String[] args) throws Exception {
        Config conf = new Config();

        LocalDRPC drpc = new LocalDRPC();
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("hackaton", conf, buildTopology(drpc));
    }

    public static class PerActorTweetsFilter extends BaseFilter {

        private int partitionIndex;
        private String actor;

        public PerActorTweetsFilter(String actor) {
            this.actor = actor;
        }
        @Override
        public void prepare(Map conf, TridentOperationContext context) {
            this.partitionIndex = context.getPartitionIndex();
        }
        @Override
        public boolean isKeep(TridentTuple tuple) {
            boolean filter = tuple.getString(0).equals(actor);
            if(filter) {
                System.err.println("I am partition [" + partitionIndex + "] and I have kept a tweet by: " + actor);
            }
            return filter;
        }
    }

    public static class LocationAggregator extends BaseAggregator<Map<String, Integer>> {

        @Override
        public Map<String, Integer> init(Object batchId, TridentCollector collector) {
            return new HashMap<String, Integer>();
        }

        @Override
        public void aggregate(Map<String, Integer> val, TridentTuple tuple, TridentCollector collector) {
            String location = tuple.getString(0);
            val.put(location, MapUtils.getInteger(val, location, 0) + 1);
        }

        @Override
        public void complete(Map<String, Integer> val, TridentCollector collector) {
            collector.emit(new Values(val));
        }
    }

    public static class UppercaseFunction extends BaseFunction {
        @Override
        public void execute(TridentTuple tuple, TridentCollector collector) {
            collector.emit(new Values(tuple.getString(0).toUpperCase()));
        }
    }
}