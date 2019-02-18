package SELECTive;

import java.util.Date;

public class LectureBlock {
    //region Private properties
    private int id = 0;
    private Date startDate = null;
    private Date endDate = null;
    //endregion

    //region Getters
    //endregion

    //region Constructor
    public LectureBlock(int BlockID, Date start, Date end) {
        this.id = BlockID;
        this.startDate = start;
        this.endDate = end;
    }
    //endregion
}
