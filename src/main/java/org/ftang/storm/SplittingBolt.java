package org.ftang.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import java.util.Date;
import java.util.Map;

/**
 * @author mcl
 */
public class SplittingBolt extends BaseRichBolt {

    private OutputCollector _collector;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        _collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {
        int counter = 0;
        for (char c : tuple.getStringByField(RandomDataSpout.FIELD_CONTENT).toCharArray()) {
            _collector.emit(tuple, new Values(new Date(), counter, "" + c));
            counter++;
        }
        _collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(FIELD_DATE, FIELD_LINE_NO, FIELD_CONTENT));
    }

    public static String FIELD_DATE = "date";
    public static String FIELD_LINE_NO = "line_no";
    public static String FIELD_CONTENT = "content";
}
