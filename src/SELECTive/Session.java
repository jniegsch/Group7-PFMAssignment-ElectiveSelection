package SELECTive;

import java.util.Arrays;

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
            InternalCore.println("Initial Admin successfully created! \n \n");
        } else {
            InternalCore.printTitle("Welcome", '*');
            InternalCore.println("Please login to use the system.");
            InternalCore.println("Info: see the code for the masterAdmin ;)");
            InternalCore.println(InternalCore.consoleLine('-'));
            InternalCore.println("");
            if (!loginToSession()) {
                InternalCore.printIssue("Incorrect Username/Password",
                        "The username or password you have entered is incorrect.");
                System.exit(InternalCore.NO_AUTHENTICATION);
            }
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

        InternalCore.printTitleNoName("Logging out", '-');
        InternalCore.cleanup();
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

        sessionUser = User.login();
        if (sessionUser != null) return true;
        return false;
    }
    //endregion

    //region User Specific Dashboard
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private static void adminDashboard() {
        while (true) {
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
                    " 8) View all electives\n" +
                    "- - - \n" +
                    " 0) Logout\n" +
                    "- - -");
            Integer userChoice = InternalCore.getUserInput(Integer.class, "Choice (0, 1, 2, etc.):");
            if (userChoice == null) break;
            int choice = userChoice.intValue();
            if (choice < 0 || choice > 8) {
                InternalCore.printIssue("Invalid input.", "Please specify one of the available options.");
                continue;
            }

            if (choice == 0) break;

            InternalCore.println();
            InternalCore.println(InternalCore.consoleLine('-'));
            switch (choice) {
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
                case 8:
                    viewElectives();
                    break;
            }
            InternalCore.println(InternalCore.consoleLine('-'));
            waitToReturnToDashboard();
        }
    }

    private static void lecturerDashboard() {
        while (true) {
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
                    + " 5) Find an elective\n"
                    + " 6) View electives you teach\n"
                    + "- - - Account Management:\n"
                    + " 7) Reset/Change password\n"
                    + "- - - \n"
                    + " 0) Logout\n"
                    + "- - -");

            Integer userChoice = InternalCore.getUserInput(Integer.class, "Choice (0, 1, 2, etc.):");
            if (userChoice == null)
                break;
            int choice = userChoice.intValue();
            if (choice < 0 || choice > 7) {
                InternalCore.printIssue("Invalid input", "Please specify one of the available options.");
                continue;
            }

            if (choice == 0) break;

            InternalCore.println();
            InternalCore.println(InternalCore.consoleLine('-'));
            switch (choice) {
                case 1:
                    addOrChangeStudentGrade();
                    break;
                case 2:
                    viewRegisteredStudentsPerElective();
                    break;
                case 3:
                    viewStudentGradesPerElective();
                    break;
                case 4:
                    viewGradeStatsPerElective();
                    break;
                case 5:
                    filterElectives();
                    break;
                case 6:
                    viewOwnElectives();
                    break;
                case 7:
                    resetOrChangePasswordOfUser(sessionLecturer);
                    break;

            }
            InternalCore.println(InternalCore.consoleLine('-'));
            waitToReturnToDashboard();
        }
    }

    private static void studentDashboard() {
        while (true) {
            InternalCore.println(InternalCore.consoleLine('*'));
            InternalCore.printTitle("Student Dashboard", '*');
            InternalCore.println(InternalCore.consoleLine('*'));
            InternalCore.println("What would you like to do?");
            InternalCore.println(""
                    + "- - - Elective Management:\n"
                    + " 1) Find an elective\n"
                    + " 2) View all electives\n"
                    + " 3) Register to an elective\n"
                    + " 4) View a list of your enrolled electives\n"
                    + " 5) View your grade(s)\n"
                    + "- - - Account Management:\n"
                    + " 6) Reset/Change password\n"
                    + "- - - \n"
                    + " 0) Logout\n"
                    + "- - -");

            Integer userChoice = InternalCore.getUserInput(Integer.class, "Choice (0, 1, 2, etc.):");
            if (userChoice == null)
                break;
            int choice = userChoice.intValue();
            if (choice < 0 || choice > 6) {
                InternalCore.printIssue("Invalid input.", "Please specify one of the available options.");
                continue;
            }

            if (choice == 0) break;

            InternalCore.println();
            InternalCore.println(InternalCore.consoleLine('-'));
            switch (choice) {
                case 1:
                    filterElectives();
                    break;
                case 2:
                    viewElectives();
                    break;
                case 3:
                    registerForElective();
                    break;
                case 4:
                    sessionStudent.viewEnrolledElectives();
                    break;
                case 5:
                    viewGrades();
                    break;
                case 6:
                    resetOrChangePasswordOfUser(sessionStudent);
                    break;

            }
            InternalCore.println(InternalCore.consoleLine('-'));
            waitToReturnToDashboard();
        }
    }
    //endregion

    //region Dashboard Return Buffer
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private static void waitToReturnToDashboard() {
        InternalCore.println();
        InternalCore.getUserInput(String.class, "Close view / Back (press any key)");
        InternalCore.println();
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
        User.showPasswordRules();
        String pass = InternalCore.getUserInput(String.class,
                "Enter a password: ");
        if (pass == null) return;
        char[] pword = pass.toCharArray();
        if (User.invalidPassword(pword)) {
            InternalCore.printIssue("Invalid password", "");
            return;
        }
        String pass2 = InternalCore.getUserInput(String.class,
                "Repeat the password: ");
        if (pass2 == null) return;
        char[] pword2 = pass2.toCharArray();
        if (!Arrays.equals(pword, pword2)) {
            InternalCore.printIssue("The passwords do not match.", "");
            return;
        }
        if (sessionAdmin.createNewUser(uname, pword, UserType.values()[typeSelect]) == null) {
            InternalCore.printIssue("Couldn't create the user", "For some reason the user could not be created, please try again.");
        }
        InternalCore.println("User created successfully!");
    }

    // Method to view users
    private static void viewUsers() {
        if (sessionUser.getUserType() != UserType.ADMIN) {
            InternalCore.printIssue("Insufficient access rights", "You do not have the rights to create a new Elective");
            return;

        }

        InternalCore.println(" \n ");
        InternalCore.printTitle("Here is a list of all users", '-');

        Admin[] adminUsers = Admin.getAllAdmins(sessionAdmin);
        Student[] studentUsers = Student.getAllStudents(sessionAdmin);
        Lecturer[] lectureUsers = Lecturer.getAllLecturers(sessionAdmin);

        // print the users
        if (adminUsers != null) {
            InternalCore.println("> Admins:");
            for (Admin au : adminUsers) {
                InternalCore.println(au.toString());
            }
        }
        if (studentUsers != null) {
            InternalCore.println("> Students:");
            for (Student su : studentUsers) {
                InternalCore.println(su.toString());
            }
        }
        if (lectureUsers != null) {
            InternalCore.println("> Lecturers:");
            for (Lecturer lu : lectureUsers) {
                InternalCore.println(lu.toString());
            }
        }
        InternalCore.println(InternalCore.consoleLine('-') + "\n ");
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

        if (Elective.getElectiveWithCourseCode(InternalCore.stripWhitespace(code)) == null) return; 
        Elective toEdit = Elective.getElectiveWithCourseCode(InternalCore.stripWhitespace(code));
        if (toEdit.getElectiveId() == -1) return;
        toEdit.edit(sessionAdmin);
    }

    private static void editAUser() {
        String uname = InternalCore.getUserInput(String.class,
                "Enter a username: ");
        UserType utype = User.getUserTypeOfUser(uname);
        switch (utype) {
            case STUDENT:
                sessionAdmin.editSpecificUser(uname, UserType.STUDENT);
                break;
            case ADMIN:
                sessionAdmin.editSpecificUser(uname, UserType.ADMIN);
                break;
            case LECTURER:
                sessionAdmin.editSpecificUser(uname, UserType.LECTURER);
                break;
            case DEFAULT:
                InternalCore.printIssue("User does not seem to exist!", "");
                break;
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
        String courseCode = InternalCore.getUserInput(String.class, "Please enter the course code for which you would like to view the registered students: ");
        sessionLecturer.showStudents(courseCode); // REMOVE ", LectureBlock block" as input for the showStudents method from line 56 in class Lecturer since it is not used there
    }

    // Method to view student grades for an elective
    private static void viewStudentGradesPerElective() {
        String courseCode = InternalCore.getUserInput(String.class, "Please enter the course code for which you would like to view the student grades: ");
        sessionLecturer.showStudentGrades(courseCode);
    }

    // Method to print grade statistics for an elective AND number of students that failed the elective
    private static void viewGradeStatsPerElective() {
        String courseCode = InternalCore.getUserInput(String.class, "Please enter the course code for which you would like to view the grade statistics: ");
        sessionLecturer.viewStatsForElective(courseCode);
    }

    private static void viewOwnElectives() {
        Elective[] electives = Elective.getAllElectivesForLecturer(sessionLecturer);
        if (electives == null) {
            InternalCore.println("You do not seem to be teaching any electives. If you are, please contact a system admin.");
            return;
        }
        InternalCore.println("These are the course you teach: (If any are missing, or there that shouldn't be, contact an admin)");
        for (Elective elective : electives) {
            InternalCore.println(elective.toString());
        }
    }
    //endregion

    //region Student Actions
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private static void registerForElective() {
        String courseCode = InternalCore.getUserInput(String.class, "For which elective do you want to register? Please give the course code:");
        if (courseCode == null) return;
        Elective elect = Elective.getElectiveWithCourseCode(courseCode);
        if (elect == null) {
            InternalCore.printIssue("No such course exists", "No elective exists with that course code.");
            return;
        }
        Registration studentsRegistrations = Registration.registrationForStudent(sessionStudent);
        if (studentsRegistrations == null) {
            Elective[] elects = new Elective[3];
            double[] grds = {-1, -1, -1};
            elects[elect.getBlock() - 3] = elect;
            studentsRegistrations = new Registration(
                    sessionStudent,
                    elects,
                    grds
            );
            if (studentsRegistrations.saveRegistration(true)) {
                InternalCore.println("> Registration successful");
            } else {
                InternalCore.println("> Registration unsuccessful");
            }
            return;
        }
        if (!studentsRegistrations.registerForElective(sessionStudent, elect)) {
            InternalCore.printIssue("Could not register",
                    "For some reason you couldn't register for the elective. Please try again.");
        }
    }

    private static void viewGrades() {
        String viewAll = InternalCore.getUserInput(String.class, "Would you like to see your progress for all electives? (y/n)");
        if (viewAll != null) {
            if (viewAll.toLowerCase().equals("y")) {
                InternalCore.println();
                sessionStudent.viewProgress();
                return;
            }
        }
        String courseCode = InternalCore.getUserInput(String.class, "Please enter the course code of the elective for which you would like to see your grades");
        if (courseCode == null) return;
        InternalCore.println();
        sessionStudent.viewElectiveProgress(courseCode);
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
      
        boolean success = false;
        if (sessionAdmin == null) {
            if (sessionUser.changePassword(oldPassword.toCharArray(), newPassword.toCharArray())) success = true;
        } else {
            if (sessionAdmin.changePassword(username, (u == null)? null : oldPassword.toCharArray(), newPassword.toCharArray())) success = true;
        }

        if (success) {
            InternalCore.println("> Password successfully changed.\n \n ");
        } else {
            InternalCore.println("> Password NOT successfully changed.\n \n ");
        }
    }

    private static void filterElectives() {
        // Print filter options
        int optId = 1;
        InternalCore.printTitle("Available Filter Options: ", '-');
        for (Elective.ElectiveFilterType eft : Elective.ElectiveFilterType.values()) {
            InternalCore.println(" (" + optId + ") " + eft.name());
            optId++;
        }
        Integer userFilterTypeChoice = InternalCore.getUserInput(Integer.class,
                "Please enter your choice of the available filters: ");
        if (userFilterTypeChoice == null) return;
        Elective.ElectiveFilterType userFilterType = Elective.ElectiveFilterType.values()[userFilterTypeChoice - 1];

        Elective[] electives = null;
        switch (userFilterType) {
            case COURSEID:
                String courseCodes = InternalCore.getUserInput(String.class,
                        "Please enter the course code you would like to filter on (separated by a ';' if you want to filter on multiple course codes: ");
                if (courseCodes == null) return;
                String[] codes = courseCodes.split(";");
                electives = Elective.filterOn(Elective.ElectiveFilterType.COURSEID, InternalCore.stripWhitespaceOfArray(codes));
                break;
            case ECTS:
                String ectsString = InternalCore.getUserInput(String.class,
                        "Please enter the ects number you would like to filter on (separated by a ';' if you want to filter on multiple ects): ");
                if (ectsString == null) return;
                String[] ectsArr = ectsString.split(";");
                electives = Elective.filterOn(Elective.ElectiveFilterType.ECTS, InternalCore.stripWhitespaceOfArray(ectsArr));
                break;
            case BLOCK:
                String blockStr = InternalCore.getUserInput(String.class,
                        "Please enter the block number you would like to filter on (separated by a ';' if you want to filter on multiple blocks): ");
                if (blockStr == null) return;
                String[] block = blockStr.split(";");
                electives = Elective.filterOn(Elective.ElectiveFilterType.BLOCK, InternalCore.stripWhitespaceOfArray(block));
                break;
            case KEYWORDS:
                String keywordStr = InternalCore.getUserInput(String.class,
                        "Please enter the keyword you would like to filter on (separated by a ';' if you want to filter on multiple keywords): ");
                if (keywordStr == null) return;
                String[] keywords = keywordStr.split(";");
                electives = Elective.filterOn(Elective.ElectiveFilterType.KEYWORDS, InternalCore.stripWhitespaceOfArray(keywords));
                break;
        }

        InternalCore.println("\nThe electives that match your search are presented in the following match list: ");
        int optEl = 1;
        if (electives == null) {
			InternalCore.println("No matches.");
		} else {
            for (Elective elect : electives) {
                InternalCore.println(" " + optEl + ") " + elect.toString());
                optEl++;
            }
            String viewChoice = InternalCore.getUserInput(String.class, "Would you like to view the details of one of the electives in the match list? (y/n)");
            if (viewChoice == null) return;
            if (viewChoice.toLowerCase().equals("y")) {
                while (true) {
                    Integer electiveChoice = InternalCore.getUserInput(Integer.class, "Which elective of the match list would you like to view (1, 2, etc.): ");
                    if (electiveChoice == null) break;
                    if (electiveChoice < 1 || electiveChoice > electives.length) break;
                    InternalCore.println(" \n" + InternalCore.consoleLine('-'));
                    InternalCore.println(electives[electiveChoice - 1].view());
                    InternalCore.println();
                    String contChoice = InternalCore.getUserInput(String.class, "Would you like to view another elective from the match list? (y/n)");
                    if (contChoice == null) break;
                    if (contChoice.toLowerCase().equals("y")) {
                        optEl = 1;
                        for (Elective elect : electives) {
                            InternalCore.println(" " + optEl + ") " + elect.toString());
                            optEl++;
                        }
                        continue;
                    }
                    break;
                }
            }
        }
    }

    private static void viewElectives() {
        Elective[] electives = Elective.getAllElectives();
        if (electives == null) {
            InternalCore.println("Looks like there are no electives...");
            return;
        }

        InternalCore.println("These are the available electives:");
        int printCount = 0;
        InternalCore.println("[Block 3]");
        for (Elective elective : electives) {
            if (elective.getBlock() != 3) continue;
            InternalCore.println(elective.toString());
            printCount++;
        }
        if (printCount == 0) InternalCore.println("No electives in this block");
        printCount = 0;
        InternalCore.println("\n[Block 4]");
        for (Elective elective : electives) {
            if (elective.getBlock() != 4) continue;
            InternalCore.println(elective.toString());
            printCount++;
        }
        if (printCount == 0) InternalCore.println("No electives in this block");
        printCount = 0;
        InternalCore.println("\n[Block 5]");
        for (Elective elective : electives) {
            if (elective.getBlock() != 5) continue;
            InternalCore.println(elective.toString());
            printCount++;
        }
        if (printCount == 0) InternalCore.println("No electives in this block");
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

        String uname = InternalCore.getUserInput(String.class, "What username would you like: ");
        if (uname == null) {
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

            if (User.invalidPassword(pword)) {
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
        if ((rootUser = User.rootLogin(User.rootUserName, User.rootUserPass)) == null) return false;
        return true;
    }
    //endregion
}
