package com.jet.storm;

import backtype.storm.Constants;
import backtype.storm.tuple.Tuple;
import com.jet.storm.config.KafkaToHiveConfig;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by christoph on 8/26/15.
 */
public class Util {

    private static Logger LOG = Logger.getLogger(Util.class.getName());

    public  static KafkaToHiveConfig getKafkaToHiveConfig(String fileName) throws IOException {
        List<String> configCP = IOUtils.readLines(ClassLoader.getSystemResourceAsStream(fileName));
        String configString = StringUtils.join(configCP, "\n");
        LOG.info("using config '" + configString + "'");

        return KafkaToHiveConfig.parse(configString);
    }

    public static boolean isTickTuple(Tuple tuple) {
        return tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID)
                && tuple.getSourceStreamId().equals(Constants.SYSTEM_TICK_STREAM_ID);
    }
}
