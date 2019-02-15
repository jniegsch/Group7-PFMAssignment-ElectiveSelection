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
        this.weekDay = dayFromInt(day);
        this.time = LocalTime.parse(timeString);
    }
    //endregion

    //region Converter
    private Day dayFromInt(int x) {
        switch (x) {
            case 0:
                return Day.MONDAY;
            case 1:
                return Day.TUESDAY;
            case 2:
                return Day.WEDNESDAY;
            case 3:
                return Day.THURSDAY;
            case 4:
                return Day.FRIDAY;
            case 5:
                return Day.SATURDAY;
            case 6:
                return Day.SUNDAY;
            default:
                return Day.MONDAY;
        }
    }
    //endregion
}
