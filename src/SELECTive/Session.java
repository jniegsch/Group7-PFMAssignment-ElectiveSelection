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

        if (User.hasNoUsers()) {
            createInitialAdmin();
            InternalCore.println("\n \n \n");
        }

        InternalCore.printTitle("Welcome", '*');
        InternalCore.println("Please login to use the system.");
        InternalCore.println("Info: see the code for the masterAdmin ;)");
        InternalCore.println(InternalCore.consoleLine('-'));
        InternalCore.println("");
        if (!loginToSession()) {
            InternalCore.printIssue("Incorrect Username/Password", "The username or password you have entered is incorrect.");
            System.exit(InternalCore.NO_AUTHENTICATION);
        }

        switch (sessionUser.getUserType()) {
            case ADMIN:
                adminDashboard();
                break;
            case STUDENT:
                studentDashboard();
                break;
            case LECTURER:
                lecturerDashboard();
                break;
            case DEFAULT:
                break;
        }

        InternalCore.println("Logging out...");
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
            InternalCore.printIssue("Fatal error occured",
                    "A fatal error occured and the initial user could not be created. Please retry, and if the problem persists inform a rep. of the 'GoodEnoughCloseEnoughCoding corp.'");
            System.exit(InternalCore.INITIAL_STATE_SETUP_FAILED_FATALITY);
        }

        sessionUser = new User();
        if (sessionUser.login()) return true;
        return false;
    }
    //endregion

    //region User Specific Dashboard
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private static void adminDashboard() {
        boolean running = true;
        while(running) {
            InternalCore.println(InternalCore.consoleLine('*'));
            InternalCore.printTitle("Admin Dashboard", '*');
            InternalCore.println(InternalCore.consoleLine('*'));
            InternalCore.println("What would you like to do?");
            InternalCore.println("" +
                    "- - - User Management:\n" +
                    " 1) Reset/Change password\n" +
                    " 2) Create a new user\n" +
                    " 3) View users\n" +
                    "- - - Elective Management:\n" +
                    " 4) Add an elective\n" +
                    " 5) View elective statistics\n" +
                    " 6) Find an elective\n" +
                    "- - - \n" +
                    " 0) Logout\n");
            Integer userChoice = InternalCore.getUserInput(Integer.class, "Choice (1, 2, ..., or 6):");
            if (userChoice == null) break;
            int choice = userChoice.intValue();
            if (choice < 0 || choice > 6) {
                InternalCore.printIssue("Invalid input.", "Please specify one of the available options.");
                continue;
            }

            switch (choice) {
                case 0:
                    running = false;
                    break;
                case 1:
                    resetOrChangePasswordOfUser();
                    break;
            }
        }
    }

    private static void lecturerDashboard() {
        //TODO:
    }

    private static void studentDashboard() {
        //TODO:
    }
    //endregion

    //region Admin Actions
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private static void resetOrChangePasswordOfUser() {
        String username = InternalCore.getUserInput(String.class,
                "What is the username of the user who's password you would like to change: ");
        if (username == null) return;
        if (!User.userExists(username))  {
            InternalCore.printIssue("No such user.", "The user you requested does not seem to exist.");
            return;
        }

        User.showPasswordRules();
        String newPassword = InternalCore.getUserInput(String.class,
                "Please enter the new password: ");
        if (sessionUser.changePassword(username, null, newPassword.toCharArray())) {
            InternalCore.println("> Password successfully changed.\n \n ");
        } else {
            InternalCore.println("> Password NOT successfully changed.\n \n ");
        }
    }

    // Method to create a new user
    private static void createNewUser() {
        // User type to be created
        int typeCount = 0;
        for (UserType type : UserType.values()) {
            if (type == UserType.DEFAULT) continue;
            InternalCore.println("(" + (typeCount + 1) + ") " + type.toString());
        }
        Integer utypeSelection = InternalCore.getUserInput(Integer.class,
                "Specify the user type (1, 2, etc.): ");
        int typeSelect = (utypeSelection != null)? utypeSelection.intValue() : UserType.values().length - 1; // Last UserType is default

        // Get user input
        String uname = InternalCore.getUserInput(String.class,
                "Enter a username: ");
        String pword = InternalCore.getUserInput(String.class,
                "Enter a password: ");
        if (sessionUser.createNewUser(uname, pword.toCharArray(), UserType.values()[typeSelect]) == null) {
            InternalCore.printIssue("Coulnd't create the user", "For some reason the user could not be created, please try again.");
        }
    }

    // Method to view users
    private static void viewUsers() {
        return;

    }

    // Method to add an elective
    private static void addElective() {
        if (sessionUser.getUserType() != UserType.ADMIN) {
            InternalCore.printIssue("Insufficient access rights", "You do not have the rights to create a new Elective");
            return;
        }

        String courseCode = InternalCore.getUserInput(String.class,
                "Enter courseCode: ");
        ((Admin) sessionUser).addElective(courseCode);
    }

    // Method to view elective statistics
    private static void viewElectiveStats() {
        if (sessionUser.getUserType() != UserType.ADMIN && sessionUser.getUserType() != UserType.LECTURER) {
            InternalCore.printIssue("Insufficient access rights", "You do not have the rights to create a new Elective");
            return;
        }

        String codes = InternalCore.getUserInput(String.class,
                "Please enter all the course codes for which you would like to see the statistics, separated by ';'");
        String[] courseCodes = codes.split(";");
        Elective[] electives = new Elective[courseCodes.length];
        String[][] allElectives = InternalCore.readInfoFile(SEObjectType.ELECTIVE, null);
        for (int i = 0; i < courseCodes.length; i++) {
            int pos = 0;
            // strip all beginning whitespace
            while (pos < courseCodes[i].length()) {
                if (courseCodes[i].toCharArray()[0] != ' ') break;
                courseCodes[i] = courseCodes[i].substring(1);
            }
            // strip all end whitespace
            while (pos >= 0) {
                if (courseCodes[i].toCharArray()[courseCodes[i].length() - 1] != ' ') break;
                courseCodes[i] = courseCodes[i].substring(0, courseCodes[i].length() - 1);
            }
            for (String[] erow : allElectives) {
                if (erow[1].equals(courseCodes[i])) {
                    electives[i] = new Elective(
                            Long.parseLong(erow[0]),
                            erow[1],
                            erow[2],
                            Integer.parseInt(erow[3]),
                            MasterProgram.valueOf(erow[4]),
                            Elective.keywordsFromKeywordString(erow[5]),
                            LectureTime.generateLectureTimeArrayFromStringRepresentation(erow[6]),
                            (new LectureBlock(erow[7]))
                    );
                }
            }
        }
        ((Lecturer) sessionUser).viewStatsForElective(electives);
    }

    // Method to find an elective
    private static void findElective() {
    }
    //endregion

    //region Lecturer Actions
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    //endregion

    //region Student Actions
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

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
            InternalCore.println("Account creation failed.");
            return null;
        }
        if (User.userExists(uname)) {
            InternalCore.println("Account creation failed. The user already exists. Please create a new one.");
            return null;
        }
        InternalCore.print("Password: ");
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
        if (!u.rootLogin(User.rootUserName, User.rootUserPass)) return false;
        rootUser = new Admin(u);
        return true;
    }
    //endregion
}
