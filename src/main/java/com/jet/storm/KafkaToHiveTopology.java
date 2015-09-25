package com.jet.storm;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.AuthorizationException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import com.jet.storm.config.KafkaToHiveConfig;
import org.apache.storm.hive.bolt.HiveBolt;
import org.apache.storm.hive.bolt.mapper.DelimitedRecordHiveMapper;
import org.apache.storm.hive.common.HiveOptions;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.ZkHosts;

import java.io.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;


public class KafkaToHiveTopology {


        public static final String KAFKA_SPOUT_ID = "kafka-spout";
        public static final String MAPPER_BOLT_ID = "mapper-bolt";
        public static final String HIVE_BOLT_ID = "hive-bolt";

        public static final String SAMPLE_CONFIG = "clicks-config-jetpoc.json";

        private static Logger LOG = Logger.getLogger(KafkaToHiveTopology.class.getName());

        public static void main(String... args) throws IOException {

            String configFile = SAMPLE_CONFIG;

            if(args.length!=1){
                LOG.warning("no path to config file provided via commandline -" +
                        "using sample config " + configFile);
            } else {
                configFile = SAMPLE_CONFIG;
            }

            KafkaToHiveConfig config = Util.getKafkaToHiveConfig(configFile);

            String[] combinedFieldNames = config.mappingConfig.getCombinedFieldNames();
            LOG.info("configuring field names "+ Arrays.toString(combinedFieldNames));

            //Kafka spout
            SpoutConfig spoutConfig = new SpoutConfig(
                    new ZkHosts(config.kafkaConfig.kafkaSpoutZkHosts),
                    config.kafkaConfig.sourceTopic,
                    config.kafkaConfig.kafkaSpoutZkRoot,
                    config.kafkaConfig.kafkaSpoutConsumerClientId);
            //FIXME proper consumer group support

            spoutConfig.useStartOffsetTimeIfOffsetOutOfRange = config.kafkaConfig.useStartOffsetTimeIfOffsetOutOfRange;
            spoutConfig.startOffsetTime = System.currentTimeMillis();
            KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig);

            //nl.minvenj.nfi.storm.kafka.KafkaSpout cgSpout = new nl.minvenj.nfi.storm.kafka.KafkaSpout();

            // Hive bolt
            DelimitedRecordHiveMapper mapper = new DelimitedRecordHiveMapper()
                    .withColumnFields(new Fields(config.mappingConfig.getFieldNames()))
                    .withPartitionFields(new Fields(config.mappingConfig.getPartitionFieldNames()));
//                    .withTimeAsPartitionField("yyyy-MM-dd-HH");

            HiveOptions hiveOptions;
            hiveOptions = new HiveOptions(
                    config.hiveConfig.metaStoreURI,
                    config.hiveConfig.dbName,
                    config.hiveConfig.tblName,
                    mapper)
                    .withTxnsPerBatch(config.hiveConfig.transactionsPerBatch)
                    .withBatchSize(config.hiveConfig.batchSize)
                    .withIdleTimeout(config.hiveConfig.idlTimeout);
            //.withKerberosKeytab(path_to_keytab)
            //.withKerberosPrincipal(krb_principal);

            TopologyBuilder builder = new TopologyBuilder();

            builder.setSpout(
                    KAFKA_SPOUT_ID,
                    kafkaSpout,
                    config.stormConfig.kafkaSpoutCount);
            builder.setBolt(
                    MAPPER_BOLT_ID,
                    new MapperBolt(config),
                    config.stormConfig.mapperBoltCount).shuffleGrouping(KAFKA_SPOUT_ID);
            builder.setBolt(
                    HIVE_BOLT_ID,
                    new HiveBolt(hiveOptions),
                    config.stormConfig.hiveBoltCount).shuffleGrouping(MAPPER_BOLT_ID);

            Config stormConfig = new Config();
            stormConfig.setNumWorkers(config.stormConfig.numberOfWorkers);
            stormConfig.setMessageTimeoutSecs(config.stormConfig.msgTimeoutSeconds);
            stormConfig.setMaxSpoutPending(config.stormConfig.maxSpoutPending);
            //stormConfig.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, 10);

            //consumer group spout
            stormConfig.put("kafka.spout.topic", config.kafkaConfig.sourceTopic);
            stormConfig.put("kafka.spout.consumer.group", config.kafkaConfig.kafkaSpoutConsumerGroup);
            stormConfig.put("kafka.zookeeper.connect", config.kafkaConfig.kafkaSpoutZkHosts);
            stormConfig.put("kafka.consumer.timeout.ms", 100);

            try {
                StormSubmitter.submitTopology(
                        config.stormConfig.topologyName,
                        stormConfig,
                        builder.createTopology());
            } catch (AlreadyAliveException | InvalidTopologyException | AuthorizationException ex) {
                ex.printStackTrace();
                Logger.getLogger(KafkaToHiveTopology.class.getName()).log(Level.SEVERE, null, ex);
            }
        }



}
