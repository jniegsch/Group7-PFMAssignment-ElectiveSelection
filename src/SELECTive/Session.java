package SELECTive;

import java.io.Console;
import java.io.IOError;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Scanner;

public class Session {
    private static final int consoleCharWidth = 120;
    private static final String systemName = "SELECTive";

    public static final boolean systemPrintsErrors = true;

    //region Session Properties
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private static User sessionUser = null;
    //endregion

    //region Exit Codes
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    protected static final int ALL_GOOD_IN_THE_HOOD = 0;
    protected static final int INITIAL_STATE_SETUP_FAILED_FATALITY = 1;
    protected static final int BROKEN_INTERNAL_STATE_FATAL = 2;
    protected static final int USER_SAVING_FAILED_INCONSISTENT_INTERNAL_STATE = 3;
    protected static final int REQUIRED_ALGORITHM_NOT_AVAILABLE_CANNOT_CONTINUE = 4;
    protected static final int NO_AUTHENTICATION = 5;
    //endregion

    //region _MAIN
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public static void main(String[] args) {
        //TODO: The actual program...

        if (User.hasNoUsers(true)) createRootUser();
        loadRootUser();

        printTitle("Welcome", '*', false);
        println("Please login to use the system.");
        println("Info: see the code for the masterAdmin ;)");
        println(consoleLine('-'));
        println("");
        if (!loginToSession()) {
            printIssue("Incorrect Username/Password", "The username or password you have entered is incorrect.");
            System.exit(NO_AUTHENTICATION);
        }

    }
    //endregion

    //region Dashboard Login
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public static boolean loginToSession() {
        if (sessionUser != null) return true;

        // Check if initial startup
        if (User.hasNoUsers(false)) {
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
    private static Admin createInitialAdmin() {
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
        return new Admin(new User().createNewUser(uname, pword, UserType.ADMIN, rootUser));
    }
    //endregion

    // region General printing
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public static void printTitleNoName(String title, char c) {
        printTitle(title, c, true);
    }

    public static void printTitle(String title, char c) {
        printTitle(title, c, false);
    }

    public static void printTitle(String title, char c, boolean supressName) {
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

    public static String consoleLine(char c) {
        String line = "";
        int i;
        for (i = 0; i < consoleCharWidth; i += 2) line += c + " ";
        if (i - 1 == consoleCharWidth) line += c;
        return line;
    }

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
    private static Admin rootUser;

    private static boolean createRootUser() {
        rootUser = new Admin(User.getRootAdmin());
        if (rootUser != null) return true;
        return false;
    }

    private static boolean loadRootUser() {
        User u = new User();
        if (!u.login(User.rootUserName, User.rootUserPass)) return false;
        rootUser = new Admin(u);
        return true;
    }
    //endregion
}
