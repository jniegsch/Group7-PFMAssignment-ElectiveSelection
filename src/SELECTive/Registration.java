package SELECTive;

import java.util.ArrayList;

public class Registration {
    //region Private Properties
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private Student student = null;
    private Elective[] electives = null;
    private double[] grades = null;
    //endregion

    //region Getters
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public Student getStudent() {
        return this.student;
    }

    public Elective[] getAllElectives() {
        return electives;
    }

    public Elective getElective(int block) {
        return electives[block - 3];
    }

    public double[] getAllGrades() {
        return grades;
    }

    public double getGrade(int block) {
        return grades[block - 3];
    }

    public double getGrade(Elective elect) {
        if (isNotRegistrationForElective(elect.getCourseCode())) return -1.0;
        return grades[elect.getBlock().getBlockNumber() - 3];
    }

    public boolean isNotRegistrationForElective(Elective elective) {
        return isNotRegistrationForElective(elective.getCourseCode());
    }

    public boolean isNotRegistrationForElective(String courseCode) {
        if (electives[0].getCourseCode().equals(courseCode)) return false;
        if (electives[1].getCourseCode().equals(courseCode)) return false;
        if (electives[2].getCourseCode().equals(courseCode)) return false;
        return true;
    }

    public boolean mayNotViewGrades(User them) {
        if (them.getUserType() == UserType.ADMIN) return false;
        if (them.getUserId() == student.getUserId() && them.getUserType() == UserType.STUDENT) return false;
        return mayNotEditGrades(them);
    }

    public boolean mayNotEditGrades(User them) {
        for (Elective elective : electives) {
            if (elective.getLecturerId() == them.getUserId() && them.getUserType() == UserType.LECTURER) return false;
        }
        return true;
    }

    public boolean mayNotAdaptRegistration(User them) {
        if (them.getUserType() == UserType.ADMIN) return false;
        if (them.getUserType() == UserType.STUDENT && them.getUserId() == student.getUserId()) return false;
        return true;
    }
    //endregion

    //region Private Static Properties
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private static Registration[] registrations = null;
    private static boolean hasValidRegistrations = false;
    //endregion

    //region Constructors
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public Registration() {
        hasValidRegistrations = loadRegistrations();
    }

    public Registration(Student std, Elective[] elects, double[] grds) {
        this();
        this.student = std;
        this.electives = elects;
        this.grades = grds;
    }
    //endregion

    //region Public Retrieval
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public static Registration registrationForStudent(Student stu) {
        hasValidRegistrations = loadRegistrations();
        for (Registration registration : registrations) {
            if (registration.student.getUserId() == stu.getUserId()) return registration;
        }
        return null;
    }

    public static Registration[] registrationsForCourse(Elective elect) {
        hasValidRegistrations = loadRegistrations();
        ArrayList<Registration> validRegs = new ArrayList<>();
        for (Registration registration : registrations) {
            if (registration.electives[elect.getBlock().getBlockNumber() - 3].getCourseCode().equals(elect.getCourseCode())) validRegs.add(registration);
        }
        if (validRegs.size() < 1) return null;
        Registration[] regsToReturn = new Registration[validRegs.size()];
        return validRegs.toArray(regsToReturn);
    }
    //endregion

    //region Registration
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public boolean registerForElective(Student stu, Elective elect) {
        if (mayNotAdaptRegistration(stu)) {
            InternalCore.printIssue("Insufficient rights",
                    "This registration is not for the requested student");
            return false;
        }
        if (this.electives[elect.getBlock().getBlockNumber() - 3] != null) {

        }
        return false;
    }
    //endregion

    //region Updating & Access
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public boolean updateGradeForElective(Elective elect, Lecturer lecturer, double grade) {
        if (mayNotEditGrades(lecturer)) {
            InternalCore.printIssue("Invalid access rights",
                    "You are not the professor of this elective and thus cannot edit the grades.");
            return false;
        }

        if (isNotRegistrationForElective(elect.getCourseCode())) {
            InternalCore.printIssue("This registration set does not contain this elective", "");
            return false;
        }

        grades[elect.getBlock().getBlockNumber() - 3] = grade;

        String[] newInfo = {
                electives[0].getCourseCode(),
                Double.toString(grades[0]),
                electives[1].getCourseCode(),
                Double.toString(grades[1]),
                electives[2].getCourseCode(),
                Double.toString(grades[2])
        };
        if (!InternalCore.updateInfoFile(SEObjectType.STU_ELECT_RELATION, Long.toString(this.student.getUserId()), newInfo)) {
            InternalCore.printIssue("Failed to save the file",
                    "If the problem persists, please restart the system. Changes were not saved.");
            hasValidRegistrations = false;
        }
        return hasValidRegistrations;
    }
    //endregion

    //region Backend Loading
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private static boolean loadRegistrations() {
        if (hasValidRegistrations && registrations != null) return true;
        String[][] regs = InternalCore.readInfoFile(SEObjectType.STU_ELECT_RELATION, null);
        registrations = new Registration[regs.length];
        int i = 0;
        for (String[] reg : regs) {
            if (reg.length != 7) continue;
            long studentId = Long.parseLong(reg[0]);
            if (studentId == -1) continue;
            Student tmpStudent = Student.getStudentWithId(studentId);

            Elective[] els = new Elective[3];
            double[] grs = new double[3];

            els[0] = new Elective(reg[1]);
            grs[0] = Double.parseDouble(reg[2]);
            els[0] = new Elective(reg[3]);
            grs[0] = Double.parseDouble(reg[4]);
            els[0] = new Elective(reg[5]);
            grs[0] = Double.parseDouble(reg[6]);

            registrations[i]= new Registration(tmpStudent, els, grs);
            i++;
        }
        if (i == 0) return false;
        return true;
    }
    //endregion
}
