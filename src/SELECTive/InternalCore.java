package SELECTive;

import java.io.*;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

/**
 * Enum defining the Info File Types
 */
enum SEObjectType {
    ADMIN_USER,
    STUDENT_USER,
    LECTURER_USER,
    ELECTIVE,
    STU_ELECT_RELATION,
    USER_AUTH,
    INTERNAL
}

public final class InternalCore {

    //region General System Constants
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    /**
     * Defines the constant width in chars
     */
    private static final int consoleCharWidth = 120;
    /**
     * Defines the name of the software for printing
     */
    private static final String systemName = "SELECTive";
    //endregion

    //region Exit Codes
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    /**
     * Defines the exit code for a fatal error when setting up the initial state
     */
    public static final int INITIAL_STATE_SETUP_FAILED_FATALITY = 1;
    /**
     * Defines the exit code when something has gone wrong in the internal state that cannot be fixed without a reset
     */
    public static final int BROKEN_INTERNAL_STATE_FATAL = 2;
    /**
     * Defines the exit code when something in general has gone that also requires a reset
     */
    public static final int FATAL_ERROR_RESET_REQUIRED = 3;
    /**
     * Defines the exit code when saving a user has failed due to an issue with the consistency of the internal state.
     * Does not necessarily require a reset.
     */
    public static final int USER_SAVING_FAILED_INCONSISTENT_INTERNAL_STATE = 4;
    /**
     * Defines the exit code when one of the required hashing algorithms is not available. In this case the system
     * cannot be used unless these are installed
     */
    public static final int REQUIRED_ALGORITHM_NOT_AVAILABLE_CANNOT_CONTINUE = 5;
    /**
     * Defines the exit code when the authentication has failed - reset is probably required
     */
    public static final int NO_AUTHENTICATION = 6;
    //endregion

    //region File Access Constants
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    /**
     * The location of the database folder (directory)
     */
    private final static String DBLoc = "database/";
    /**
     * The location in the DB folder where the authentication file is stored
     */
    private final static String UPLoc = DBLoc + "UPdb.txt";
    /**
     * The location in the DB folder where the Admin User Information file is stored
     * Admin ID; First Name; Last Name; Initial Middle Name; Username; Date of Birth
     */
    private final static String AdminInfoLoc = DBLoc + "adminuinfo.txt";
    /**
     * The location in the DB folder where the Lecturer User Information file is stored
     * Lecturer ID; First Name; Last Name; Initial Middle Name; Username; Date of Birth; Title
     */
    private final static String LecturerInfoLoc = DBLoc + "lectureruinfo.txt";
    /**
     * The location in the DB folder where the Student User Information file is stored
     * Student ID; First Name; Last Name; Initial Middle Name; Username; Date of Birth
     */
    private final static String StudentInfoLoc = DBLoc + "studentuinfo.txt";
    /**
     * The location in the DB Folder where the Elective Information file is stored
     * Elective ID; Course Code; Course Name; ECTS; Program; Keywords; Class Time; Block; Lecturer ID
     */
    private final static String ElectiveInfoLoc = DBLoc + "electiveinfo.txt";
    /**
     * The location in the DB Folder where the Student and Elective relation is stored
       * ID; Student ID; Block 3; Grade 3; Block 4; Grade 4; Block 5; Grade 5
     */
    private final static String StudentElectiveRelationLoc = DBLoc + "stuelectrel.txt";
    /**
     * The separator that MUST be used to store the different properties of an instance. This value must always be used
     * to separate the individual object properties. For example, any row must be something like this:
     * <pre>
     *     <object instance id> ;; <property 1> ;; <property 2> ;; <...> ;; <property n>
     * </pre>
     * Seeing as this class should solely read, update, or write this value is kept privately
     */
    private static final String infoSeparator = ";;";
    //endregion

    //region File Access Methods
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // Important: All file accessors that search will not use binarySearch since the arrays read are not guaranteed to
    // be sorted. Thus, applying binarySearch would require a sort first resulting in worst case of O(n log(n)), compared
    // to which a linear search in O(n) is better.

