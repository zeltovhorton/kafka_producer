package com.jet.sample;

import kafka.producer.KeyedMessage;

import java.util.Iterator;

/**
 * Created by christoph on 8/27/15.
 */
public interface Messages extends Iterator<KeyedMessage<String, String>> {
    public long getCurrentCount();
}
