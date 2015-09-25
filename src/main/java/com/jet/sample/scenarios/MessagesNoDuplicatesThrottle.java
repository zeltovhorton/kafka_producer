package com.jet.sample.scenarios;

import com.jet.sample.RandomSampleMessage;
import kafka.producer.KeyedMessage;

/**
 * Created by christoph on 8/27/15.
 */
public class MessagesNoDuplicatesThrottle implements com.jet.sample.Messages {

    private int numberOfMessages;
    private long counter;
    private String topic;

    private boolean throttle = false;
    private long throttleMillis = 10L;

    public MessagesNoDuplicatesThrottle(String topic, int numberOfMessages){
        this.numberOfMessages = numberOfMessages;
        this.topic = topic;
    }

    @Override
    public boolean hasNext() {
        return counter < numberOfMessages;
    }

    @Override
    public synchronized KeyedMessage<String, String> next() {
        counter ++;
        try {
            if(throttle){
                Thread.sleep(throttleMillis);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new KeyedMessage<String, String>(topic, RandomSampleMessage.nextRandomMessage()) ;
    }

    @Override
    public long getCurrentCount() {
        return counter;
    }

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}
}
