package com.jet.storm.config;

import java.io.Serializable;

/**
 * Created by christoph on 8/24/15.
 */
public class StormConfig implements Serializable {

    public String topologyName;

    public int numberOfWorkers = 2;
    public int msgTimeoutSeconds = 60;

    public int kafkaSpoutCount = 1;
    public int mapperBoltCount = 1;
    public int hiveBoltCount = 1;
    public int maxSpoutPending = 3000;

}
