package SELECTive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

enum MasterProgram {
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

public class Elective {
    //region TO DEFINITELY REMOVE SCANNERS
    Scanner userInputInt = new Scanner(System.in);
    Scanner userInputString = new Scanner(System.in);
    //endregion

    //region Class specific enum declarations
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
    private long lecturerId = -1;
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

    //TODO: write constructor to just take course code
    public Elective(String courseCode) {

    }
    //endregion

    //region Getters
    public String getElectiveName() {
        return this.electiveName;
    }
    public String getCourseCode() {
        return this.courseCode;
    }
    public LectureBlock getElectiveBlock() {
        return this.block;
    }
    public long getLecturerId() {
        return this.lecturerId;
    }
    public LectureTime[] getLectureTimes() {
        return this.classTimes;
    }
    public LectureBlock getBlock() {
        return this.block;
    }

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

    /**
     * Returns the electives given a certain filter and arguments. The possible arguments for the filters are:
     * <pre>
     *     > COURSEID
     *     args: a {@code String[]} of the course ids to look for
     *     > ECTS
     *     args: a {@code String[]} of ints of ects to consider
     *     > BLOCK
     *     args: a {@code String[]} of ints representing the blocks to search for
     *     > AVAILABILITY
     *     args: a {@code String[]} where only the first is the file path to an ical file
     *     > KEYWORDS
     *     args: a {@code String[]} of keyword strings
     * </pre>
     * @param filter
     * @param argument
     * @return
     */
    public static Elective[] filterOn(ElectiveFilterType filter, String[] argument) {
        Elective[] allElectives = getAllAvailableElectives();

        switch (filter) {
            case COURSEID:
                return electivesFilteredOnCourseCode(allElectives, argument);
            case ECTS:
                return electivesFilteredOnEcts(allElectives, argument);
            case BLOCK:
                return electivesFilteredOnBlock(allElectives, argument);
            case KEYWORDS:
                return electivesFilteredOnKeywords(allElectives, argument);
            case AVAILABILITY:
                return null;
            default:
                return null;
        }
    }

    /*
    IMPORTANT: comparing the array of Elective to the String array of the argument, the larger is most probably the
    elective. However, since we need to check on a property of elective not an entire elective a HashSet will not
    be beneficial when it comes to performance. Furthermore, the ArrayList (even if it is probably coded efficiently)
    will not save much time compared to a generic foor loop approach. Changing String[] to ArrayList<String> will
    probably mitigate this performance increase.
     */
    //TODO: Add parrellism for filters and file reading?
    private static Elective[] electivesFilteredOnCourseCode(Elective[] electives, String[] argument) {
        ArrayList<Elective> finalElectives = new ArrayList<Elective>();
        for (Elective elect : electives) {
            for (String arg : argument) {
                if (elect.courseCode.equals(arg)) {
                    finalElectives.add(elect);
                    break;
                }
            }
        }
        Elective[] filteredElectives = new Elective[finalElectives.size()];
        return finalElectives.toArray(filteredElectives);
    }

    private static Elective[] electivesFilteredOnEcts(Elective[] electives, String[] argument) {
        ArrayList<Elective> finalElectives = new ArrayList<>();
        for (Elective elect : electives) {
            for (String arg : argument) {
                if (elect.ects == Integer.parseInt(arg)) {
                    finalElectives.add(elect);
                    break;
                }
            }
        }
        Elective[] filteredElectives = new Elective[finalElectives.size()];
        return finalElectives.toArray(filteredElectives);
    }

    private static Elective[] electivesFilteredOnBlock(Elective[] electives, String[] argument) {
        ArrayList<Elective> finalElectives = new ArrayList<>();
        for (Elective elect : electives) {
            for (String arg : argument) {
                if (elect.block.getBlockNumber() == Integer.parseInt(arg)) {
                    finalElectives.add(elect);
                    break;
                }
            }
        }
        Elective[] filteredElectives = new Elective[finalElectives.size()];
        return finalElectives.toArray(filteredElectives);
    }

