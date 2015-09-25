package com.jet.sample;

import java.util.concurrent.atomic.AtomicInteger;

public class MyCounter {
	   private AtomicInteger count = new AtomicInteger(0);
	   
	   public void incrementCount() {
	      count.incrementAndGet();
	   }
	 
	   public int getCount() {
	     return count.get();
	   }
}
