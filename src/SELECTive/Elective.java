package SELECTive;

public class Elective {
    //region Class specific enum declarations
    public enum MasterProgram {
        AFM,
        BIM,
        FI,
        GBS,
        HRM,
        MI,
        MM,
        OCC,
        SCM,
        SE,
        SM,
        INVLD
    }
    //endregion

    // region Private Constants
    private static final String invalidCourseID = "invC0iceNegativeWithdrawl";
    //endregion

    //region Private Properties
    private String courseID = invalidCourseID;
    private String electiveName = "";
    private int ects = 0;
    private MasterProgram program = MasterProgram.INVLD;
    private String[] keywords = null;
    private LectureTime[] classTimes = null;
    //endregion
}