    private  static Elective[] electivesFilteredOnKeywords(Elective[] electives, String[] argument) {
        ArrayList<Elective> finalElectives = new ArrayList<>();
        for (Elective elect : electives) {
            boolean earlyExit = false;
            for (String arg : argument) {
                for (String key : elect.keywords) {
                    if (key.equals(arg)) {
                        finalElectives.add(elect);
                        earlyExit = true;
                        break;
                    }
                }
                if (earlyExit) {
                    break;
                }
            }
        }
        Elective[] filteredElectives = new Elective[finalElectives.size()];
        return finalElectives.toArray(filteredElectives);
    }
    //endregion

    //region Misc Lifters
    private static final String keywordStorageSeparator = "&";
    private String keywordString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.keywords.length; i++) builder.append(this.keywords[i]).append(keywordStorageSeparator);
        return builder.toString();
    }

    public static String[] keywordsFromKeywordString(String keywordStr) {
        return keywordStr.split(keywordStorageSeparator);
    }
    //endregion

    public boolean saveElective(Admin who, boolean newElective) {
        if (who.getUserType() != UserType.ADMIN) {
            InternalCore.printIssue("Insufficient access rights", "");
            return false;
        }
        String[] info = {
                Long.toString(this.electiveId),
                this.courseCode,
                this.electiveName,
                Integer.toString(this.ects),
                this.program.toString(),
                keywordString(),
                LectureTime.generateLectureTimeArrayStringRepresentation(this.classTimes),
                this.block.toString()
        };

        if (newElective) {
            this.electiveId = InternalCore.addEntryToInfoFile(SEObjectType.ELECTIVE, Arrays.copyOfRange(info, 1, info.length));
            if (this.electiveId != -1) return true;
            return false;
        } else {
            return InternalCore.updateInfoFile(SEObjectType.ELECTIVE, Long.toString(this.electiveId), info);
        }
    }

    // This method allows the user to edit the elective
    public static boolean editElective(Admin who) {
        // Check access rights
        UserType requestorsType = who.getUserType();
        if (requestorsType != UserType.ADMIN) {
            InternalCore.printIssue("Invalid Access Rights",
                    "You do not have the correct access privileges to edit an elective.");
            return false;
        }

        // Get course to edit
        String courseCode = InternalCore.getUserInput(String.class,
                "Which elective do you want to edit? Please enter the course code");
        String[][] electiveList = InternalCore.readInfoFile(SEObjectType.ELECTIVE, null);
        int toEditElective = -1;
        for (int i = 0; i < electiveList.length; i++) {
            if (electiveList[i][1].equals(courseCode)) {
                toEditElective = i;
                break;
            }
        }

        // Check if course exists
        if (toEditElective == -1) {
            String choice = InternalCore.getUserInput(String.class,
                    "It seems like the course you want to edit doesn't exist. Would you like to create it? (Y/n)");
            if (choice.toLowerCase().equals("y")) {
                Admin adminWho = (Admin) who;
                return adminWho.addElective(courseCode);
            }
            return false;
        }

        Elective toEdit = new Elective(
                Long.parseLong(electiveList[toEditElective][0]),
                electiveList[toEditElective][1],
                electiveList[toEditElective][2],
                Integer.parseInt(electiveList[toEditElective][3]),
                MasterProgram.valueOf(electiveList[toEditElective][4]),
                keywordsFromKeywordString(electiveList[toEditElective][5]),
                LectureTime.generateLectureTimeArrayFromStringRepresentation(electiveList[toEditElective][6]),
                (new LectureBlock(electiveList[toEditElective][7]))
        );
        return toEdit.edit(who);
    }

    public boolean edit(Admin who) {
        // Check access rights
        UserType requestorsType = who.getUserType();
        if (requestorsType != UserType.ADMIN) {
            InternalCore.printIssue("Invalid Access Rights",
                    "You do not have the correct access privileges to edit an elective.");
            return false;
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
        Integer userChoice = InternalCore.getUserInput(Integer.class, "Please enter your choice (1, 2, 3, 4, 5 or 6): ");
        if (userChoice == null) return false;
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

        return saveElective(who, false);
    }

    // The file elective is structured this way:
    // Elective ID | Course Code | Name | ECTS | Program | Keywords | ClassTimes | Block

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
    private boolean editBlock() {
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

    //region Register For Elective
    public boolean registerForElective(Student stu) {
        return false; //TODO
    }

    public boolean deregisterForElective(Student stu) {
        return false; //TODO
    }
    //endregion
}
