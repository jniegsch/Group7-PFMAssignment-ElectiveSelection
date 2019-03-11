package SELECTive;

import java.util.ArrayList;
import java.util.Arrays;

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

enum Day {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    INVLD
}

public class Elective {
    //region Class specific enum declarations
    public enum ElectiveFilterType {
        COURSEID,
        ECTS,
        BLOCK,
        KEYWORDS,
    }
    //endregion

    // region Private Constants
    public static final String invalidCourseID = "invC0iceNegativeWithdrawl";

    private static Elective[] electives = null;
    private static boolean hasValidElectives = false;
    private static boolean isLoading = false;
    //endregion

    //region Private Properties
    private long electiveId = -1;
    private String courseCode = invalidCourseID;
    private String electiveName = "";
    private int ects = 0;
    private MasterProgram program = MasterProgram.INVLD;
    private String[] keywords = null;
    private Day lectureDay = null;
    private int block = 0;
    private long lecturerId = -1;
    //endregion

    //region Constructors
    public Elective() {
        hasValidElectives = loadElectives();
    }

    public Elective(long id, String code, String name, int e, MasterProgram prog, String[] keys, Day day, int block, long lecturerId) {
        this();
        this.electiveId = id;
        this.courseCode = code;
        this.electiveName = name;
        this.ects = e;
        this.program = prog;
        this.keywords = keys;
        this.lectureDay = day;
        this.block = block;
        this.lecturerId = lecturerId;
    }
    //endregion

    //region Retrievers
    //This method returns a specific elective object, based on its course code
    public static Elective getElectiveWithCourseCode(String courseCode) {
        hasValidElectives = loadElectives();
        if (electives == null) return null;
        for (Elective elective : electives) {
            if (elective.courseCode.equals(courseCode)) return elective;
        }
        return null;
    }

    //This method returns those elective objects that are taught by one specific lecturer
    public static Elective[] getAllElectivesForLecturer(Lecturer lecturer) {
        hasValidElectives = loadElectives();
        ArrayList<Elective> validElectives = new ArrayList<>();
        if (electives == null) return null;
        for (Elective elective : electives) {
            if (elective.lecturerId == lecturer.getUserId()) validElectives.add(elective);
        }
        if (validElectives.size() == 0) return null;
        Elective[] electivesToReturn = new Elective[validElectives.size()];
        return validElectives.toArray(electivesToReturn);
    }

    //This method returns all elective objects
    public static Elective[] getAllElectives() {
        hasValidElectives = loadElectives();
        return electives;
    }
    //endregion

    //region Getters
    //This method allows access to certain private properties of an elective object
    public long getElectiveId() {
        return this.electiveId;
    }
    public String getCourseCode() {
        return this.courseCode;
    }
    public long getLecturerId() {
        return this.lecturerId;
    }
    public int getBlock() {
        return this.block;
    }
    //endregion

    //region Validators
    //This methods validates the access rights of a user
    public boolean mayNotAccessElective(User them) {
        if (them.isValidAdmin()) return false;
        if (them.isValidLecturer() && them.getUserId() == this.lecturerId) return false;
        InternalCore.printIssue("Insufficient Access Rights",
                "You do not have the required access rights to view details of this course.");
        return true;
    }
    //endregion

    //region Static I/O Access
    // STORED AS:
    // 1 | 2         | 3         | 4   | 5      | 6       | 7  | 8    | 9
    // ID;;CourseCode;;CourseName;;ECTS;;Program;;Keywords;;Day;;Block;;Lecturer
	
    // This method loads all elective entries from the file and create an object for every instance
    private static boolean loadElectives() {
        if (hasValidElectives) return true;
        if (isLoading) return false;
        isLoading = true;
        String[][] electiveList = InternalCore.readInfoFile(SEObjectType.ELECTIVE, null);
        if (electiveList == null) return true;
        int electiveCount = electiveList.length;
        electives = new Elective[electiveCount];
        for (int i = 0; i < electiveCount; i++) {
            Elective temp = new Elective(
                    Long.parseLong(electiveList[i][0]),
                    electiveList[i][1],
                    electiveList[i][2],
                    Integer.parseInt(electiveList[i][3]),
                    (!electiveList[i][4].equals("")) ? MasterProgram.valueOf(electiveList[i][4]) : MasterProgram.INVLD,
                    keywordsFromKeywordString(electiveList[i][5]),
                    (!electiveList[i][6].equals("")) ? Day.valueOf(electiveList[i][6]) : Day.INVLD,
                    Integer.parseInt(electiveList[i][7]),
                    Long.parseLong(electiveList[i][8])
            );
            electives[i] = temp;
        }
        return true;
    }

    //This method adds an elective
    private static void addElective(Elective elective) {
        if (alreadyHasLoaded(elective)) return;
        int currLength = 0;
        if (electives != null) {
            currLength = electives.length;
            electives = Arrays.copyOf(electives, currLength + 1);
        } else {
            electives = new Elective[1];
        }
        electives[currLength] = elective;
    }

