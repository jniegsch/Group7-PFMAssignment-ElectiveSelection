package SELECTive;

import java.util.Scanner;

public class Elective {
    //region TO DEFINITELY REMOVE SCANNERS
    Scanner userInputInt = new Scanner(System.in);
    Scanner userInputString = new Scanner(System.in);
    //endregion

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
        COURSEID, // only equal filter
        ECTS, //TODO: check if electives have diff ects
        BLOCK, // equal or selection
        KEYWORDS, // any match, (single)
        AVAILABILITY // ical rep
    }
    //endregion

    // region Private Constants
    private static final String invalidCourseID = "invC0iceNegativeWithdrawl";
    //endregion

    //region Private Properties
    private long electiveId = -1;
    private String courseCode = invalidCourseID;
    private String electiveName = "";
    private int ects = 0;
    private MasterProgram program = MasterProgram.INVLD;
    private String[] keywords = null;
    private LectureTime[] classTimes = null;
    private LectureBlock block = null;
    //TODO: add test method?
    //endregion

    //region Constructors
    public Elective(String code, String name) {
        this.courseCode = code;
        this.electiveName = name;
    }

    public Elective(long id, String code, String name, int e, MasterProgram prog, String[] keys, LectureTime[] times, LectureBlock block) {
        this.electiveId = id;
        this.courseCode = code;
        this.electiveName = name;
        this.ects = e;
        this.program = prog;
        this.keywords = keys;
        this.classTimes = times;
        this.block = block;
    }
    //endregion

    //region Editing
    //TODO: Fill this in
    //endregion

    //region Static Access
    public static Elective[] getAllAvailableElectives() {
        String[][] electiveList = InternalCore.readInfoFile(SEObjectType.ELECTIVE, null);
        int electiveCount = electiveList.length;
        Elective[] allElectives = new Elective[electiveCount];
        for (int i = 0; i < electiveCount; i++) {
            Elective temp = new Elective(
                    Long.parseLong(electiveList[i][0]),
                    electiveList[i][1],
                    electiveList[i][2],
                    Integer.parseInt(electiveList[i][3]),
                    (electiveList[i][4] != "")? MasterProgram.valueOf(electiveList[i][4]) : MasterProgram.INVLD,
                    keywordsFromKeywordString(electiveList[i][5]),
                    LectureTime.generateLectureTimeArrayFromStringRepresentation(electiveList[i][6]),
                    new LectureBlock(electiveList[i][7])
            );
            allElectives[i] = temp;
        }

        return allElectives;
    }

    public static Elective[] filterOn(ElectiveFilterType filter, String argument) {
        return null; //TODO: this function, delete this call as well it only suppresses error warnings
    }
    //endregion

    //region Misc Lifters
    private static final String keywordStorageSeparator = "&";
    private String keywordString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.keywords.length; i++) builder.append(this.keywords[i]).append(keywordStorageSeparator);
        return builder.toString();
    }

    private static String[] keywordsFromKeywordString(String keywordStr) {
        return keywordStr.split(keywordStorageSeparator);
    }
    //endregion

    // This method allows the user to edit the elective
    public boolean editElective() {

        InternalCore.println("Which elective do you want to edit? Please enter the course code");
        String courseCode = userInputString.nextLine();

        String[][] electiveList = InternalCore.readInfoFile(SEObjectType.ELECTIVE, null);
        String editedElectiveID;
        for (int i = 0; i < electiveList.length; i++) {
            if (electiveList[i][1] == courseCode) {
                editedElectiveID = electiveList[i][1];
                break;
            }
        }

        InternalCore.println("" +
                "What do you want to edit? \n" +
                "(1) Elective name \n" +
                "(2) Program \n" +
                "(3) ECTS \n" +
                "(4) Keywords \n" +
                "(5) Class times \n" +
                "(6) Block");
        InternalCore.println(InternalCore.consoleLine('-'));
        int userChoice = InternalCore.getUserInput(Integer.class, "Please enter your choice (1, 2, 3, 4, 5 or 6): ").intValue();
        switch (userChoice) {
            case 1:
                editElectiveName();
                break;
            case 2:
                editProgram();
                break;
            case 3:
                editECTS();
                break;
            case 4:
                editKeywords();
                break;
            case 5:
                editClassTimes();
                break;
            case 6:
                editBlock();
                break;
        }

        String[] info = {
                Long.toString(this.electiveId),
                this.courseCode,
                this.electiveName,
                this.program.toString(),
                Integer.toString(this.ects),
                keywordString(),
                this.classTimes.toString(),
                this.block.toString()
        };

        return InternalCore.updateInfoFile(SEObjectType.ELECTIVE, Long.toString(this.electiveId), info);
    }

    // The file elective is structured this way:
    // Elective ID | Course Code | Name | Program | ECTS | Keywords | ClassTimes | Block

    // This method asks and returns what the user wants to do
    //TODO: - DEPRECATED -
    public int getUserChoice(){
        InternalCore.println("What do you want to edit?");
        InternalCore.println("(1) Elective name");
        InternalCore.println("(2) Program");
        InternalCore.println("(3) ECTS");
        InternalCore.println("(4) Keywords");
        InternalCore.println("(5) Class times");
        InternalCore.println("(6) Block");
        InternalCore.println(InternalCore.consoleLine('-'));
        InternalCore.print("Please enter your choice (1, 2, 3, 4, 5 or 6): ");

        return userInputInt.nextInt();
    }


    // This method asks for the change of the elective name and saves this in the file
    private boolean editElectiveName() {
        String newName = InternalCore.getUserInput(String.class, "What is the new name of this elective?");
        if (newName == null) return false;
        this.electiveName = newName;
        return true;
    }


    // This method asks for the change of the corresponding program and saves this in the file
    private boolean editProgram() {
        InternalCore.println("To which program does this elective belong?");
        int optCount = 1;
        for (MasterProgram p : MasterProgram.values()) {
            InternalCore.print("(" + optCount + ") " + p.toString());
            optCount++;
        }
        Integer selection = InternalCore.getUserInput(Integer.class, "The program: ");
        if (selection == null) return false;
        this.program = MasterProgram.values()[selection.intValue() - 1];
        return true;

    }

    //TODO: Add courseCode exists function

    // This method asks for the change of the elective ECTS number and saves this in the file
    private boolean editECTS() {
        Integer newECTS = InternalCore.getUserInput(Integer.class, "What is the new ECTS number of this elective?");
        if (newECTS == null) return false;
        this.ects = newECTS.intValue();
        return true;
    }


    // This method asks for the change of the keywords and saves it in the file
    private boolean editKeywords() {
        String newKeywords = InternalCore.getUserInput(String.class, "What are the keywords for this elective (separate each keyword using a ';')?");
        if (newKeywords == null) return false;
        // loop through and strip starting or ending whitespace
        for (int i = 0 ; i < this.keywords.length; i++) {
            char[] temp = this.keywords[i].toCharArray();
            if (temp[0] == ' ') this.keywords[i] = this.keywords[i].substring(1);
            if (temp[this.keywords[i].length() - 1] == ' ') this.keywords[i] = this.keywords[i].substring(0, this.keywords[i].length() - 2);
        }
        this.keywords = newKeywords.split(";");
        return true;
    }


    // This method asks for the change of the elective class times and saves this in the file
    private boolean editClassTimes() {
        String in = InternalCore.getUserInput(String.class,
                "What is the new class times of this elective?" +
                        "> Lesson days and times (separate the list with ';' using the format <week-day code> @ <hh:mm>): \n" +
                        "   codes: 1 = mon, 2 = tues, 3 = wed, 4 = thurs, 5 = fri, 6 = sat, 7 = sun");
        if (in == null) return false;
        in.replaceAll(" ", ""); // remove all whitespaces
        String[] dateTimes = in.split(";");
        this.classTimes = new LectureTime[dateTimes.length];
        for (int i = 0 ; i < dateTimes.length; i++) {
            String[] tmp = dateTimes[i].split("@");
            this.classTimes[i] = new LectureTime(tmp[1], Integer.parseInt(tmp[0]));
        }

        //TODO: get the new times in the info array
        return true;
    }


    // This method asks for the change of the elective block and saves this in the file
    public boolean editBlock() {
        InternalCore.println("Which block is this elective taught?");
        InternalCore.println("(1) Block 1");
        InternalCore.println("(2) Block 2");
        InternalCore.println("(3) Block 3");
        InternalCore.println(InternalCore.consoleLine('-'));
        String newBlock = InternalCore.getUserInput(String.class, "Your selection (1, 2, or 3:");
        if (newBlock == null) return false;
        this.block = new LectureBlock(Long.parseLong(newBlock) - 1);
        return true;
    }


//    public static void main(String[] args) {
//        // TODO Auto-generated method stub
//        editElective();
//    }
}
