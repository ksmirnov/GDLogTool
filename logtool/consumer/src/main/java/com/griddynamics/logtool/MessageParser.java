package com.griddynamics.logtool;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class MessageParser {
    private static final String ISO8601Date = "2[0-9]{3}-[0-1][0-9]-[0-3][0-9]";
    private static final String DATEDate="[0-3][0-9] [JFMASOND][a-z]{2} 2[0-9]{3}";
    private static final String ABSOLUTE="[0-2][0-9]:[0-5][0-9]:[0-5][0-9],[0-9]{3}";
    private static final Pattern ISO8601DatePattern = Pattern.compile("(" + ISO8601Date+ ") " + ABSOLUTE);
    private static final Pattern DATEDatePattern = Pattern.compile("(" + DATEDate+ ") " + ABSOLUTE);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static final DateTimeFormatter isoFmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss,SSS");
    private static final DateTimeFormatter dateFmt = DateTimeFormat.forPattern("dd MMM yyyy HH:mm:ss,SSS");
    private final DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm:ss");
    private final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static DateTime dt;
    private Pattern parsePattern;
    private Map<String,Integer> groups;

    public MessageParser(String regexp, Map<String,Integer> groups){
        parsePattern = Pattern.compile(regexp);
        this.groups=groups;
    }


    public Map<String,String> parseMessage(String incoming) {
        Map<String,String> parsedIndexes = new HashMap<String,String>();
        Matcher m = parsePattern.matcher(incoming);
        if(m.matches()){
            for(String s: groups.keySet()){
                if(s.equals("timestamp")){
                parsedIndexes.put(s,parseDate(m.group(groups.get(s))));
                }else{
                    parsedIndexes.put(s,m.group(groups.get(s)));
                }
            }
        }
        return parsedIndexes;
    }
    private String parseDate(String someDate){
        String timestamp = "";
        Matcher ISOMatcher = ISO8601DatePattern.matcher(someDate);
        Matcher DATEMatcher = DATEDatePattern.matcher(someDate);
        if(ISOMatcher.matches()){
            //2011-07-22 13:52:48,150 -2011-07-21T21:59:24
            dt = isoFmt.parseDateTime(someDate);
            timestamp = dateTimeFormatter.print(dt);
        } else if (DATEMatcher.matches()){
            //20 Jul 2011 21:20:11,006 - 2011-07-21T21:59:24
            dt = dateFmt.parseDateTime(someDate);
            timestamp = dateTimeFormatter.print(dt);
        } else {
            //21:20:11,006 - 2011-07-21T21:59:24
           dt = new DateTime();
           timestamp = dateTimeFormatter.print(dt);
        }
        return timestamp;

    }
}