package SELECTive;

import java.util.Arrays;

public class Admin extends User {
    //region Static Properties
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private static Admin[] admins = null;
    private static boolean hasValidAdmins = false;
    private static boolean isLoading = false;
    //endregion

    //region Constructor
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public Admin() {
        hasValidAdmins = loadAdmins();
    }

    public Admin(User base) {
        super(base, UserType.ADMIN);
        hasValidAdmins = loadAdmins();
    }
    //endregion

    //region Static Init
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private static boolean loadAdmins() {
        if (hasValidAdmins) return true;
        if (isLoading) return false;
        isLoading = true;
        String[][] ads = InternalCore.readInfoFile(SEObjectType.ADMIN_USER, null);
        if (ads == null) return true;
        admins = new Admin[ads.length];
        for (int i = 0; i < ads.length; i++) {
            User tmp = new User(
                    ads[i][0],
                    ads[i][1],
                    ads[i][2],
                    ads[i][3],
                    ads[i][4],
                    ads[i][5],
                    UserType.ADMIN);
            admins[i] = new Admin(tmp);
        }
        return true;
    }

    public static void addAdmin(Admin admin) {
        if (alreadyHasLoaded(admin)) return;
        int currLength = 0;
        if (admins != null) {
            currLength = admins.length;
            admins = Arrays.copyOf(admins, currLength + 1);
        } else {
            admins = new Admin[1];
        }
        admins[currLength] = admin;
    }

    private static boolean alreadyHasLoaded(Admin admin) {
        hasValidAdmins = loadAdmins();
        if (admins == null) return false;
        for (Admin adm : admins) {
            if (adm.equals(admin)) return true;
        }
        return false;
    }
    //endregion

    //region Retrievers
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public static Admin getAdminWithUsername(String uname) {
        hasValidAdmins = loadAdmins();
        if (admins == null) return null;
        for (Admin adm : admins) {
            if (adm.getUsername().equals(uname)) return adm;
        }
        return null;
    }

    public static Admin[] getAllAdmins(Admin admin) {
        if (!admin.isValidAdmin()) return null;
        hasValidAdmins = loadAdmins();
        return admins;
    }
    //endregion

