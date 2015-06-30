package no.javazone.cake.redux;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.TimeZone;

public class SlotTimeFormatter {
    private String start;
    private String end;
    private int length;


    public SlotTimeFormatter(String formattedSlot) {
        String[] split = formattedSlot.split("\\+");
        String startPart = split[0];
        String endPart = split[1];

        DateTimeFormatter outputFormat = DateTimeFormat.forPattern("yyMMdd HH:mm");

        DateTime startTime = toTime(startPart);
        start = outputFormat.print(startTime);
        DateTime endTime = toTime(endPart);
        end = outputFormat.print(endTime);

        Period period = new Period(startTime, endTime);
        length = period.toStandardMinutes().getMinutes();
    }



    public SlotTimeFormatter(String startPart,int duration) {

        DateTimeFormatter outputFormat = DateTimeFormat.forPattern("yyMMdd HH:mm");

        DateTime startTime = toTime(startPart);
        start = outputFormat.print(startTime);
        DateTime endTime = startTime.plusMinutes(duration);
        end = outputFormat.print(endTime);

        length = duration;
    }

    private DateTime toTime(String startPart) {
        DateTimeFormatter inputFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ").withZoneUTC();
        DateTimeZone oslo = DateTimeZone.forID("Europe/Oslo");
        DateTime dateTime = inputFormat.parseDateTime(startPart);
        return dateTime.withZone(oslo);
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public int getLength() {
        return length;
    }
}
