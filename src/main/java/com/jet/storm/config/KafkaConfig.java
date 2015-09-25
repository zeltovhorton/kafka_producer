package com.jet.storm.config;

import org.apache.hadoop.hive.serde2.SerDeException;

import java.io.Serializable;

/**
 * Created by christoph on 8/24/15.
 */
public class KafkaConfig implements Serializable {

    public String sourceTopic;

    
    public String kafkaSpoutZkHosts;
    public String kafkaSpoutZkRoot;
    public String kafkaSpoutConsumerGroup;
    public String kafkaSpoutConsumerClientId;
    public boolean useStartOffsetTimeIfOffsetOutOfRange;
    
    public int kafkaSenderBatchSize;
    public int kafkaSampleSize;
    public int kafkaSenderThreads;

}
