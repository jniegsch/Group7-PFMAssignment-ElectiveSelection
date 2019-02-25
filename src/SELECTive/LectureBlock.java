package SELECTive;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LectureBlock {

    //region Static Final Properties
    // The following sets the block start dates to the current academic year for simplicity. At a later stage this could
    // be expanded to adapt for a year basis (for this project though this simpler approach will be applied)

    /**
     * Defines the start date of a block in String format
     */
    private static final String sDates[] = {"2019-01-28", "2019-03-18", "2019-05-06"};
    /**
     * Defines the end date of a block in String format
     */
    private static final String eDates[] = {"2019-03-15", "2019-05-03", "2019-06-21"};
    //endregion

    //region Private properties
    private long id = -1;
    private Date startDate = null;
    private Date endDate = null;
    //endregion

    //region Getters
    public Date[] getDateRangeForBlock() {
        Date[] range = {startDate, endDate};
        return range;
    }

    public int getBlockNumber() {
        return (int)this.id + 1;
    }
    //endregion

    //region Constructor
    public LectureBlock(String strBlock) {
        String[] props = strBlock.split(lectureBlockSeparator);
        // length must be 3 if not proper or may be 1 then we try and create them, other wise return default
        if (props.length != 3 && props.length != 1) return;
        this.id = Long.parseLong(props[0]);
        if (props.length == 3) {
            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                this.startDate = df.parse(props[1]);
                this.endDate = df.parse(props[2]);
                return;
            } catch (ParseException pe) {
                InternalCore.printIssue("Could not parse the date",
                        "Failed to parse the date passed, will try to recreate them");
            }
        }

        Date[] calculatedDates = datesForBlock(this.id);
        if (calculatedDates == null) return;
        this.startDate = calculatedDates[0];
        this.endDate = calculatedDates[1];
    }

    public LectureBlock(long id) {
        this.id = id;
        Date[] calculatedDates = datesForBlock(this.id);
        if (calculatedDates == null) return;
        this.startDate = calculatedDates[0];
        this.endDate = calculatedDates[1];
    }
    //endregion

    //region Stringify
    private static final String lectureBlockSeparator = ",";
    public String toString() {
        StringBuilder representation = new StringBuilder();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        representation.append(this.id).append(lectureBlockSeparator);
        representation.append(df.format(this.startDate)).append(lectureBlockSeparator);
        representation.append(df.format(this.endDate));
        return representation.toString();
    }
    //endregion

    //region Date Calcs
    private static Date[] datesForBlock(long blockId) {
        // check block num
        if (blockId < 0 || blockId > 2) {
            InternalCore.printIssue("Invalid block id",
                    "A valid block id has to be 0, 1, or 2");
            return null;
        }

        Date[] retDates = new Date[2]; // Always will create 2 dates
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            retDates[0] = df.parse(sDates[(int)blockId]);
            retDates[1] = df.parse(eDates[(int)blockId]);
        } catch (ParseException pe) {
            InternalCore.printError("LectureBlock",
                    "datesForBlock",
                    "ParseException",
                    "Could not parse the date: " + pe.getMessage());
            return null;
        } finally {
            df = null;
        }
        return retDates;
    }
    //endregion
}
