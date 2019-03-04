package SELECTive;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;

public class LectureTime {

    public enum Day {
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY,
        SUNDAY
    }

    private LocalTime time = LocalTime.MIDNIGHT;
    private Day weekDay = Day.MONDAY;

    //region Constructor
    public LectureTime(String timeString, int day) {
        this.weekDay = Day.values()[day];
        this.time = LocalTime.parse(timeString);
    }

    public LectureTime(String savedTimeRep) {
        // check if length is not 2. If it isn't, it is invalid so return as default
        String[] rep = savedTimeRep.split("@");
        if (rep.length != 2) return;
        this.weekDay = Day.valueOf(rep[0]);
        this.time = LocalTime.parse(rep[1]);
    }
    //endregion

    //region LectureTime Array handling
    private static final String lectureTimeStringSeparator = ",";
    public static LectureTime[] generateLectureTimeArrayFromStringRepresentation(String rep) {
        String[] times = rep.split(lectureTimeStringSeparator);
        int timeAmount = times.length;
        LectureTime[] ret = new LectureTime[timeAmount];
        for (int i = 0; i < timeAmount; i++) {
            ret[i] = new LectureTime(times[i]);
        }
        return ret;
    }

    public static String generateLectureTimeArrayStringRepresentation(LectureTime[] times) {
        StringBuilder representation = new StringBuilder();
        for (int i = 0; i < times.length; i++) {
            representation.append(times[i].toString());
            representation.append(lectureTimeStringSeparator);
        }
        return representation.toString();
    }
    //endregion

    //region Stringify
    public String toString() {
        StringBuilder objectRepresentation = new StringBuilder();
        objectRepresentation.append(weekDay.toString()).append("@").append(time.toString());
        return objectRepresentation.toString();
    }
    //endregion
}
