package no.javazone.cake.redux.util;

import no.javazone.cake.redux.CommunicatorHelper;
import no.javazone.cake.redux.UserAccessType;
import no.javazone.cake.redux.sleepingpill.SleepingpillCommunicator;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class UpdateRoomSlotsFromCsv {
    private SleepingpillCommunicator sleepingpillCommunicator = new SleepingpillCommunicator();

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Use arg config csvfile");
            return;
        }
        System.setProperty("cake-redux-config-file",args[0]);
        UpdateRoomSlotsFromCsv updateRoomSlotsFromCsv = new UpdateRoomSlotsFromCsv();
        updateRoomSlotsFromCsv.doIt(args[1]);
    }

    private void doIt(String csvfile) {
        String filecontent = readFile(csvfile);
        String[] allLines = filecontent.split("\r\n");
        int numleft = allLines.length;
        for (String line : allLines) {
            System.out.println("Processing " + numleft);
            numleft--;
            List<String> cols = Arrays.asList(line.split(";"));
            if (cols.size() != 5) {
                System.err.println("Wrong line _: " +line);
                continue;
            }
            String id = cols.get(0);
            String room = cols.get(3);
            String slot = cols.get(4);
            LocalDateTime slotTime = startTimeFromCode(slot);
            if (slotTime == null) {
                System.err.println("Wrong slot_: " +line);
                continue;
            }
            sleepingpillCommunicator.updateRoom(id,room,UserAccessType.FULL);
            sleepingpillCommunicator.updateSlotTime(id,slotTime,UserAccessType.FULL);
        }
    }

    private static final LocalDate wed = LocalDate.of(2018,9,12);

    private static LocalDateTime startTimeFromCode(String code) {
        LocalDate day;
        switch (code.substring(0,3).toLowerCase()) {
            case "ons":
                day = wed;
                break;
            case "tor":
                day = wed.plusDays(1);
                break;
            default:
                return null;
        }
        int hour;
        int min;
        try {
            hour = Integer.parseInt(code.substring(4, 6));
            min = Integer.parseInt(code.substring(6));
        } catch (NumberFormatException e) {
            return null;
        }
        return day.atTime(hour,min);

    }

    private String readFile(String csvfile) {
        try {
            return CommunicatorHelper.toString(new FileInputStream(csvfile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
