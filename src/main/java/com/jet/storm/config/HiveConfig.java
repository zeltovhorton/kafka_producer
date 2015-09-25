package com.jet.storm.config;

import java.io.Serializable;

/**
 * Created by christoph on 8/24/15.
 */
public class HiveConfig implements Serializable {

    //hive
    public String metaStoreURI;
    public String dbName;
    public String tblName;
    public int transactionsPerBatch = 100;
    public int batchSize = 15000;
    public int idlTimeout = 10;
}
