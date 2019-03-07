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
    private static Admin sessionAdmin = null;
    private static Lecturer sessionLecturer = null;
    private static Student sessionStudent = null;
    //endregion

    //region _MAIN
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public static void main(String[] args) {
        //TODO: The actual program...

        if (User.hasNoUsers()) {
            sessionUser = createInitialAdmin();
            sessionAdmin = (Admin)sessionUser;
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
                sessionAdmin = new Admin(sessionUser);
                adminDashboard();
                break;
            case STUDENT:
                sessionStudent = new Student(sessionUser);
                studentDashboard();
                break;
            case LECTURER:
                sessionLecturer = new Lecturer(sessionUser);
                lecturerDashboard();
                break;
            case DEFAULT:
                sessionUser = null;
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
                    " 4) Edit user\n" +
                    "- - - Elective Management:\n" +
                    " 5) Add an elective\n" +
                    " 6) Edit an elective\n" +
                    " 7) Find an elective\n" +
                    "- - - \n" +
                    " 0) Logout\n");
            Integer userChoice = InternalCore.getUserInput(Integer.class, "Choice (0, 1, 2, ..., or 7):");
            if (userChoice == null) break;
            int choice = userChoice.intValue();
            if (choice < 0 || choice > 7) {
                InternalCore.printIssue("Invalid input.", "Please specify one of the available options.");
                continue;
            }

            switch (choice) {
                case 0:
                    running = false;
                    break;
                case 1:
                    resetOrChangePasswordOfUser(null);
                    break;
                case 2:
                    createNewUser();
                    break;
                case 3:
                    viewUsers();
                    break;
                case 4:
                    editAUser();
                    break;
                case 5:
                    addElective();
                    break;
                case 6:
                    editElective();
                    break;
                case 7:
                    filterElectives();
                    break;
            }
        }
    }

    private static void lecturerDashboard() {
        boolean running = true;
        while (running) {
            InternalCore.println(InternalCore.consoleLine('*'));
            InternalCore.printTitle("Lecturer Dashboard", '*');
            InternalCore.println(InternalCore.consoleLine('*'));
            InternalCore.println("What would you like to do?");
            InternalCore.println(""
                    + "- - - Student Management:\n"
                    + " 1) Add/change student grades\n"
                    + " 2) View list of registered students per elective\n"
                    + " 3) View list of student grades per elective\n"
                    + " 4) View grade statistics per elective\n"
                    + "- - - Elective Management:\n"
                    + " 5) Find an elective"
                    + "- - - Account Management:\n"
                    + " 6) Reset/Change password\\n"
                    + "- - - \n"
                    + " 0) Logout\n");

            Integer userChoice = InternalCore.getUserInput(Integer.class, "Choice (0, 1, 2, ..., or 6):");
            if (userChoice == null)
                break;
            int choice = userChoice.intValue();
            if (choice < 0 || choice > 6) {
                InternalCore.printIssue("Invalid input", "Please specify one of the available options.");
                continue;
            }

            switch (choice) {
                case 0:
                    running = false;
                    break;
                case 1:
                    addOrChangeStudentGrade();
                    break;
                case 2:
                    viewRegisteredStudentsPerElective();
                    break;
                case 3:
                    viewStudentGradesPerElectice();
                    break;
                case 4:
                    viewGradeStatsPerElective();
                    break;
                case 5:
                    filterElectives();
                    break;
                case 6:
                    resetOrChangePasswordOfUser(sessionLecturer);
                    break;

            }
        }
    }

    private static void studentDashboard() {
        boolean running = true;
        while (running) {
            InternalCore.println(InternalCore.consoleLine('*'));
            InternalCore.printTitle("Student Dashboard", '*');
            InternalCore.println(InternalCore.consoleLine('*'));
            InternalCore.println("What would you like to do?");
            InternalCore.println(""
                    + "- - - Elective Management:\n"
                    + " 1) Find an elective\n"
                    + " 2) Register to an elective\n"
                    + " 3) View a list of your enrolled electives\n"
                    + " 4) View your grade for a specific elective\n"
                    + " 5) Create ICal export\n"
                    + "- - - Account Management:\n"
                    + " 6) Reset/Change password\\n"
                    + "- - - \n"
                    + " 0) Logout\n");

            Integer userChoice = InternalCore.getUserInput(Integer.class, "Choice (0, 1, 2, ..., or 6):");
            if (userChoice == null)
                break;
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
                    filterElectives();
                    break;
                case 2:
                    String courseCode = InternalCore.getUserInput(String.class, "For which elective do you want to register? Please give the course code:");
                    sessionStudent.registerToElective(courseCode);
                    break;
                case 3:
                    sessionStudent.viewEnrolledElectives();
                    break;
                case 4:
                    String courseCodeProgress = InternalCore.getUserInput(String.class, "For which elective do you want to register? Please give the course code:");
                    sessionStudent.viewElectiveProgress(courseCodeProgress);;
                    break;
                case 5:
                    sessionStudent.exportCalForElectives();
                    break;
                case 6:
                    resetOrChangePasswordOfUser(sessionStudent);
                    break;

            }
        }
    }
    //endregion

    //region Admin Actions
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // Method to create a new user
    private static void createNewUser() {
        // User type to be created
        int typeCount = 0;
        for (UserType type : UserType.values()) {
            if (type == UserType.DEFAULT) continue;
            InternalCore.println("(" + (typeCount + 1) + ") " + type.toString());
            typeCount++;
        }
        Integer utypeSelection = InternalCore.getUserInput(Integer.class,
                "Specify the user type (1, 2, etc.): ");
        int typeSelect = (utypeSelection != null)? utypeSelection.intValue() - 1 : UserType.values().length - 1; // Last UserType is default

        // Get user input
        String uname = InternalCore.getUserInput(String.class,
                "Enter a username: ");
        String pword = InternalCore.getUserInput(String.class,
                "Enter a password: ");
        if (sessionAdmin.createNewUser(uname, pword.toCharArray(), UserType.values()[typeSelect]) == null) {
            InternalCore.printIssue("Couldn't create the user", "For some reason the user could not be created, please try again.");
        }
    }

    // Method to view users
    private static void viewUsers() {
        if (sessionUser.getUserType() != UserType.ADMIN) {
            InternalCore.printIssue("Insufficient access rights", "You do not have the rights to create a new Elective");
            return;

        }

        InternalCore.println(" \n ");
        InternalCore.printTitle("Here is a list of all users", '-');

        long[] userIDs = null;
        User[] adminUsers = User.getUsers(userIDs, UserType.ADMIN);
        User[] studentUsers = User.getUsers(userIDs, UserType.STUDENT);
        User[] lectureUsers = User.getUsers(userIDs, UserType.LECTURER);

        // print the users
        if (adminUsers != null) {
            for (User au : adminUsers) {
                InternalCore.println(au.toString());
            }
        }
        if (studentUsers != null) {
            for (User su : studentUsers) {
                InternalCore.println(su.toString());
            }
        }
        if (lectureUsers != null) {
            for (User lu : lectureUsers) {
                InternalCore.println(lu.toString());
            }
        }
        InternalCore.println(InternalCore.consoleLine('-') + "\n \n ");

    }

    // Method to add an elective
    private static void addElective() {
        if (sessionUser.getUserType() != UserType.ADMIN) {
            InternalCore.printIssue("Insufficient access rights", "You do not have the rights to create a new Elective");
            return;
        }

        String courseCode = InternalCore.getUserInput(String.class,
                "Enter courseCode: ");
        sessionAdmin.addElective(courseCode);
    }

    private static void editElective() {
        if (sessionUser.getUserType() != UserType.ADMIN) {
            InternalCore.printIssue("Insufficient access rights", "You do not have the rights to create a new Elective");
            return;
        }

        String code = InternalCore.getUserInput(String.class,
                "Enter the course code for the elective you would like to edit: ");
        if (code == null) return;

        Elective toEdit = new Elective(InternalCore.stripWhitespace(code));
        if (toEdit.getElectiveId() == -1) return;
        toEdit.edit(sessionAdmin);
    }

    // Method to view elective statistics
    private static void viewElectiveStats() {
        if (sessionUser.getUserType() != UserType.ADMIN && sessionUser.getUserType() != UserType.LECTURER) {
            InternalCore.printIssue("Insufficient access rights", "You do not have the rights to create a new Elective");
            return;
        }

        String codes = InternalCore.getUserInput(String.class,
                "Please enter all the course codes for which you would like to see the statistics, separated by ';'");
        if (codes == null) return;
        String[] courseCodes = codes.split(";");
        Elective[] electives = new Elective[courseCodes.length];
        for (int i = 0; i < courseCodes.length; i++) {
            courseCodes[i] = InternalCore.stripWhitespace(courseCodes[i]);
            electives[i] = new Elective(courseCodes[i]);
        }

        if (sessionLecturer != null) sessionLecturer.viewStatsForElective(electives);
        if (sessionAdmin != null) return;
    }

    private static void editAUser() {
        String uname = InternalCore.getUserInput(String.class,
                "Enter a username: ");
        InternalCore.println("What usertype is " + uname + ":\n" +
                "(1) Lecturer\n" +
                "(2) Student\n" +
                "(3) Admin");
        Integer utype = InternalCore.getUserInput(Integer.class,
                "Please enter your choice (1 or 2):");
        if (utype == null) {
            InternalCore.println("You entered an invalid Username.");
            return;
        }
        if (utype == 1) {
            sessionAdmin.editSpecificUser(uname, UserType.LECTURER);
        } else if (utype == 2) {
            sessionAdmin.editSpecificUser(uname, UserType.STUDENT);
        } else if (utype == 3) {
            sessionAdmin.editSpecificUser(uname, UserType.ADMIN);
        }
    }
    //endregion

    //region Lecturer Actions
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // Method to add or change a student grade
    private static void addOrChangeStudentGrade() {
        sessionLecturer.newGradeEntry();
    }

    // Method to view registered students for an elective
    private static void viewRegisteredStudentsPerElective() {
        String courseCode = InternalCore.getUserInput(String.class, "Please enter the coursecode for which you would like to view the registered students: ");
        sessionLecturer.showStudents(courseCode); // REMOVE ", LectureBlock block" as input for the showStudents method from line 56 in class Lecturer since it is not used there
    }

    // Method to view student grades for an elective
    private static void viewStudentGradesPerElectice() {
        String courseCode = InternalCore.getUserInput(String.class, "Please enter the coursecode for which you would like to view the registered students: ");
        sessionLecturer.showStudentGrades(courseCode);
    }

    // Method to print grade statistics for an elective AND number of students that failed the elective
    private static void viewGradeStatsPerElective() {
        String courseCode = InternalCore.getUserInput(String.class, "Please enter the coursecode for which you would like to view the grade statistics: ");
        sessionLecturer.viewStatsForElective(new Elective(courseCode));
    }
    //endregion

    //region All User Actions
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private static void resetOrChangePasswordOfUser(User u) {
        String username = null, oldPassword = null;
        if (u == null) {
            username = InternalCore.getUserInput(String.class,
                    "What is the username of the user who's password you would like to change: ");
            if (username == null) return;
            if (!User.userExists(username))  {
                InternalCore.printIssue("No such user.", "The user you requested does not seem to exist.");
                return;
            }
        } else {
            username = u.getUsername();
            oldPassword = InternalCore.getUserInput(String.class, "Please enter the old password: ");
            if (oldPassword == null) {
                InternalCore.printIssue("Invalid password entered.", "");
                return;
            }
        }


        User.showPasswordRules();
        String newPassword = InternalCore.getUserInput(String.class,
                "Please enter the new password: ");
        if (newPassword == null) {
            InternalCore.printIssue("Invalid password entered.", "");
            return;
        }
        if (sessionAdmin.changePassword(username, (u == null)? null : oldPassword.toCharArray(), newPassword.toCharArray())) {
            InternalCore.println("> Password successfully changed.\n \n ");
        } else {
            InternalCore.println("> Password NOT successfully changed.\n \n ");
        }
    }

    private static boolean filterElectives() {
        // Print filter options
        int optId = 1;
        InternalCore.printTitle("Available Filter Options: ", '-');
        for (Elective.ElectiveFilterType eft : Elective.ElectiveFilterType.values()) {
            InternalCore.println(" (" + optId + ") " + eft.name());
            optId++;
        }
        Integer userFilterTypeChoice = InternalCore.getUserInput(Integer.class,
                "Please enter your choice of the available filters: ");
        if (userFilterTypeChoice == null) return false;
        Elective.ElectiveFilterType userFilterType = Elective.ElectiveFilterType.values()[userFilterTypeChoice - 1];

        Elective[] electives = null;
        switch (userFilterType) {
            case COURSEID:
                String courseCodes = InternalCore.getUserInput(String.class,
                        "Please enter the course codes you would like to filter on separated by a ';': ");
                if (courseCodes == null) return false;
                String[] codes = courseCodes.split(";");
                electives = Elective.filterOn(Elective.ElectiveFilterType.COURSEID, InternalCore.stripWhitespaceOfArray(codes));
                break;
            case ECTS:
                String ectsString = InternalCore.getUserInput(String.class,
                        "Please enter the ects you would like to filter on separated by a ';': ");
                if (ectsString == null) return false;
                String[] ectsArr = ectsString.split(";");
                electives = Elective.filterOn(Elective.ElectiveFilterType.ECTS, InternalCore.stripWhitespaceOfArray(ectsArr));
                break;
            case BLOCK:
                String keywordStr = InternalCore.getUserInput(String.class,
                        "Please enter the keywords you would like to filter on separated by a ';': ");
                if (keywordStr == null) return false;
                String[] keywords = keywordStr.split(";");
                electives = Elective.filterOn(Elective.ElectiveFilterType.ECTS, InternalCore.stripWhitespaceOfArray(keywords));
                break;
                //TODO Availability
        }

        InternalCore.println("\nThe electives that match your search are: ");
        for (Elective elect : electives) {
            InternalCore.println(elect.toString());
        }

        return true;
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
            InternalCore.println("Account creation failed.");
            return null;
        }
        if (User.userExists(uname)) {
            InternalCore.println("Account creation failed. The user already exists. Please create a new one.");
            return null;
        }

        int numTries = 0;
        while(numTries < User.MAX_LOGIN_ATTEMPTS) {
            User.showPasswordRules();
            String pass = InternalCore.getUserInput(String.class, "Password: ");
            if (pass == null) return null;
            char[] pword = pass.toCharArray();
            pass = null; // try and get pass GCed

            if (!User.validPassword(pword)) {
                InternalCore.println("Invalid password!");
                numTries++;
                continue;
            }

            String pass2 = InternalCore.getUserInput(String.class, "Repeat Password: ");
            if (!Arrays.equals(pword, pass2.toCharArray())) {
                InternalCore.println("Account creation failed.");
                numTries++;
                continue;
            }
            return new Admin(rootUser.createNewUser(uname, pword, UserType.ADMIN));
        }
        InternalCore.println("Account creation failed.");
        return null;
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
