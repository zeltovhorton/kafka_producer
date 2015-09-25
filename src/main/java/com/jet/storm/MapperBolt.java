package com.jet.storm;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.FailedException;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.google.common.base.Throwables;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.jet.storm.config.Field;
import com.jet.storm.config.KafkaToHiveConfig;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by christoph on 8/24/15.
 */
public class MapperBolt  extends BaseBasicBolt {

    private static final Logger LOG = Logger.getLogger(MapperBolt.class.getName());

    private KafkaToHiveConfig config;

    public MapperBolt(KafkaToHiveConfig config) {
        LOG.info("mapper bolt instanciated with "+config);
        this.config = config;
    }

    private Random random = new Random();

    @Override
    public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {

        //FIXME
        if(false && random.nextInt(10) == 1)
            throw new RuntimeException(
                    "hello chaos monkey - simulating random failure"
            );

        if(!Util.isTickTuple(tuple)){
            Fields fields = tuple.getFields();
            try {
                String jsonIn = new String((byte[]) tuple.getValueByField(fields.get(0)), "UTF-8");
                Values values = createValues(jsonIn);
                basicOutputCollector.emit(values);
            } catch (Exception ex) {
                ex.printStackTrace();
                LOG.log(Level.SEVERE, "ERROR in execute with fields " + fields, ex);
                throw new FailedException(ex.toString());
            }
        }else{
            LOG.info("tick "+tuple.getLong(0));
        }
    }


    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(config.mappingConfig.getCombinedFieldNames()));
    }

    public Values createValues(String jsonIn) throws IOException, ClassNotFoundException {

        JsonNode rootNode = getJsonNode(jsonIn);
        Field[] fields = config.mappingConfig.getCombinedFields();
        Object[] values = new Object[fields.length];
        int index = 0;
        for(Field f : fields){
            String o = getValue(rootNode, f);
            values[index] = o;
            index++;
        }
        return new Values(values);
    }

    private JsonNode getJsonNode(String jsonIn) throws IOException {
        JsonFactory factory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(factory);
        return mapper.readTree(jsonIn);
    }

    private String getValue(JsonNode rootNode, Field f) throws ClassNotFoundException {
        JsonNode node = rootNode.get(f.name);
        if(node == null)
            throw new RuntimeException(
                    f.name + " cannot be found in json " + rootNode +
                            " please revisit input json or configuration.");
        if(node.isContainerNode()){
            return node.toString();
        }else{
            return node.asText();
        }
    }

}
