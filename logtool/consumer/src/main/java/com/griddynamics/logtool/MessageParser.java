package com.griddynamics.logtool;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class MessageParser {
    private static final String ISO8601Date = "2[0-9]{3}-[0-1][0-9]-[0-3][0-9]";
    private static final String DATEDate="[0-3][0-9] [JFMASOND][a-z]{2} 2[0-9]{3}";
    private static final String ABSOLUTE="[0-2][0-9]:[0-5][0-9]:[0-5][0-9],[0-9]{3}";
    private static final String AnyDate = "(?:(?:" + DATEDate + ")?|(?:" + ISO8601Date + ")?)?";
    private static final String delimiter = "[ ]*[|][ ]*";
    private static final String appInst = "([a-zA-Z0-9]+)";
    private static final Pattern parsePattern = Pattern.compile("[^|]+" + delimiter + appInst +
            delimiter + appInst + delimiter +
            "(" + "(?:.+" + delimiter + ")?(" + AnyDate + " ?" + ABSOLUTE + ")[ ]*[|][ ]*.+)\\n");
    private static final Pattern ISO8601DatePattern = Pattern.compile("(" + ISO8601Date+ ") " + ABSOLUTE);
    private static final Pattern DATEDatePattern = Pattern.compile("(" + DATEDate+ ") " + ABSOLUTE);

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final DateTimeFormatter isoFmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss,SSS");
    private static final DateTimeFormatter dateFmt = DateTimeFormat.forPattern("dd MMM yyyy HH:mm:ss,SSS");    
    private static DateTime dt;
    

    public static ParsedMessage parseMessage(String incoming) {
        ParsedMessage output = new ParsedMessage();
        Matcher m = parsePattern.matcher(incoming);
        if(m.matches()){
            output.setApplication(m.group(1));
            output.setInstance(m.group(2));
            output.setMessage(m.group(3));
            output.setTimestamp(parseDate(m.group(4)));
        }
        return output;
    }
    private static String parseDate(String someDate){
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

class ParsedMessage {
    private String application;
    private String instance;
    private String timestamp;
    private String message;

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}