    /**
     * Checks if a file exists at the specified path. If it doesn't the function automatically creates the file.
     * <b>
     *     IMPORTANT!
     *     This function does take action if a file is not found, the {@code boolean} return is mainly there to check
     *     if file creation failed in the case it wasn't found. By calling this function a missing file will be created
     *     thus - in a sense - fixing the issue.
     * </b>
     * @param ot the {@code SEObjectType} defining the type of the object to which to add an entry
     * @return {@code boolean} indicating if the file exists after completion of this function
     */
    public static boolean fileDoesNotExist(SEObjectType ot) {
        String location = fileLocationForObjectType(ot);
        if (location == null) {
            printError("InternalCore",
                    "fileDoesNotExist",
                    "Could Not Find Myself",
                    "Something has really gone wrong and we can't find ourselves");
            System.exit(INITIAL_STATE_SETUP_FAILED_FATALITY);
        }
        String[] locSections = location.split("/");
        String[] folders = Arrays.copyOf(locSections, locSections.length - 1);

        //folders
        for (String folder : folders) {
            File dir = new File(folder);
            if (dir.exists()) continue;
            try{
                if (dir.mkdir()) continue;
                printError("InternalCore",
                        "fileDoesNotExist",
                        "Could Not Create File",
                        "Couldn't create the folder needed!");
                System.exit(INITIAL_STATE_SETUP_FAILED_FATALITY);
            }
            catch(SecurityException se){
                printError("InternalCore",
                        "fileDoesNotExist",
                        "SecurityException",
                        "Could not create the folders");
                return true;
            }
        }

        File tmp = new File(location);
        if (tmp.exists()) return false;
        try {
            if (!tmp.createNewFile()) {
                printError("InternalCore",
                        "fileDoesNotExist",
                        "Could Not Create File",
                        "Could not create the necessary new file!");
                System.exit(INITIAL_STATE_SETUP_FAILED_FATALITY);
            }
            if (location.equals(InternalStateLoc)) {
                // Create the internal state for all to be 1
                for (SEObjectType t : SEObjectType.values()) {
                    if (t == SEObjectType.INTERNAL || t == SEObjectType.USER_AUTH) continue;
                    saveLastIdForType(t, 0);
                }
            }
        } catch (IOException ioe) {
            printError("InternalCore",
                    "fileDoesNotExist",
                    "IOException",
                    "Could not create the file...");
            return true;
        }
        return false;
    }

    /**
     * Reads information from one of the DB files. Ensure you are passing a correct one, the method does not check
     * the validity and assumes the caller knows what they are accessing. Otherwise a new file (and possibly
     * directories) will be created, thus returning null
     * @param ot    the {@code SEObjectType} defining the type of the object to which to add an entry
     * @param ids   a list of ids to check for as {@code String}. Pass null if you want all records returned
     * @return  {@code String[][]} representing an array of row (or observation) arrays
     */
    public static String[][] readInfoFile(SEObjectType ot, String[] ids) {
        String locString = fileLocationForObjectType(ot);
        if (locString == null) return null;
        // not thread safe, but faster than StringBuffer -> in a single thread environment so all good
        StringBuilder userDump = new StringBuilder();
        try {
            if (fileDoesNotExist(ot)) {
                InternalCore.printError("InternalCore",
                        "readInfoFile",
                        "File IO Error",
                        "Apparently the requested file (" + locString + ") does not exist and cannot be created.");
                System.exit(InternalCore.BROKEN_INTERNAL_STATE_FATAL);
            }
            BufferedReader reader = new BufferedReader(new FileReader(locString));
            String currentLine;
            while((currentLine = reader.readLine()) != null) {
                String[] userInfo  = currentLine.split(infoSeparator);
                if (ids != null) {
                    boolean found = false;
                    for (String id : ids) {
                        if (id.equals(userInfo[0])) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) continue;
                }
                userDump.append(currentLine).append("\n");
            }
            reader.close();
        } catch (IOException ioe) {
            InternalCore.printError("InternalCore",
                    "readInfoFile",
                    "IOException",
                    ioe.getMessage());
            return null;
        }

        if (userDump.toString().equals("")) return null;
        String[] fileRows = userDump.toString().split("\n");
        String[][] info = new String[fileRows.length][];
        int j = 0;
        for (String fileRow : fileRows) {
            String[] row = fileRow.split(infoSeparator);
            if (row.length <= 1) continue;
            info[j++] = row;
        }

        if (info.length < 1) return null;
        return (info.length - 1 > j)? Arrays.copyOfRange(info, 0, j) : info;
    }

