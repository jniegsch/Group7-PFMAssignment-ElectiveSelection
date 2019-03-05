package SELECTive;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

final public class ICalendar {
    //region Static Declarations
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private static final String version = "VERSION:2.0";
    private static final String prodid = "PRODID:-//SELECTive//NONSGML v1.0/EN";
    private static final String beginCal = "BEGIN:VCALENDAR";
    private static final String endCal = "END:VCALENDAR";
    private static final String beginEvent = "BEGIN:VEVENT";
    private static final String endEvent = "END:VEVENT";
    private static final String organizer = "ORGANIZER;CN=RSM SELECTive Team:MAILTO:selecting.electing@rsm.nl";
    private static final String calScale = "CALSCALE:GREGORIAN";
    private static final String pubMEthod = "METHOD:PUBLISH";
    private static final String beginTZ = "BEGIN:VTIMEZONE";
    private static final String defTZ = "TZID:Europe/Amsterdam";
    private static final String begTZStan = "BEGIN:STANDARD";
    private static final String tzOffTo = "TZOFFSETTO:+0100";
    private static final String tzOffFr = "TZOFFSETFROM:+1000";
    private static final String endTZStan = "END:STANDARD";
    private static final String endTZ = "END:VTIMEZONE";
    private static final String defStart = "DTSTART:";
    private static final String defEnd = "DTEND:";
    private static final String tzDefStart = "DTSTART;TZID=Europe/Amsterdam:";
    private static final String txDefEnd = "DTEND;TZID=Europe/Amsterdam:";
    private static final String eventSummaryMac = "SUMMARY:";
    private static final String eventLocationMac = "LOCATION:";
    private static final String eventDescriptionMac = "DESCRIPTION:";
    private static final String eventPriorityMac = "PRIORITY:";

    private static final String lineEnd = "\\x0d\\x0a"; // \r\n
    private static final String internalOutput = "output/";
    //endregion

    //region Creation
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public static boolean createCalendarForElectives(Elective[] electives) {
        // Approximate downloads folder location
        String homeFolder = System.getProperty("user.home");
        // Check if the folder exists
        File targetFolder = new File(homeFolder + "/downloads/");
        if (!targetFolder.exists()) {
            InternalCore.printIssue("Downloads Folder doesn't exist", "It looks like the downloads folder isn't were we thought it was. We will try and play the file in your home folder here: " + homeFolder);
            targetFolder = new File(homeFolder + "/");
            if (!targetFolder.exists()) {
                InternalCore.printIssue("Home Folder doesn't exist", "It looks like the home folder isn't were we thought it was. We will put it in the internal output.");
                targetFolder = new File(internalOutput);
            }
        }

        String fileName = "selective_timetable.ics";
        try {
            File calFile = new File(targetFolder.toString() + fileName);

            if (!calFile.exists()) {
                // doesn't exist so make it
                calFile.createNewFile();
            } else {
                // file already exists, take the mac approach and add (x) to the file name
                // this is a lazy approach
                boolean notCreated = false;
                int x = 1;
                do {
                    calFile = new File(calFile.toString() + "(" + x + ")");
                    if (calFile.exists()) {
                        notCreated = true;
                    } else {
                        notCreated = false;
                    }
                } while (notCreated);
                calFile.createNewFile();
            }

            FileWriter writer = new FileWriter(calFile.getAbsoluteFile());
            // write iCal header & TZ
            StringBuilder headerAndTZ = new StringBuilder();
            headerAndTZ.append(beginCal).append(lineEnd).append(version).append(lineEnd).append(prodid).append(lineEnd);

            headerAndTZ.append(beginTZ).append(lineEnd).append(defTZ).append(lineEnd).append(begTZStan).append(lineEnd);
            headerAndTZ.append(tzOffTo).append(lineEnd).append(tzOffFr).append(lineEnd).append(endTZStan);
            headerAndTZ.append(lineEnd).append(endTZ).append(lineEnd);

            writer.write(headerAndTZ.toString());

            // write the events
            for (Elective elect : electives) {
                writer.write(beginEvent);
                writer.write(tzDefStart);
                // need date and time to be yyyyMMddTHHmm00
                String sdate = new SimpleDateFormat("yyyyMMdd").format(elect.getBlock().getDateRangeForBlock()[0]);
                for (LectureTime time : elect.getLectureTimes()) {

                }
            }

        } catch (IOException ioe) {
            // Could not create new file
        }

        return false;
    }

    private static String[] dateRanges(Date time, String sdate, LectureTime.Day day) {
        Calendar cal = Calendar.getInstance();
//        cal.set();
        return null;
    }
    //endregion

}