    //This method checks whether an elective has already been loaded
    private static boolean alreadyHasLoaded(Elective elective) {
        hasValidElectives = loadElectives();
        if (electives == null) return false;
        for (Elective ele : electives) {
            if (ele.equals(elective)) return true;
        }
        return false;
    }
    //endregion

    //region Filters
    /**
     * Returns the electives given a certain filter and arguments. The possible arguments for the filters are:
     * <pre>
     *     > COURSEID
     *     args: a {@code String[]} of the course ids to look for
     *     > ECTS
     *     args: a {@code String[]} of ints of ects to consider
     *     > BLOCK
     *     args: a {@code String[]} of ints representing the blocks to search for
     *     > KEYWORDS
     *     args: a {@code String[]} of keyword strings
     * </pre>
     * @param filter    the {@code ElectiveFilterType} to apply
     * @param argument  a {@code String[]} of the arguments
     * @return {@code Elective[]} of all the electives that fit the filter
     */
    public static Elective[] filterOn(ElectiveFilterType filter, String[] argument) {
        switch (filter) {
            case COURSEID:
                return electivesFilteredOnCourseCode(argument);
            case ECTS:
                return electivesFilteredOnEcts(argument);
            case BLOCK:
                return electivesFilteredOnBlock(argument);
            case KEYWORDS:
                return electivesFilteredOnKeywords(argument);
            default:
                return null;
        }
    }

