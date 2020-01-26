package no.javazone.cake.redux;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class SlotTimeFormatter {
    private String start;
    private String end;
    private int length;


    public SlotTimeFormatter(String formattedSlot) {
        String[] split = formattedSlot.split("\\+");
        String startPart = split[0];
        String endPart = split[1];

        LocalDateTime startTime = toLocalDate(startPart);
        LocalDateTime endTime = toLocalDate(endPart);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyMMdd HH:mm");
        this.start = startTime.format(dateTimeFormatter);
        this.end = endTime.format(dateTimeFormatter);

        length = (int) startTime.until(endTime, ChronoUnit.MINUTES);
    }


    private LocalDateTime toLocalDate(String datestring) {
        ZonedDateTime parsed = ZonedDateTime.parse(datestring);
        ZoneId oslo = ZoneId.of("Europe/Oslo");
        ZonedDateTime osloZone = parsed.withZoneSameInstant(oslo);
        LocalDateTime localDateTime = osloZone.toLocalDateTime();
        return localDateTime;
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
