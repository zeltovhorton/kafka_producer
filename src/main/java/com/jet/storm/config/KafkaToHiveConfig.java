package com.jet.storm.config;

import com.google.gson.Gson;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by christoph on 8/24/15.
 */
public class KafkaToHiveConfig implements Serializable{

    public HiveConfig hiveConfig = new HiveConfig();
    public KafkaConfig kafkaConfig = new KafkaConfig();
    public MappingConfig mappingConfig = new MappingConfig();
    public StormConfig stormConfig = new StormConfig();

    public String id = "";

    @Override
    public String toString(){
        return new Gson().toJson(this);
    }

    public static KafkaToHiveConfig parse(String config){
        return new Gson().fromJson(config, KafkaToHiveConfig.class);
    }

    public String getId() {
        return id;
    }
    public void setId(String id)
    {
        this.id=id;
    }
    
}
