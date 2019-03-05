package SELECTive;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

public class Admin extends User {

    //region Constructor
    public Admin(User base) {
        super(base, UserType.ADMIN);
    }
    //endregion


    public boolean addElective(String courseCode) {
        if (this.getUserType() != UserType.ADMIN) return false;

        Scanner inpScanner = new Scanner(System.in);

        InternalCore.printTitle("Adding an Elective", '*');

        // There are 6 properties to set for an elective
        String electiveCourseCode = courseCode, electiveName = "";
        int electiveECTS = 0;
        String[] electiveKeywords = null;
        MasterProgram electiveProgramName = null;
        LectureBlock block = null;
        LectureTime[] electiveTimes = null;

        int prop = (courseCode == null)? 0 : 1;
        for (; prop < 7; prop++) {
            boolean successfulSet = false;
            switch (prop) {
                case 0:
                    InternalCore.print("> Elective course code: ");
                    if (inpScanner.hasNextLine()) {
                        electiveCourseCode = inpScanner.nextLine();
                        successfulSet = true;
                    }
                    break;
                case 1:
                    InternalCore.print("> Elective name: ");
                    if (inpScanner.hasNextLine()) {
                        electiveName = inpScanner.nextLine();
                        successfulSet = true;
                    }
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
                        electiveProgramName = MasterProgram.values()[selection.intValue() - 1];
                        successfulSet = true;
                    }
                    break;
                case 3:
                    InternalCore.print("> Elective ECTS: ");
                    if (inpScanner.hasNextInt()) {
                        electiveECTS = inpScanner.nextInt();
                        successfulSet = true;
                    }
                    break;
                case 4:
                    String keys = InternalCore.getUserInput(String.class, "> Elective Keywords (separate each keyword using a ';'): ");
                    if (keys != null) {
                        electiveKeywords = keys.split(";");
                        // loop through and strip starting or ending whitespace
                        for (int i = 0 ; i < electiveKeywords.length; i++) {
                            electiveKeywords[i] = InternalCore.stripWhitespace(electiveKeywords[i]);
                        }
                        successfulSet = true;
                    }
                    break;
                case 5:
                    InternalCore.println("Which block is this elective taught?");
                    InternalCore.println("(1) Block 3");
                    InternalCore.println("(2) Block 4");
                    InternalCore.println("(3) Block 5");
                    String newBlock = InternalCore.getUserInput(String.class, "Your selection (3, 4, or 5):");
                    if (newBlock != null) {
                        block = new LectureBlock(Long.parseLong(newBlock) - 1);
                        successfulSet = true;
                    }
                    break;
                case 6:
                    String times = InternalCore.getUserInput(String.class, "" +
                            "> Lesson days and times (separate the list with ';' using the format <week-day code> @ <hh:mm>): \n" +
                            "   codes: 1 = mon, 2 = tues, 3 = wed, 4 = thurs, 5 = fri, 6 = sat, 7 = sun");
                    if (times != null) {
                        String[] dateTimes = times.split(";");
                        electiveTimes = new LectureTime[dateTimes.length];
                        for (int i = 0 ; i < dateTimes.length; i++) {
                            String[] tmp = dateTimes[i].split("@");
                            electiveTimes[i] = new LectureTime(InternalCore.stripWhitespace(tmp[1]),
                                    Integer.parseInt(InternalCore.stripWhitespace(tmp[0])) - 1);
                        }
                        successfulSet = true;
                    }
                    break;
            }

            if (!successfulSet) prop--;
        }
        InternalCore.println("\nCreating elective...");

        Elective newElective = new Elective(
                -1,
                courseCode,
                electiveName,
                electiveECTS,
                electiveProgramName,
                electiveKeywords,
                electiveTimes,
                block
        );
        newElective.saveElective(this, true);

        InternalCore.println("Elective successfully created!");
        InternalCore.println(InternalCore.consoleLine('*'));
        return false;
    }
    //endregion

    //region User Management
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public boolean editSpecificUser(String uname, UserType ut) {

        if (ut.equals(UserType.LECTURER)) {
            Lecturer tempLecturer = new Lecturer(new User(uname, UserType.LECTURER, this));
            if (!tempLecturer.editUser(true)) {
                InternalCore.printIssue("Could not edit the lecturer", "");
                return false;
            }
        } else if (ut.equals(UserType.STUDENT)) {
            Student tempStudent = new Student(new User(uname, UserType.STUDENT, this));
            if (!tempStudent.editUser(false)) {
                InternalCore.printIssue("Could not edit the student", "");
                return false;
            }
        } else if (ut.equals(UserType.ADMIN)) {
            if (uname == this.getUsername()) {
                if (!this.editUser(false)) {
                    InternalCore.printIssue("Could not edit yourself", "");
                    return false;
                }
            } else {
                if (uname.equals("sudo")) {
                    InternalCore.printIssue("You may not edit the root user!", "");
                    return false;
                }
                Admin tempAdmin = new Admin(new User(uname, UserType.ADMIN, this));
                if (!tempAdmin.editUser(false)) {
                    InternalCore.printIssue("Could not edit the admin", "");
                    return false;
                }
            }
        } else {
            InternalCore.println("You entered an invalid number.");
            return false;
        }
        return true;
    }
    //endregion
}
