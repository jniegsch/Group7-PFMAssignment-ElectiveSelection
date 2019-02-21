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

    public enum ElectiveFilterType {
        COURSEID,
        ELECTIVEID,
        ECTS,
        BLOCK,
        TESTMETHOD,
        KEWORDS,
        AVAILABILITY
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
    private LectureBlock[] blocks = null;
    //TODO: add test method?
    //endregion

    //region Constructors
    public Elective(String id, String name) {
        this.courseID = id;
        this.electiveName = name;
    }

    public Elective(String id, String name, int e, MasterProgram prog, String[] keys, LectureTime[] times) {
        this.courseID = id;
        this.electiveName = name;
        this.ects = e;
        this.program = prog;
        this.keywords = keys;
        this.classTimes = times;
    }
    //endregion

    //region Editing
    //TODO: Fill this in
    //endregion

    //region Static Access
    public static Elective[] getAllAvailableElectives() {
        return null; //TODO: this function, delete this call as well it only suppresses error warnings
    }

    public static Elective[] filterOn(ElectiveFilterType filter, String argument) {
        return null; //TODO: this function, delete this call as well it only suppresses error warnings
    }
    //endregion

    //region Misc Lifters
    private static final String keywordStorageSeperator = "|";
    private String keywordString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.keywords.length; i++) builder.append(this.keywords[i]).append(keywordStorageSeperator);
        return builder.toString();
    }

    private String[] keywordsFromKeywordString(String keywordStr) {
        return keywordStr.split(keywordStorageSeperator);
    }
    //endregion
}