    /*
    IMPORTANT: comparing the array of Elective to the String array of the argument, the larger is most probably the
    elective. However, since we need to check on a property of elective not an entire elective a HashSet will not
    be beneficial when it comes to performance. Furthermore, the ArrayList (even if it is probably coded efficiently)
    will not save much time compared to a generic for loop approach. Changing String[] to ArrayList<String> will
    probably mitigate this performance increase.
     */
    //This method filters for electives based on course code
    private static Elective[] electivesFilteredOnCourseCode(String[] argument) {
        ArrayList<Elective> finalElectives = new ArrayList<>();
	if (electives == null) return null;
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

    //This method filters for electives based on ECTS value
    private static Elective[] electivesFilteredOnEcts(String[] argument) {
        ArrayList<Elective> finalElectives = new ArrayList<>();
	if (electives == null) return null;
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

    //This method filters for electives based on the block in which they are taught
    private static Elective[] electivesFilteredOnBlock(String[] argument) {
        ArrayList<Elective> finalElectives = new ArrayList<>();
	if (electives == null) return null;
        for (Elective elect : electives) {
            for (String arg : argument) {
                if (elect.block == Integer.parseInt(arg)) {
                    finalElectives.add(elect);
                    break;
                }
            }
        }
        Elective[] filteredElectives = new Elective[finalElectives.size()];
        return finalElectives.toArray(filteredElectives);
    }

    //This method filters for electives based on keywords
    private  static Elective[] electivesFilteredOnKeywords(String[] argument) {
        ArrayList<Elective> finalElectives = new ArrayList<>();
	if (electives == null) return null;
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
    //This method creates a standardized expression for an elective's keywords
    private static final String keywordStorageSeparator = "&";
    private String keywordString() {
        StringBuilder builder = new StringBuilder();
        for (String keyword : keywords) builder.append(keyword).append(keywordStorageSeparator);
        return builder.toString();
    }

    //This method returns all keywords in an array
    private static String[] keywordsFromKeywordString(String keywordStr) {
        return keywordStr.split(keywordStorageSeparator);
    }
    //endregion

    //region Stringify
    //This method turns values into a string
    public String toString() {
        int minSize = this.courseCode.length() + this.electiveName.length() + 50; //50 for good measure of the program, ects, and extras
        StringBuilder repBuilder = new StringBuilder();
        repBuilder.ensureCapacity(minSize);
        repBuilder.append(this.courseCode).append(": ").append(this.electiveName);
        repBuilder.append("\n    ").append("Offered by: ").append(this.program.toString());
        repBuilder.append("\n    ").append("For ").append(this.ects).append(" ECTS");
        return repBuilder.toString();
    }

    //This method returns the details of an elective (its block, class day, lecturer and keywords)
    public String view() {
        Lecturer lecturer = Lecturer.getLecturerWithId(this.lecturerId);
        // need to add keywords, block, and time so increase toString capa by 100
        StringBuilder viewBuilder = new StringBuilder(this.toString());
        viewBuilder.ensureCapacity(viewBuilder.length() + 100);
        viewBuilder.append("\n    ").append("Taught in Block ").append(this.block);
        viewBuilder.append("\n    ").append("Every ").append(InternalCore.capitalizeString(this.lectureDay.toString()));
        viewBuilder.append("\n    ").append("By: ").append((lecturer == null) ? "none defined" : lecturer.toString());
        viewBuilder.append("\n\n    Keywords:");
        for (String keyword : this.keywords) {
            viewBuilder.append("\n    > ").append(keyword);
        }
        return viewBuilder.toString();
    }
    //endregion

    //region Elective Editing
    //This method saves the updated properties to an elective object and the corresponding file
    public boolean saveElective(Admin who, boolean newElective) {
        if (!who.isValidAdmin()) {
            InternalCore.printIssue("Insufficient access rights", "");
            return false;
        }
        String[] info = {
                this.courseCode,
                this.electiveName,
                Integer.toString(this.ects),
                this.program.toString(),
                keywordString(),
                this.lectureDay.toString(),
                Long.toString(this.block),
                Long.toString(this.lecturerId)
        };

        boolean storeSuccessful = false, updateSuccessful = false;
        if (newElective) {
            this.electiveId = InternalCore.addEntryToInfoFile(SEObjectType.ELECTIVE, info);
            if (this.electiveId != -1) storeSuccessful = true;
        } else {
            storeSuccessful = InternalCore.updateInfoFile(SEObjectType.ELECTIVE, this.electiveId, info);
        }

        if (!storeSuccessful) return false;

        if (newElective) {
            addElective(this);
            return true;
        }

        for (Elective elect : electives) {
            if (elect.electiveId == this.electiveId) {
                elect.courseCode = this.courseCode;
                elect.electiveName = this.electiveName;
                elect.ects = this.ects;
                elect.program = this.program;
                elect.keywords = this.keywords;
                elect.lectureDay = this.lectureDay;
                elect.block = this.block;
                elect.lecturerId = this.lecturerId;
                updateSuccessful = true;
                break;
            }
        }

        if (!updateSuccessful) {
            InternalCore.printIssue("State Consistency Issue", "A fatal internal state consistency has occurred, will gracefully crash and burn...");
            System.exit(InternalCore.BROKEN_INTERNAL_STATE_FATAL);
        }
        return true;
    }

    //This method asks for the property of an elective object that is to be edited and calls the corresponding method
    public boolean edit(Admin who) {
        // Check access rights
        if (!who.isValidAdmin()) {
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
                "(5) Class day \n" +
                "(6) Block \n" +
                "(7) Lecturer");
        InternalCore.println(InternalCore.consoleLine('-'));
        Integer userChoice = InternalCore.getUserInput(Integer.class, "Please enter your choice (1, 2, etc.): ");
        if (userChoice == null) return false;
        switch (userChoice) {
            case 1:
                if (editElectiveName()) break;
                return false;
            case 2:
                if (editProgram()) break;
                return false;
            case 3:
                if (editECTS()) break;
                return false;
            case 4:
                if (editKeywords()) break;
                return false;
            case 5:
            	if (editClassDay()) break;
            	return false;
            case 6:
                if (editBlock()) break;
                return false;
            case 7:
                if (editLecturer()) break;
            	return false;
        }

        return saveElective(who, false);
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
        this.program = MasterProgram.values()[selection - 1];
        return true;

    }

    // This method asks for the change of the elective ECTS number and saves this in the file
    private boolean editECTS() {
        Integer newECTS = InternalCore.getUserInput(Integer.class, "What is the new ECTS number of this elective?");
        if (newECTS == null) return false;
        this.ects = newECTS;
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

     //This method asks for the change of the class time and saves this in the file
    private boolean editClassDay() {
    	InternalCore.println("On which day will this class be taught?");
    	String classDay = InternalCore.getUserInput(String.class, "" +
				"> Lesson day: \n" +
				"   codes: 1 = mon, 2 = tues, 3 = wed, 4 = thurs, 5 = fri");
		
    	if (classDay == null) return false;
    	if (Integer.parseInt(classDay) < 1 || Integer.parseInt(classDay) > 5) return false;
        this.lectureDay = Day.values()[Integer.parseInt(classDay) - 1];
    	return true;
	}

    // This method asks for the change of the elective block and saves this in the file
    private boolean editBlock() {
        InternalCore.println("Which block is this elective taught?");
        InternalCore.println("(3) Block 3");
        InternalCore.println("(4) Block 4");
        InternalCore.println("(5) Block 5");
        InternalCore.println(InternalCore.consoleLine('-'));
        String newBlock = InternalCore.getUserInput(String.class, "Your selection (3, 4 or 5:");
        if (newBlock == null) return false;
        this.block = Integer.parseInt(newBlock);
        return true;
    }
    
    // This method asks for the change of the lecturer id and saves this in the file
    private boolean editLecturer() {
        String newLecturer = InternalCore.getUserInput(String.class, "What is the new username of this elective's lecturer?");
        if (newLecturer == null) return false;
        Lecturer lecturer = Lecturer.getLecturerWithUsername(newLecturer);
        if (lecturer == null) return false;
        this.lecturerId = lecturer.getUserId();
        return true;
    }
}
