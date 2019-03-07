package SELECTive;

import java.util.Arrays;

public class Student extends User {
    //region Static Properties
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private static Student[] students = null;
    private static boolean hasValidStudents = false;
    private static boolean isLoading = false;
    //endregion

    //region Constructor
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public Student() {
        hasValidStudents = loadStudents();
    }

    public Student(User base) {
        super(base, UserType.STUDENT);
        hasValidStudents = loadStudents();
    }
    //endregion

    //region Student Getter
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public static Student getStudentWithId(long id) {
        hasValidStudents = loadStudents();
        for (Student stu : students) {
            if (stu.getUserId() == id) return stu;
        }
        return null;
    }

    public static Student getStudentWithUsername(String uname) {
        hasValidStudents = loadStudents();
        for (Student stu : students) {
            if (stu.getUsername().equals(uname)) return stu;
        }
        return null;
    }
    //endregion

    //region Static Init
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private static boolean loadStudents() {
        if (hasValidStudents && students != null) return true;
        if (isLoading) return false;
        isLoading = true;
        String[][] stus = InternalCore.readInfoFile(SEObjectType.STUDENT_USER, null);
        if (stus.length < 1) return false;
        students = new Student[stus.length];
        for (int i = 0; i < stus.length; i++) {
            User tmp = new User(
                    stus[i][0],
                    stus[i][1],
                    stus[i][2],
                    stus[i][3],
                    stus[i][4],
                    stus[i][5],
                    UserType.STUDENT);
            students[i] = new Student(tmp);
        }
        return true;
    }

    public static void addStudent(Student student) {
        int currLength = students.length;
        students = Arrays.copyOf(students, currLength + 1);
        students[currLength] = student;
    }
    //endregion

    //region Elective Registration
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public boolean registerToElective(String courseCode) {
        String[] ids = {Long.toString(this.getUserId())};
        String[][] enrolledElectives = InternalCore.readInfoFile(SEObjectType.STU_ELECT_RELATION, ids);
        String[][] electiveList = InternalCore.readInfoFile(SEObjectType.ELECTIVE, null);
        int courseBlock = 0;

        for (int i = 1; i < electiveList.length; i++) {
            if (electiveList[i][1].equals(courseCode)) {
                courseBlock = Integer.parseInt(electiveList[i][7]);
            }
        }

        switch(courseBlock) {
            case 3:
                if (!(enrolledElectives[0][1].equals(""))) {
                    enrolledElectives[0][1] = courseCode;
                }
                else {
                    Integer userRegistrationChoice = InternalCore.getUserInput(Integer.class, "You are already registered for: "
                            + enrolledElectives[0][1] + " in block: " + courseBlock  + ". Do you want to change your elective choice to: " + courseCode + "?\n "
                            + "(1) Yes"
                            + "(2) No");
                    if (userRegistrationChoice == null) {
                        InternalCore.printIssue("Invalid input.", "Your input was invalid too many times.");
                        return false;
                    }
                    switch(userRegistrationChoice) {
                        case 1:
                            enrolledElectives[0][1] = courseCode;
                            InternalCore.println("You are now registered for: " + courseCode);
                            break;
                        case 2:
                            InternalCore.println("You are now registered for: " + enrolledElectives[0][1]);
                            break;
                    }
                }
                break;
            case 4:
                if (!(enrolledElectives[0][3].equals(""))) {
                    enrolledElectives[0][3] = courseCode;
                }
                else {
                    Integer userRegistrationChoice = InternalCore.getUserInput(Integer.class, "You are already registered for: "
                            + enrolledElectives[0][3] + " in block: " + courseBlock + ". Do you want to change your elective choice to: " + courseCode + "?\n "
                            + "(1) Yes"
                            + "(2) No");
                    if (userRegistrationChoice == null) {
                        InternalCore.printIssue("Invalid input.", "Your input was invalid too many times.");
                        return false;
                    }
                    switch(userRegistrationChoice) {
                        case 1:
                            enrolledElectives[0][3] = courseCode;
                            InternalCore.println("You are now registered for: " + courseCode);
                            break;
                        case 2:
                            InternalCore.println("You are now registered for: " + enrolledElectives[0][3]);
                            break;
                    }
                }
                break;
            case 5:
                if (!(enrolledElectives[0][5].equals(""))) {
                    enrolledElectives[0][5] = courseCode;
                }
                else {
                    Integer userRegistrationChoice = InternalCore.getUserInput(Integer.class, "You are already registered for: "
                            + enrolledElectives[0][5] + " in block: " + courseBlock + ". Do you want to change your elective choice to: " + courseCode + "?\n "
                            + "(1) Yes"
                            + "(2) No");
                    if (userRegistrationChoice == null) {
                        InternalCore.printIssue("Invalid input.", "Your input was invalid too many times.");
                        return false;
                    }
                    switch(userRegistrationChoice) {
                        case 1:
                            enrolledElectives[0][5] = courseCode;
                            InternalCore.println("You are now registered for: " + courseCode);
                            break;
                        case 2:
                            InternalCore.println("You are now registered for: " + enrolledElectives[0][5]);
                            break;
                    }
                }
                break;
        }
        return true;
    }
    //endregion

    //region Enrollment
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public void viewEnrolledElectives() {
        String[] ids = {Long.toString(this.getUserId())};
        String[][] enrolledElectives = InternalCore.readInfoFile(SEObjectType.STU_ELECT_RELATION, ids);
        if (enrolledElectives != null) {
            if (!enrolledElectives[0][1].equals(""))
                InternalCore.println("In block 3 you are enrolled in the following elective: " + enrolledElectives[0][1]);
            if (!enrolledElectives[0][3].equals(""))
                InternalCore.println("In block 4 you are enrolled in the following elective: " + enrolledElectives[0][3]);
            if (!enrolledElectives[0][5].equals(""))
                InternalCore.println("In block 5 you are enrolled in the following elective: " + enrolledElectives[0][5]);
        } else {
            InternalCore.println("You are not yet enrolled in any elective");
        }
    }

    public void viewElectiveProgress(String courseCode) {
        String[] ids = {Long.toString(this.getUserId())};
        String[][] enrolledElectives = InternalCore.readInfoFile(SEObjectType.STU_ELECT_RELATION, ids);
        if (enrolledElectives == null) return; // an error occured
        if (enrolledElectives[0].length > 1) {
            for (int i = 1; i < enrolledElectives[0].length; i += 2) {
                if (enrolledElectives[0][i].equals(courseCode))
                    InternalCore.println("Your progress for " + courseCode + "is: " + enrolledElectives[0][i + 1]);
            }
        } else {
            InternalCore.println("You are not yet enrolled in any elective");
        }
    }
    //endregion
}
