package org.ftang.storm;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.UUID;

/**
 * @author mcl
 */
public class RandomDataSpout extends BaseRichSpout {

    private Log log = LogFactory.getLog(RandomDataSpout.class);

    private SpoutOutputCollector _collector;
    private final int _interval;

    public RandomDataSpout(int interval) {
        _interval = interval;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(FIELD_CONTENT));
    }

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector collector) {
        _collector = collector;
    }

    @Override
    public void ack(Object id) {
        log.info("ack file " + id);

    }
    @Override
    public void nextTuple() {
        _collector.emit(new Values(UUID.randomUUID().toString()), UUID.randomUUID().toString());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error(e);
        }
    }

    public static String FIELD_FILENAME = "filename";
    public static String FIELD_CONTENT = "file_content";
}
