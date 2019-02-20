package SELECTive;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Session {


    //region Private Session Properties
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private static User sessionUser = null;
    //endregion

    //region _MAIN
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public static void main(String[] args) {
        //TODO: The actual program...

        if (User.hasNoUsers()) createInitialAdmin();

        InternalCore.printTitle("Welcome", '*');
        InternalCore.println("Please login to use the system.");
        InternalCore.println("Info: see the code for the masterAdmin ;)");
        InternalCore.println(InternalCore.consoleLine('-'));
        InternalCore.println("");
        if (!loginToSession()) {
            InternalCore.printIssue("Incorrect Username/Password", "The username or password you have entered is incorrect.");
            System.exit(InternalCore.NO_AUTHENTICATION);
        }

        // DEBUG
        long[] a = {1};
        User[] retUsers = User.getUsers(a, UserType.STUDENT);

        for (int i = 0; i < retUsers.length; i++) {
            InternalCore.println(retUsers[i].toString());
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
            InternalCore.printIssue("Fatal error occured", "A fatal error occured and the initial user could not be created. Please retry, and if the problem persists inform a rep. of the 'GoodEnoughCloseEnoughCoding corp.'");
            System.exit(InternalCore.INITIAL_STATE_SETUP_FAILED_FATALITY);
        }

        Scanner lgSelect = new Scanner(System.in);
        String uname = "";
        char[] pword = null;
        InternalCore.print("Username: ");
        if (lgSelect.hasNextLine()) {
            uname = lgSelect.nextLine();
        } else {
            InternalCore.printIssue("Invalid Username", "You have selected an invalid username. The system will exit.");
            System.exit(InternalCore.ALL_GOOD_IN_THE_HOOD);
        }
        try {
            if (System.console() != null) {
                pword = System.console().readPassword();
            } else {
                throw new IOError(new IOException("No console"));
            }
        } catch (IOError ioe) {
            InternalCore.printError("Session",
                    "loginToSession",
                    "IOError",
                    "An IO Error occurred when trying to read the password securely. Will use the standard Scanner instead");
            InternalCore.print("Password: ");
            if (lgSelect.hasNextLine()) {
                pword = lgSelect.nextLine().toCharArray();
            } else {
                InternalCore.printIssue("Invalid password", "You have selected an invalid password. The system will exit.");
                pword = null;
            }
        }
        if (pword == null) {
            InternalCore.printIssue("Invalid Username", "You have selected an invalid username. The system will exit.");
            System.exit(InternalCore.ALL_GOOD_IN_THE_HOOD);
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
        InternalCore.printIssue("No admin created.", "Please create an admin first. You will be guided through the setup.");
        Scanner adminCreationScanner = new Scanner(System.in);
        InternalCore.print("\nWhat username would you like: ");
        String uname = "";
        if (adminCreationScanner.hasNextLine()) {
            uname = adminCreationScanner.nextLine();
        } else {
            InternalCore.println("Using default.");
        }
        InternalCore.print("Passord: ");
        char[] pword;
        if (adminCreationScanner.hasNextLine()) {
            pword = adminCreationScanner.nextLine().toCharArray();
            InternalCore.print("Repeat password: ");
            if (adminCreationScanner.hasNextLine()) {
                if (!Arrays.equals(pword, adminCreationScanner.nextLine().toCharArray())) {
                    InternalCore.println("Account creation failed.");
                    return null;
                }
            } else {
                InternalCore.println("Account creation failed.");
                return null;
            }
        } else {
            InternalCore.println("Account creation failed.");
            return null;
        }
        return new Admin(rootUser.createNewUser(uname, pword, UserType.ADMIN));
    }
    //endregion

    //region Initial System Setup
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
