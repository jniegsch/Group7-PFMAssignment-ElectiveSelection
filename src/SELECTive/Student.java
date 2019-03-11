package SELECTive;

import java.util.Arrays;

public class Student extends User {
    //region Static Properties
    private static Student[] students = null;
    private static boolean hasValidStudents = false;
    private static boolean isLoading = false;
    //endregion

    //region Constructor
    public Student() {
        hasValidStudents = loadStudents();
    }

    public Student(User base) {
        super(base, UserType.STUDENT);
        hasValidStudents = loadStudents();
    }
    //endregion

    //region Student Retrievers
    //This method returns a specific student object 
    public static Student getStudentWithId(long id) {
        hasValidStudents = loadStudents();
        if (students == null) return null;
        for (Student stu : students) {
            if (stu.getUserId() == id) return stu;
        }
        return null;
    }

    //This method returns a specific student object 
    public static Student getStudentWithUsername(String uname) {
        hasValidStudents = loadStudents();
        if (students == null) return null;
        for (Student stu : students) {
            if (stu.getUsername().equals(uname)) return stu;
        }
        return null;
    }

    //This method returns all student objects 
    public static Student[] getAllStudents(Admin admin) {
        if (!admin.isValidAdmin()) return null;
        hasValidStudents = loadStudents();
        return students;
    }
    //endregion

    //region Static Init
    //This method returns the student list 
    private static boolean loadStudents() {
        if (hasValidStudents) return true;
        if (isLoading) return false;
        isLoading = true;
        String[][] stus = InternalCore.readInfoFile(SEObjectType.STUDENT_USER, null);
        if (stus == null) return true;
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

    //This method adds a student to the student list 
    public static void addStudent(Student student) {
        if (alreadyHasLoaded(student)) return;
        int currLength = 0;
        if (students != null) {
            currLength = students.length;
            students = Arrays.copyOf(students, currLength + 1);
        } else {
            students = new Student[1];
        }
        students[currLength] = student;
    }

    //This method checks whether students have already been loaded 
    private static boolean alreadyHasLoaded(Student student) {
        hasValidStudents = loadStudents();
        if (students == null) return false;
        for (Student stu : students) {
            if (stu.equals(student)) return true;
        }
        return false;
    }
    //endregion

    //region Enrollment
    //This method allows student users to register themselves to an elective 
    public void viewEnrolledElectives() {
        Registration reg = Registration.registrationForStudent(this);
        if (reg == null) {
            InternalCore.println("You are not yet enrolled in any electives");
            return;
        }
        Elective[] enrolledElectives = reg.getAllElectives();
        if (enrolledElectives != null) {
            if (enrolledElectives[0] != null)
                InternalCore.println("In block 3 you are enrolled in the following elective:\n "
                        + enrolledElectives[0].toString());
            if (enrolledElectives[1] != null)
                InternalCore.println("In block 4 you are enrolled in the following elective:\n "
                        + enrolledElectives[1].toString());
            if (enrolledElectives[2] != null)
                InternalCore.println("In block 5 you are enrolled in the following elective:\n "
                        + enrolledElectives[2].toString());
        } else {
            InternalCore.println("You are not yet enrolled in any electives");
        }
    }

    //This method allows student users to view their grades for electives to which they are registered
    public void viewElectiveProgress(String courseCode) {
        Registration reg = Registration.registrationForStudent(this);
        if (reg == null) {
            InternalCore.printIssue("You are not yet enrolled in any electives", "");
            return;
        }
        Elective elective = Elective.getElectiveWithCourseCode(courseCode);
        if (elective == null) {
            InternalCore.printIssue("The elective you requested does not exist", "");
            return;
        }
        if (reg.isNotRegisteredForElective(elective)) {
            InternalCore.printIssue("You are not enrolled for this course",
                    "If you should be, please register");
            return;
        }
        if (reg.getGrade(elective) == -1) {
            InternalCore.println("You have not received a grade for " + courseCode + " yet.");
            return;
        }
        InternalCore.println("Your progress for " + courseCode + " is: " + reg.getGrade(elective));
    }
    
    //This method allows student users to view their grades for electives to which they are registered
    public void viewProgress() {
        Registration reg = Registration.registrationForStudent(this);
        if (reg == null) {
            InternalCore.println("You are not yet enrolled in any electives");
            return;
        }
        if (reg.getAllElectives() == null) {
            InternalCore.println("You are not yet enrolled in any electives");
            return;
        }
        for (Elective elective : reg.getAllElectives()) {
            if (elective == null) continue;
            if (reg.getGrade(elective) == -1) {
                InternalCore.println("You have not received a grade for " + elective.getCourseCode() + " yet.");
                continue;
            }
            InternalCore.println("Your progress for " + elective.getCourseCode() + " is: " + reg.getGrade(elective));
        }
    }
    //endregion
}