    /**
     * Updates a info file with the specified info. All info must be passed, even if only one property was changed
     * since the function lazily overwrites the specific row. Order is preserved though.
     * <pre>
     *     The function uses a StringBuilder to create the string since this is used anyways implicitly when using
     *     String concatination (i.e. String z = (String)x + (String)y). This way one entery can be easily exchanged.
     *     The entire 'buffer' (the StringBuilder object instance) is then converted to a String and directly writes
     *     this to a temporary file  using the writer
     * </pre>
     *
     * <b>IMPORTANT:</b> the info does not have to contain the id, the function checks if the first passed info element
     * is the same as the defined id. If not then it will write this first followed by the info.
     * @param ot        the {@code SEObjectType} defining the type of the object to which to add an entry
     * @param id        a {@code String} representing the id of the object to update
     * @param info      a {@code String[]} defining the info of the object to write to the file
     * @return a {@code boolean} indicating if the update occurred successful
     */
    public static boolean updateInfoFile(SEObjectType ot, long id, String[] info) {
        String locString = fileLocationForObjectType(ot);
        if (locString == null) return false;
        try {
            if (fileDoesNotExist(ot)) {
                printError("InternalCore",
                        "updateInfoFile",
                        "File IO Error",
                        "Apparently the requested file (" + locString + ") does not exist and cannot be created.");
                return false;
            }
            BufferedReader reader = new BufferedReader(new FileReader(locString));
            String currentLine;
            StringBuilder newBuffer = new StringBuilder();
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.split(infoSeparator)[0].equals(Long.toString(id))) {
                    newBuffer.append(id).append(infoSeparator);
                    for (int i = 0; i < info.length; i++) {
                        if (info[i].equals(""))
                            info[i] = " "; // make space instead of empty otherwise reading issues will occur
                        newBuffer.append(info[i]).append(infoSeparator);
                    }
                    newBuffer.append("\n");
                    continue;
                }
                newBuffer.append(currentLine);
                newBuffer.append("\n");
            }
            reader.close();

