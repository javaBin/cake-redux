package no.javazone.cake.redux;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class SlotTimeFormatter {
    private String start;
    private String end;
    private int length;

    public SlotTimeFormatter(String formattedSlot) {
        String[] split = formattedSlot.split("\\+");
        String startPart = split[0];
        String endPart = split[1];

        DateTimeFormatter inputFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        DateTimeFormatter outputFormat = DateTimeFormat.forPattern("yyMMdd HH:mm");

        DateTime startTime = inputFormat.parseDateTime(startPart);
        start = outputFormat.print(startTime);
        DateTime endTime = inputFormat.parseDateTime(endPart);
        end = outputFormat.print(endTime);

        Period period = new Period(startTime, endTime);
        length = period.toStandardMinutes().getMinutes();
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