    //region Adding Electives
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public void addElective(String courseCode) {
        if (!this.isValidAdmin()) {
            InternalCore.printIssue("Insufficient access rights",
                    "You do not have the correct access rights to create a new elective");
            return;
        }
        InternalCore.println();
        InternalCore.printTitle("Adding an Elective", '*');
        InternalCore.println("You MUST fill in all fields!\n ");

        // There are 8 properties to set for an elective
        String electiveCourseCode, electiveName = "";
        int electiveECTS = 0, block = 0;
        String[] electiveKeywords = null;
        MasterProgram electiveProgramName = null;
        Day lectureDay = null;
	    long lecturerId = -1;

        int prop = (courseCode == null)? 0 : 1;
        for (; prop < 8; prop++) {
            boolean successfulSet = false;
            switch (prop) {
                case 0:
                    electiveCourseCode = InternalCore.getUserInput(String.class, "Elective course code: ");
                    if (electiveCourseCode != null) successfulSet = true;
                    if (Elective.getElectiveWithCourseCode(electiveCourseCode) != null) {
                        InternalCore.printIssue("Elective already exists",
                                "Please edit the elective if you need to change anything");
                        return; // If we don't let them leave here, then they'll be forced to make a `fake` elective just so that they can leave the dialog
                    }
                    if (electiveCourseCode == null) break;
                    successfulSet = !(electiveCourseCode.equals("") || electiveCourseCode.equals(" "));
                    break;
                case 1:
                    electiveName = InternalCore.getUserInput(String.class, "Elective name: ");
                    if (electiveName == null) break;
                    successfulSet = !(electiveName.equals("") || electiveName.equals(" "));
                    break;
                case 2:
                    InternalCore.println("> To which program does this elective belong?");
                    int optCount = 1;
                    for (MasterProgram p : MasterProgram.values()) {
                        if (p == MasterProgram.INVLD) continue;
                        InternalCore.print(" (" + optCount + ") " + p.toString() + " ");
                        optCount++;
                    }
                    InternalCore.println(" ");
                    Integer selection = InternalCore.getUserInput(Integer.class, "The program: ");
                    if (selection != null) {
                        if (selection < 0 || selection > MasterProgram.values().length) break;
                        electiveProgramName = MasterProgram.values()[selection - 1];
                        successfulSet = true;
                    }
                    break;
                case 3:
                    Integer number = InternalCore.getUserInput(Integer.class, "Elective ECTS: ");
                    if (number != null) {
                        electiveECTS = number;
                        successfulSet = true;
                    }
                    break;
                case 4:
                    String keys = InternalCore.getUserInput(String.class, "Elective Keywords (separate each keyword using a ';'): ");
                    if (keys != null) {
                        electiveKeywords = keys.split(";");
                        if (electiveKeywords.length < 1) {
                            InternalCore.printIssue("You must define keywords for the elective!", "Please try again...");
                            break;
                        }
                        if (electiveKeywords[0].equals("") || electiveKeywords[0].equals(" ")) break;
                        for (int i = 0 ; i < electiveKeywords.length; i++) {
                            electiveKeywords[i] = InternalCore.stripWhitespace(electiveKeywords[i]);
                        }
                        successfulSet = true;
                    }
                    break;
                case 5:
                    InternalCore.println("> Which block is this elective taught?");
                    InternalCore.println("(3) Block 3");
                    InternalCore.println("(4) Block 4");
                    InternalCore.println("(5) Block 5");
                    Integer newBlock = InternalCore.getUserInput(Integer.class, "Your selection (3, 4, or 5):");
                    if (newBlock != null) {
                        if (newBlock < 3 || newBlock > 5) break;
                        block = newBlock;
                        successfulSet = true;
                    }
                    break;
                case 6:
                    Integer classDay = InternalCore.getUserInput(Integer.class,
                            "> On which day is the lesson taught: \n" +
                                    "   codes: 1 = mon, 2 = tues, 3 = wed, 4 = thurs, 5 = fri");
                    if (classDay != null) {
                        if (classDay < 1 || classDay > 5) break;
                        lectureDay = Day.values()[classDay - 1];
                        successfulSet = true;
                    }
                    break;
                case 7:
                    String lecturerUsername = InternalCore.getUserInput(String.class, "> Which lecturer teaches this elective? Please enter the username:");
                    if (lecturerUsername != null) {
                        Lecturer lecturer = Lecturer.getLecturerWithUsername(lecturerUsername);
                        if (lecturer == null) {
                            String retry = InternalCore.getUserInput(String.class, "Lecturer not found. Would you like to try again? Or add one later? Try again (y/n): ");
                            if (retry != null) {
                                if (retry.toLowerCase().equals("y")) break;
                            }
                            lecturerId = -1;
                            successfulSet = true;
                            break;
                        }
                        lecturerId = lecturer.getUserId();
                        successfulSet = true;
                    }
                    break;
            }
            if (!successfulSet) --prop;
        }
        InternalCore.println("\nCreating elective...");

        Elective newElective = new Elective(
                -1,
                courseCode,
                electiveName,
                electiveECTS,
                electiveProgramName,
                electiveKeywords,
                lectureDay,
                block,
                lecturerId
        );
        newElective.saveElective(this, true);

        InternalCore.println("Elective successfully created!");
        InternalCore.println();
    }
    //endregion

    //region User Management
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public void editSpecificUser(String uname, UserType ut) {

        if (ut.equals(UserType.LECTURER)) {
            Lecturer tempLecturer = Lecturer.getLecturerWithUsername(uname);
            if (tempLecturer == null) {
                InternalCore.printIssue("Could not edit the lecturer",
                        "lecturer doesn't seem to exist");
                return;
            }
            if (tempLecturer.editUserUnsuccessful()) {
            	InternalCore.printIssue("Could not edit the lecturer", "");
            }
        } else if (ut.equals(UserType.STUDENT)) {
            Student tempStudent = Student.getStudentWithUsername(uname);
            if (tempStudent == null) {
                InternalCore.printIssue("Could not edit the student",
                        "Student doesn't seem to exist");
                return;
            }
            if (tempStudent.editUserUnsuccessful()) {
                InternalCore.printIssue("Could not edit the student",
                        "There was an issue editing the requested user.");
            }
        } else if (ut.equals(UserType.ADMIN)) {
            if (uname.equals(this.getUsername())) {
                if (this.editUserUnsuccessful()) {
                    InternalCore.printIssue("Could not edit yourself", "");
                }
            } else {
                if (uname.equals("sudo")) {
                    InternalCore.printIssue("You may not edit the root user!", "");
                    return;
                }
                Admin tempAdmin = getAdminWithUsername(uname);
		if (tempAdmin == null) {
                    InternalCore.printIssue("Could not edit the admin",
                            "admin doesn't seem to exist");
            return;
                }
                if (tempAdmin.editUserUnsuccessful()) {
                    InternalCore.printIssue("Could not edit the admin", "");
                }
            }
        } else {
            InternalCore.println("You entered an invalid number.");
        }
    }
    //endregion
}