            BufferedWriter writer = new BufferedWriter(new FileWriter(locString));
            writer.write(newBuffer.toString());
            writer.close();
        } catch (IOException ioe) {
            printError("InternalCore",
                    "updateInfoFile",
                    "IOException",
                    "Something went wrong updating the user file");
            return false;
        }
        return true;
    }

    /**
     * Adds an entry to the file returning the id assigned to the new entry.
     *
     * <b>IMPORTANT:</b> do NOT specifiy an id in the info, the function will delegate ids as necessary!
     * @param ot        the {@code SEObjectType} defining the type of the object to which to add an entry
     * @param infoToAdd a {@code String[]} representing the information - without the id - to store
     * @return the assigned id as a {@code long}
     */
    public static long addEntryToInfoFile(SEObjectType ot, String[] infoToAdd) {
        String locString = fileLocationForObjectType(ot);
        if (locString == null) return -1;
        try {
            if (fileDoesNotExist(ot)) {
                printError("InternalCore",
                        "addEntryToInfoFile",
                        "File IO Error",
                        "Apparently the requested file (" + locString + ") does not exist and cannot be created.");
                return -1;
            }
            // save user to file
            StringBuilder str = new StringBuilder();
            BufferedWriter writer = new BufferedWriter(new FileWriter(locString, true));

            long id = nextIdForType(ot);
            if (ot != SEObjectType.USER_AUTH) str.append(id).append(infoSeparator);

            for (int i = 0; i < infoToAdd.length; i++) {
                if (infoToAdd[i].equals("")) infoToAdd[i] = " ";
                str.append(infoToAdd[i]).append(infoSeparator);
                writer.write(str.toString());
                str.delete(0, str.length());
            }
            writer.write("\n");
            writer.close();
            if (ot != SEObjectType.USER_AUTH) {
                if (!saveLastIdForType(ot, id)) {
                    printIssue("Fatal Error",
                            "Could not save the internal state, please reset the system with the root user.");
                    System.exit(FATAL_ERROR_RESET_REQUIRED);
                }
            }
            return id;
        } catch (IOException ioe) {
            printError("User",
                    "saveNewUser",
                    "UIException",
                    "Something went wrong writing to the user file");
            return -1;
        }
    }
    //endregion

    //region Object ID Tracking
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    /**
     * The location of the internal state tracker where the last given id is stored for each type
     */
    private final static String InternalStateLoc = DBLoc + ".internalstate.txt";

    /**
     * Returns the key to use based on admin = 0; student = 1; lecturer = 2; elective = 3; relation = 4;
     */
    private final static char[] TypeKeys = {'A', 'S', 'L', 'E', 'R'};

    /**
     * Defines the final size of the internal state in chars
     */
    private final static int totalBlocks = 40 * TypeKeys.length;

    /**
     * Gets the next available id for the specified type. Due to the small file size and manageability
     * writing is achieved with the FileWriter in order to not have too many class instance creations
     * @param ot {@code UserType} indicating the type for which to get the next given id
     * @return {@code long} the next available id for the {@code UserType}
     */
    private static long nextIdForType(SEObjectType ot) {
        if (ot == SEObjectType.USER_AUTH) return 0;
        if (fileDoesNotExist(SEObjectType.INTERNAL)) {
            printIssue("Accessing invalid UserType", "Tried get next id for DEFAULT");
            System.exit(BROKEN_INTERNAL_STATE_FATAL);
        }
        try {
            int selector = selectorForObjectType(ot);
            FileReader fReader = new FileReader(InternalStateLoc);
            char[] fileChars = new char[totalBlocks]; // enough to store 39 digit long ids for each type in total!
            if (fReader.read(fileChars) <= 0) return -1;
            int i = 0, j = 0;
            for ( ; i < fileChars.length; i++) {
                if (fileChars[i] == TypeKeys[selector]) j = i + 1;
                if (selector + 1 < TypeKeys.length) if (fileChars[i] == TypeKeys[(selector + 1)]) break;
                if (fileChars[i] == '\u0000') break; // '\u0000' denotes the null char
            }
            fReader.close();
            long lastId = Long.parseLong(new String(Arrays.copyOfRange(fileChars, j, i)));
            return ++lastId;
        } catch (FileNotFoundException e) {
            printError("User", "nextIdForType", "FileNotFoundException", e.getMessage());
        } catch (IOException e) {
            printError("User", "nextIdForType", "IOException", e.getMessage());
        }
        System.exit(BROKEN_INTERNAL_STATE_FATAL);
        return -1;
    }

    /**
     * Saves the last used id, so that this can be tracked internally.
     * @param ot     {@code SEObjectType} defining the type for which to store the id
     * @param id    {@code long} denoting the id to store
     * @return {@code bool} indicating if the save was successful
     */
    private static boolean saveLastIdForType(SEObjectType ot, long id) {
        if (fileDoesNotExist(SEObjectType.INTERNAL)) {
            printIssue("Accessing invalid UserType", "Tried to get next id for DEFAULT");
            System.exit(BROKEN_INTERNAL_STATE_FATAL);
        }
        // Data in the internal state file is stored in 40 char blocks (1 for the type and 39 for the last id)
        try {
            int offSetMod = selectorForObjectType(ot);
            BufferedReader fReader = new BufferedReader(new FileReader(InternalStateLoc));
            char[] toWrite = Long.toString(id).toCharArray();
            String nLine = fReader.readLine();
            char[] state = (nLine != null)? nLine.toCharArray() : new char[0];
            if (state.length < totalBlocks) {
                state = new char[totalBlocks];
                // fill with initial 0 and ids
                for (int i = 0; i < totalBlocks; i++) {
                    if (i % 40 == 0) {
                        state[i] = TypeKeys[i / 40];
                    } else {
                        state[i] = '0';
                    }
                }
            }
            for (int i = 0; i < toWrite.length; i++) {
                state[offSetMod * 40 + (40 - toWrite.length) + i] = toWrite[i];
            }
            fReader.close();
            FileWriter fWriter = new FileWriter(InternalStateLoc);
            fWriter.write(state);
            fWriter.flush();
            fWriter.close();
            return true;
        } catch (FileNotFoundException e) {
            printError("User", "nextIdForType", "FileNotFoundException", e.getMessage());
            System.exit(FATAL_ERROR_RESET_REQUIRED);
        } catch (IOException e) {
            printError("User", "nextIdForType", "IOException", e.getMessage());
            System.exit(FATAL_ERROR_RESET_REQUIRED);
        }
        return false;
    }

    /**
     * Method to evaluate an SEObjectType to its respective file location
     *
     * @param ot the {@code SEObjectType} for which to return the file location
     * @return {@code String} representing the file location
     */
    public static String fileLocationForObjectType(SEObjectType ot) {
        switch (ot) {
            case ADMIN_USER: return AdminInfoLoc;
            case STUDENT_USER: return StudentInfoLoc;
            case LECTURER_USER: return LecturerInfoLoc;
            case ELECTIVE: return ElectiveInfoLoc;
            case STU_ELECT_RELATION: return StudentElectiveRelationLoc;
            case USER_AUTH: return UPLoc;
            case INTERNAL: return InternalStateLoc;
            default: return null;
        }
    }

    /**
     * Method to return an integer to use on the {@code TypeKeys} array to find the selector used in the internal state
     * @param ot    the {@code SEObjectType} for which to return the file location
     * @return the {@code int} to use to retrieve the correct type key
     */
    private static int selectorForObjectType(SEObjectType ot) {
        switch (ot) {
            case ADMIN_USER: return 0;
            case STUDENT_USER: return 1;
            case LECTURER_USER: return 2;
            case ELECTIVE: return 3;
            case STU_ELECT_RELATION: return 4;
            default: return -1;
        }
    }
    //endregion

    //region Getting User Input
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    /**
     * Scanner instance for the entire session
     */
    private static Scanner inputScanner = null;

    /**
     * Method to retrieve input from a user. The method ensures proper printing as well as allowing the user 5 tries
     * to enter the expected value. Furthermore, the method ensures the scanner is first checked before reading from it.
     * @param type      The class of the type you would like to have returned
     * @param prompt    The prompt to print for the user
     * @param <T>       The generic type
     * @return The generic return casted to the type requested
     */
    public static <T> T getUserInput(Class<T> type, String prompt) {
        final int maxTries = 5;

        int tryCount = 0;
        if (inputScanner == null) inputScanner = new Scanner(System.in);
        inputScanner.reset();
        inputScanner.useLocale(Locale.US);
        while (tryCount < maxTries) {
            println("[user input] " + stripWhitespace(prompt));

            if (type.equals(Integer.class)) {
                if (inputScanner.hasNextInt()) {
                    Integer x = inputScanner.nextInt();
                    if (inputScanner.hasNextLine()) inputScanner.nextLine(); // Used due to possible hanging lines, which causes the next `nextLine` to return ""
                    return type.cast(x);
                }
                InternalCore.printIssue("Wrong input type.", "An integer input was expected but none received");
                tryCount++;
                if (inputScanner.hasNextLine()) inputScanner.nextLine();
                continue;
            }

            if (type.equals(String.class)) {
                if (inputScanner.hasNextLine()) {
                    String x = inputScanner.nextLine();
                    return type.cast(x);
                }
                InternalCore.printIssue("Wrong input type.", "A string input was expected but none received");
                tryCount++;
                continue;
            }

            if (type.equals(Long.class)) {
                if (inputScanner.hasNextLong()) {
                    Long x = inputScanner.nextLong();
                    if (inputScanner.hasNextLine()) inputScanner.nextLine();
                    return type.cast(x);
                }
                InternalCore.printIssue("Wrong input type.", "A long input was expected but none received");
                tryCount++;
                if (inputScanner.hasNextLine()) inputScanner.nextLine();
                continue;
            }

            if (type.equals(Double.class)) {
                if (inputScanner.hasNextDouble()) {
                    Double x = inputScanner.nextDouble();
                    if (inputScanner.hasNextLine()) inputScanner.nextLine();
                    return type.cast(x);
                }
                InternalCore.printIssue("Wrong input type.", "A double input was expected but none received");
                tryCount++;
                if (inputScanner.hasNextLine()) inputScanner.nextLine();
                continue;
            }

            InternalCore.printError(
                    "InternalCore",
                    "getUserInput()",
                    "InvalidArgument",
                    "The type requested (" + type + ") is invalid and not supported by the system!"
            );
            return null;
        }
        return null;
    }

    /**
     * This function should be called at the end of a session to properly close the scanner
     */
    public static void cleanup() {
        inputScanner.close();
        inputScanner = null;
    }
    //endregion

    //region General printing
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /**
     * Prints a title on the session without the system name
     * @param title the {@code String} representing the title to print
     * @param c     a {@code char} from which to print the line
     */
    public static void printTitleNoName(String title, char c) {
        printTitle(title, c, true);
    }

    /**
     * Prints a title on the session with the system name
     * @param title the {@code String} representing the title to print
     * @param c     a {@code char} from which to print the line
     */
    public static void printTitle(String title, char c) {
        printTitle(title, c, false);
    }

    /**
     * The underlying function for the 2 public functions for printing titles
     * check: printTitle(String, char)
     * check: printTitleNoName(String, char)
     * @param title         the {@code String} representing the title to print
     * @param c             a {@code char} from which to print the line
     * @param supressName   a {@code bool} indicating if the system name should be printed
     */
    private static void printTitle(String title, char c, boolean supressName) {
        if (title.length() > consoleCharWidth - 9) return;
        String beginning = c + " " + c + " ";
        int setWidth = beginning.length() + ((supressName)? 0 : systemName.length() + 2); // +2 for the ': ' that is appended later
        int leftToFill;
        System.out.print(beginning + ((supressName)? "" : systemName + ": "));
        if (title.length() + setWidth > consoleCharWidth - 3) {
            String nLine = "\n      " + title;
            System.out.print(nLine);
            leftToFill = consoleCharWidth - nLine.length() + 2; // -2 for the '\n'
        } else {
            String nLine = beginning + ((supressName)? "" : systemName + ": ") + title;
            System.out.print(title + " ");
            leftToFill = consoleCharWidth - nLine.length();
        }
        if (leftToFill % 2 == 0) {
            System.out.print(" ");
            leftToFill--;
        }
        // start at 2 to truncate the last 2 being overwritten
        for (int i = 2; i < leftToFill; i += 2) System.out.print(c + " ");
        System.out.print("\n");
    }

    /**
     * Creates a string representing a line spanning the entire console width
     * @param c the {@code char} to use to create the line
     * @return  {@code String} representing the line to print
     */
    public static String consoleLine(char c) {
        StringBuilder line = new StringBuilder();
        int i;
        for (i = 0; i < consoleCharWidth; i += 2) line.append(c).append(" ");
        if (i - 1 == consoleCharWidth) line.append(c);
        return line.toString();
    }

    /**
     * Prints an empty line - supplied to ensure there is no need to mix around with the printing of the InternalCore
     * and the printing provided by the System
     */
    public static void println() {
        System.out.println();
    }

    /**
     * Override of the {@code System.out.println()} method to ensure prints always have a width defined by the constant
     * {@code consoleCharWidth}
     * @param str the {@code String} to print
     */
    public static void println(String str) {
        if (str == null) return;
        String[] sections = str.split("\n");
        for (int i = 0; i < sections.length; i++) {
            while (sections[i].length() > consoleCharWidth) {
                String[] sub = sections[i].split(" ");
                int charsPrinted = 0, j = 0, nextLength;
                do {
                    nextLength = (j + 1 < sub.length)? sub[j].length() : 0;
                    charsPrinted += sub[j].length() + " ".length();
                    System.out.print(sub[j] + " ");
                    j++;
                } while (charsPrinted + nextLength < consoleCharWidth);
                System.out.println();
                sections[i] = sections[i].substring(charsPrinted);
            }
            System.out.println(sections[i]);
        }
    }

    /**
     * Override of the {@code System.out.print()} method to ensure prints always have a width defined by the constant
     * {@code consoleCharWidth}
     * @param str the {@code String} to print
     */
    public static void print(String str) {
        String[] sections = str.split("\n");
        for (int i = 0; i < sections.length; i++) {
            while (sections[i].length() > consoleCharWidth) {
                String sub = sections[i].substring(0, consoleCharWidth - 1);
                System.out.print(sub + "\n");
                sections[i] = sections[i].substring(consoleCharWidth);
            }
            if (i == sections.length - 1) {
                System.out.print(sections[i]);
            } else {
                System.out.print(sections[i] + "\n");
            }
        }
    }
    //endregion

    //region Misc
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    /**
     * Trips the whitespace that might be at the beginning or end of a string if a user does not use the exact separator
     * + spacing as expected
     * @param str the {@code String} to strip the whitespace of
     * @return the {@code String} with whitespace removed at the beginning and the end
     */
    public static String stripWhitespace(String str) {
        char[] s = str.toCharArray();
        int b = 0, e = 0;
        for (int i = 0; i < s.length; i++) {
            if (s[i] == ' ') continue;
            b = i;
            break;
        }
        for (int i = s.length - 1; i >= 0; i--) {
            if (s[i] == ' ') continue;
            e = i + 1;
            break;
        }
        return String.copyValueOf(Arrays.copyOfRange(s, b, e));
    }

    /**
     * Applies the {@code stripWhitespace(String str)} to a {@code String} array
     * @param strArr the {@code String[]} representing the {@code String}s to strip
     * @return a {@code String[]} where all {@code String} of the array have been stripped
     */
    public static String[] stripWhitespaceOfArray(String[] strArr) {
        for (int i = 0; i < strArr.length; i++) strArr[i] = stripWhitespace(strArr[i]);
        return strArr;
    }

    /**
     * Capitalizes the passed string and removes all '_' for properly printing the Title enum. Furthermore, the method
     * can be used to also ensure that all names are actually capitalized when printing
     * @param str   The {@code String} to capitalize
     * @return The {@code String} of the capitalized input
     */
    public static String capitalizeString(String str) {
        if (str == null || str.equals("") || str.equals(" ")) return "";
        StringBuilder capitalizedString = new StringBuilder();
        char[] strArray = str.toLowerCase().toCharArray();
        for (int i = 0; i < strArray.length; i++) if (strArray[i] == '_') strArray[i] = ' ';
        capitalizedString.append(Character.toString(strArray[0]).toUpperCase());
        capitalizedString.append(String.valueOf(Arrays.copyOfRange(strArray, 1, strArray.length)));
        return capitalizedString.toString();
    }
    //endregion

    //region Error and Issue handling
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    /**
     * An internal boolean that can be flipped in code to easily suppress or allow for error printing
     */
    private static final boolean systemPrintsErrors = false;

    /**
     * Print an error
     * @param className {@code String} defining the class in which the error occurred
     * @param function  {@code String} defining in which function the error occurred for better debugging
     * @param type      {@code String} defining the type of error usually used to define the exception that was caught
     * @param message   {@code String} the additional message to print in order to further clarify things
     */
    public static void printError(String className, String function, String type, String message) {
        if (systemPrintsErrors) {
            printTitle("Error", '-', true);
            println("> An " + type + " error occurred in " + className + ": " + function + "\n" +
                    "    " + message + "\n");
            println(consoleLine('-'));
            print("\n");
        }
    }

    /**
     * Prints an issue or warning for the user. It is mainly meant to be informative to the user telling them about an
     * issue
     * @param title     {@code String} the title of the issue to be shown
     * @param message   {@code String} the message further describing the issue along with potential actions the user
     *                                could take and or actions the system will take on behalf of the user due to
     *                                certian conditions
     */
    public static void printIssue(String title, String message) {
        System.out.println("" +
                ">> Warning: " + title);
        if (!message.equals("")) System.out.println("" +
                "    " + message + "\n\n");
    }
    //endregion
}
