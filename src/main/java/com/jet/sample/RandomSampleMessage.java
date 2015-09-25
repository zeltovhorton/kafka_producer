package com.jet.sample;

import com.google.common.base.Throwables;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 * Created by christoph on 8/27/15.
 */


public class RandomSampleMessage {

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static Random random = new Random();
    private static Date date = new Date();
    
    private static String template;

    static {
        try{
            template = FileUtils.readFileToString(new File("src/main/resources/clicks-sample-message.json"));
        }catch (Exception e){
            e.printStackTrace();
            Throwables.propagate(e);
        }
    }

    public static String nextRandomMessage() {
        
        return template.replaceAll("@CLICK_KEY@", UUID.randomUUID().toString())
                        .replaceAll("@CLIENT_ID@", random.nextLong()+"")
                        .replaceAll("@ORDINAL@", random.nextInt(10)+"")
                        .replaceAll("@DATETIME@", date.toString())
                        .replaceAll("@USER_AGENT@", "mozilla")
                        .replaceAll("@URL@", "http://jet.com/seach")
                        .replaceAll("@DEC@", random.nextDouble()+"")
                        .replaceAll("@PARAMS@", "lskdjf,sdlfkjsldf")
                        .replaceAll("@TAGS@", "lskdjf,sdlfkjsldf")
                        .replaceAll("@SOURCE_MESSAGE@", "lskdjf,sdlfkjsldf")
                        .replaceAll("@PARTITION_SAVE_DATE@", dateFormat.format(date));
    }

}
