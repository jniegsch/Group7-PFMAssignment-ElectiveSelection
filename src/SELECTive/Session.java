package SELECTive;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Session {
    private static final int consoleCharWidth = 120;
    private static final String systemName = "SELECTive";

    public static final boolean systemPrintsErrors = true;

    //region Private Session Properties
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private static User sessionUser = null;
    //endregion

    //region Public Session Properties
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    /**
     * The location in the DB folder where the authentication file is stored
     */
    public final static String UPLoc = ".db/UPdb.txt";
    /**
     * The location in the DB folder where the Admin User Information file is stored
     */
    public final static String AdminInfoLoc = ".db/adminuinfo.txt";
    /**
     * The location in the DB folder where the Lecturer User Information file is stored
     */
    public final static String LecturerInfoLoc = ".db/lectureruinfo.txt";
    /**
     * The location in the DB folder where the Student User Information file is stored
     */
    public final static String StudentInfoLoc = ".db/studentuinfo.txt";
    /**
     * The location in the DB Folder where the Elective Information file is stored
     */
    public final static String ElectiveInfoLoc = ".db/electives.txt";
    /**
     * Checks if a file exists at the specified path. If it doesn't the function automatically creates the file.
     * <b>
     *     IMPORTANT!
     *     This function does take action if a file is not found, the {@code boolean} return is mainly there to check
     *     if file creation failed in the case it wasn't found. By calling this function a missing file will be created
     *     thus - in a sense - fixing the issue.
     * </b>
     * @param location {@code String} representing the file name/path relative to the current directory
     * @return {@code boolean} indicating if the file exists after completion of this function
     */
    public static boolean fileExists(String location) {
        String[] locSections = location.split("/");
        String file = locSections[locSections.length - 1];
        String[] folders = Arrays.copyOf(locSections, locSections.length - 1);

        //folders
        for (int i = 0; i < folders.length; i++) {
            File dir = new File(folders[i]);
            try{
                dir.mkdir();
            }
            catch(SecurityException se){
                printError("Session",
                        "fileExists",
                        "SecurityException",
                        "Could not create the folders");
                return false;
            }
        }

        File tmp = new File(file);
        if (tmp.exists()) return true;
        try {
            tmp.createNewFile();
        } catch (IOException ioe) {
            printError("Session",
                    "fileExists",
                    "IOException",
                    "Could not create the file...");
            return false;
        }
        return true;
    }
    //endregion

    //region Exit Codes
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public static final int ALL_GOOD_IN_THE_HOOD = 0;
    public static final int INITIAL_STATE_SETUP_FAILED_FATALITY = 1;
    public static final int BROKEN_INTERNAL_STATE_FATAL = 2;
    public static final int INTERNALLY_REQUIRED_FILE_CANNOT_EXIST = 3;
    public static final int USER_SAVING_FAILED_INCONSISTENT_INTERNAL_STATE = 4;
    public static final int REQUIRED_ALGORITHM_NOT_AVAILABLE_CANNOT_CONTINUE = 5;
    public static final int NO_AUTHENTICATION = 6;
    //endregion

    //region _MAIN
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public static void main(String[] args) {
        //TODO: The actual program...

        if (User.hasNoUsers()) createInitialAdmin();

        printTitle("Welcome", '*', false);
        println("Please login to use the system.");
        println("Info: see the code for the masterAdmin ;)");
        println(consoleLine('-'));
        println("");
        if (!loginToSession()) {
            printIssue("Incorrect Username/Password", "The username or password you have entered is incorrect.");
            System.exit(NO_AUTHENTICATION);
        }

        // DEBUG
        long[] a = {1};
        User[] retUsers = User.getUsers(a, UserType.STUDENT);

        for (int i = 0; i < retUsers.length; i++) {
            println(retUsers[i].toString());
        }

        //TODO: Program continues here....
    }
    //endregion

    //region Dashboard Login
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /**
     * Call to begin the login
     * @return {@code bool} indicating if the login was successful
     */
    private static boolean loginToSession() {
        if (sessionUser != null) return true;

        // Check if initial startup
        if (User.hasNoUsers()) {
            if ((sessionUser = createInitialAdmin()) != null) return true;
            printIssue("Fatal error occured", "A fatal error occured and the initial user could not be created. Please retry, and if the problem persists inform a rep. of the 'GoodEnoughCloseEnoughCoding corp.'");
            System.exit(INITIAL_STATE_SETUP_FAILED_FATALITY);
        }

        Scanner lgSelect = new Scanner(System.in);
        String uname = "";
        char[] pword = null;
        print("Username: ");
        if (lgSelect.hasNextLine()) {
            uname = lgSelect.nextLine();
        } else {
            printIssue("Invalid Username", "You have selected an invalid username. The system will exit.");
            System.exit(ALL_GOOD_IN_THE_HOOD);
        }
        try {
            if (System.console() != null) {
                pword = System.console().readPassword();
            } else {
                throw new IOError(new IOException("No console"));
            }
        } catch (IOError ioe) {
            printError("Session",
                    "loginToSession",
                    "IOError",
                    "An IO Error occurred when trying to read the password securely. Will use the standard Scanner instead");
            print("Password: ");
            if (lgSelect.hasNextLine()) {
                pword = lgSelect.nextLine().toCharArray();
            } else {
                printIssue("Invalid password", "You have selected an invalid password. The system will exit.");
                pword = null;
            }
        }
        if (pword == null) {
            printIssue("Invalid Username", "You have selected an invalid username. The system will exit.");
            System.exit(ALL_GOOD_IN_THE_HOOD);
        }

        sessionUser = new User();
        if (sessionUser.login(uname, pword)) return true;
        return false;
    }
    //endregion

    //region Initial Admin Setup
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /**
     * Begins the process to create an initial admin
     * @return the {@code Admin} created
     */
    private static Admin createInitialAdmin() {
        loadRootUser(); // only load root user if initial admin still needs to be created
        printIssue("No admin created.", "Please create an admin first. You will be guided through the setup.");
        Scanner adminCreationScanner = new Scanner(System.in);
        print("\nWhat username would you like: ");
        String uname = "";
        if (adminCreationScanner.hasNextLine()) {
            uname = adminCreationScanner.nextLine();
        } else {
            println("Using default.");
        }
        print("Passord: ");
        char[] pword;
        if (adminCreationScanner.hasNextLine()) {
            pword = adminCreationScanner.nextLine().toCharArray();
            print("Repeat password: ");
            if (adminCreationScanner.hasNextLine()) {
                if (!Arrays.equals(pword, adminCreationScanner.nextLine().toCharArray())) {
                    println("Account creation failed.");
                    return null;
                }
            } else {
                println("Account creation failed.");
                return null;
            }
        } else {
            println("Account creation failed.");
            return null;
        }
        return new Admin(rootUser.createNewUser(uname, pword, UserType.ADMIN));
    }
    //endregion

    // region General printing
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
        int leftToFill = 0;
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
        for (int i = 0; i < leftToFill; i += 2) System.out.print(c + " ");
        System.out.print("\n");
    }

    /**
     * Creates a string representing a line spanning the entire console width
     * @param c the {@code char} to use to create the line
     * @return  {@code String} representing the line to print
     */
    public static String consoleLine(char c) {
        String line = "";
        int i;
        for (i = 0; i < consoleCharWidth; i += 2) line += c + " ";
        if (i - 1 == consoleCharWidth) line += c;
        return line;
    }

    /**
     * Override of the {@code System.out.println()} method to ensure prints always have a width defined by the constant
     * {@code consoleCharWidth}
     * @param str the {@code String} to print
     */
    public static void println(String str) {
        String[] sections = str.split("\n");
        for (int i = 0; i < sections.length; i++) {
            while (sections[i].length() > consoleCharWidth) {
                String sub = sections[i].substring(0, consoleCharWidth - 1);
                System.out.println(sub);
                sections[i] = sections[i].substring(consoleCharWidth);
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

    //region Error and Issue handling
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
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

    //region Initial System and Internals
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    /**
     * The root user of the session
     */
    private static Admin rootUser;

    /**
     * Loads the root user to be used in the session
     * @return a {@code bool} indicating if the load was successful
     */
    private static boolean loadRootUser() {
        User u = new User();
        if (!u.login(User.rootUserName, User.rootUserPass)) return false;
        rootUser = new Admin(u);
        return true;
    }
    //endregion
}
