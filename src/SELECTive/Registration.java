package SELECTive;

import java.util.ArrayList;
import java.util.Arrays;

public class Registration {
    //region Private Properties
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private long relationId = -1;
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
        return grades[elect.getBlock() - 3];
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
    private static boolean isLoading = false;
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

    public Registration(long relId, Student std, Elective[] elects, double[] grds) {
        this();
        this.relationId = relId;
        this.student = std;
        this.electives = elects;
        this.grades = grds;
    }
    //endregion

    //region Public Retrieval
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public static Registration registrationForStudent(Student stu) {
        hasValidRegistrations = loadRegistrations();
        if (registrations == null || stu == null) return null;
        for (Registration registration : registrations) {
            if (registration.student.getUserId() == stu.getUserId()) return registration;
        }
        return null;
    }

    public static Registration[] registrationsForCourse(Elective elect) {
        hasValidRegistrations = loadRegistrations();
        if (registrations == null || elect == null) return null;
        ArrayList<Registration> validRegs = new ArrayList<>();
        for (Registration registration : registrations) {
            if (registration.electives[elect.getBlock() - 3].getCourseCode().equals(elect.getCourseCode())) validRegs.add(registration);
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
        if (this.electives[elect.getBlock() - 3] != null) {
            InternalCore.println("You're already enrolled in a course in this block. The course is: \n "
                    + this.electives[elect.getBlock() - 3].toString());
            String changeElective = InternalCore.getUserInput(String.class, "Change elective? (y/n)");
            if (changeElective == null) {
                return false;
            }
            if (changeElective.toLowerCase().equals("y")) {
                this.electives[elect.getBlock() - 3] = elect;
            }
        } else {
            this.electives[elect.getBlock() - 3] = elect;
        }
        return saveRegistration(false);
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

        grades[elect.getBlock() - 3] = grade;

        return saveRegistration(false);
    }

    public boolean saveRegistration(boolean isnew) {
        String[] info = {
                Long.toString(this.student.getUserId()),
                (this.electives[0] != null) ? this.electives[0].getCourseCode() : Elective.invalidCourseID,
                Double.toString(this.grades[0]),
                (this.electives[1] != null) ? this.electives[1].getCourseCode() : Elective.invalidCourseID,
                Double.toString(this.grades[1]),
                (this.electives[2] != null) ? this.electives[2].getCourseCode() : Elective.invalidCourseID,
                Double.toString(this.grades[2])
        };
        if (isnew) {
            if ((relationId = InternalCore.addEntryToInfoFile(SEObjectType.STU_ELECT_RELATION, info)) == -1) {
                InternalCore.printIssue("Failed to save the file",
                        "If the problem persists, please restart the system. Changes were not saved.");
                hasValidRegistrations = false;
            }
        } else {
            if (!InternalCore.updateInfoFile(SEObjectType.STU_ELECT_RELATION, Long.toString(this.student.getUserId()), Arrays.copyOfRange(info, 1, info.length))) {
                InternalCore.printIssue("Failed to save the file",
                        "If the problem persists, please restart the system. Changes were not saved.");
                hasValidRegistrations = false;
            }
        }

        addRegistration(this);
        return hasValidRegistrations;
    }
    //endregion

    //region Backend Loading
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private static boolean loadRegistrations() {
        if (hasValidRegistrations) return true;
        if (isLoading) return false;
        isLoading = true;
        String[][] regs = InternalCore.readInfoFile(SEObjectType.STU_ELECT_RELATION, null);
        if (regs == null) return true;
        registrations = new Registration[regs.length];
        int i = 0;
        for (String[] reg : regs) {
            if (reg.length != 8) continue;
            long relId = Long.parseLong(reg[0]);
            if (relId == -1) continue;
            long studentId = Long.parseLong(reg[1]);
            if (studentId == -1) continue;
            Student tmpStudent = Student.getStudentWithId(studentId);

            Elective[] els = new Elective[3];
            double[] grs = new double[3];

            els[0] = Elective.getElectiveWithCourseCode(reg[2]);
            grs[0] = Double.parseDouble(reg[3]);
            els[1] = Elective.getElectiveWithCourseCode(reg[4]);
            grs[1] = Double.parseDouble(reg[5]);
            els[2] = Elective.getElectiveWithCourseCode(reg[6]);
            grs[2] = Double.parseDouble(reg[7]);

            registrations[i] = new Registration(relId, tmpStudent, els, grs);
            i++;
        }
        if (i == 0) return false;
        return true;
    }

    public static void addRegistration(Registration registration) {
        if (alreadyHasLoaded(registration)) return;
        int currLength = 0;
        if (registrations != null) {
            currLength = registrations.length;
            registrations = Arrays.copyOf(registrations, currLength + 1);
        } else {
            registrations = new Registration[1];
        }
        registrations[currLength] = registration;
    }

    private static boolean alreadyHasLoaded(Registration registration) {
        hasValidRegistrations = loadRegistrations();
        if (registrations == null) return false;
        for (Registration reg : registrations) {
            if (reg.equals(registration)) return true;
        }
        return false;
    }
    //endregion

    //region Overrides
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public boolean equals(Object obj) {
        if (obj instanceof Registration) {
            Registration object = (Registration) obj;
            return this.student.getUserId() == object.student.getUserId();
        }
        return super.equals(obj);
    }
    //endregion
}
