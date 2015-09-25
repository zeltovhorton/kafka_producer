
package com.jet.sample;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import com.google.common.base.Throwables;
import com.jet.sample.scenarios.MessagesNoDuplicatesThrottle;
import com.jet.storm.Util;
import com.jet.storm.config.KafkaToHiveConfig;


public class KafkaSampleProducer {

    private static Logger LOG = Logger.getLogger(KafkaSampleProducer.class.getName());

    private static int kafkaSenderBatchSize = 10;
    private static int kafkaSampleSize = 10;
    private static boolean sendBatch = false;
    private static int kafkaSenderThreads = 1;


    private static String topic;
    static {
        try {
            KafkaToHiveConfig topologyConfig = Util.getKafkaToHiveConfig("alex.json");
//            KafkaToHiveConfig topologyConfig = Util.getKafkaToHiveConfig("alexAzure.json");
            topic = topologyConfig.kafkaConfig.sourceTopic;
            kafkaSampleSize = topologyConfig.kafkaConfig.kafkaSampleSize;
            kafkaSenderBatchSize = topologyConfig.kafkaConfig.kafkaSenderBatchSize;
            kafkaSenderThreads = topologyConfig.kafkaConfig.kafkaSenderThreads;
            
        } catch (IOException e) {
            e.printStackTrace();
            Throwables.propagate(e);
        }
    }
    private static Messages messages = new MessagesNoDuplicatesThrottle(topic, kafkaSampleSize);

    public static void main(String... args) throws IOException, InterruptedException {

    	LOG.info("STARTING ALEX===========");
    	LOG.info("STARTING ALEX===========");
		final MyCounter counter = new MyCounter();
    	
        if(kafkaSenderThreads == 1){
            //sendMessages();
        	LOG.info("error");
        }else{
            ExecutorService executorService = Executors.newFixedThreadPool(kafkaSenderThreads);
            for(int i=0; i<kafkaSenderThreads; i++){
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        sendMessages(counter);
                    }
                });
            }
            executorService.shutdown();
        }
    }

    private static void sendMessages(MyCounter counter) {
        // Kafka Properties
        Properties props = new Properties();
        // HDP uses 6667 as the broker port.
		props.put("metadata.broker.list", "sandbox.hortonworks.com:6667");
//		props.put("metadata.broker.list", "jetslave4.jetnetname.artem.com:6667,jetslave5.jetnetname.artem.com:6667,jetslave6.jetnetname.artem.com:6667,jetmaster1.jetnetname.artem.com:6667,jetmaster2.jetnetname.artem.com:6667");
		
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("request.required.acks", "1");

        ProducerConfig config = new ProducerConfig(props);
        Producer<String, String> producer = new Producer<>(config);

        while(messages.hasNext()){
            List<KeyedMessage<String, String>> messagesBatch = getMessagesBatch(messages);
            if(sendBatch){
                producer.send(messagesBatch);
            }else{
                for(KeyedMessage<String, String> m : messagesBatch){
                    producer.send(m);
                    counter.incrementCount();
                }
            }
        }
        LOG.info("thread shutdown, # of msgs processed:" + counter.getCount());

        producer.close();
    }

    private static List<KeyedMessage<String, String>> getMessagesBatch(Messages messages) {
        int counter = 0;
        List<KeyedMessage<String, String>> rList = new ArrayList<>();
        while(counter < kafkaSenderBatchSize &&  messages.hasNext()){
            counter++;
            rList.add(messages.next());
        }
        return rList;
    }


}
