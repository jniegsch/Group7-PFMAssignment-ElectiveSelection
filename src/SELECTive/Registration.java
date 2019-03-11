package SELECTive;

import java.util.ArrayList;
import java.util.Arrays;

public class Registration {
    //region Private Properties
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    /**
     * Property storing the id identifying the relation
     */
    private long relationId = -1;
    /**
     * Property for the student for which the registration is
     */
    private Student student = null;
    /**
     * Property storing the three electives
     */
    private Elective[] electives = null;
    /**
     * Property storing the three grades for the electives
     */
    private double[] grades = null;
    //endregion

    //region Getters
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /**
     * Method to get student object of the relation
     *
     * @return The specific {@code Student}
     */
    public Student getStudent() {
        return this.student;
    }

    /**
     * Method to get all electives of the registration
     * @return The specific {@code Elective} array
     */
    public Elective[] getAllElectives() {
        return electives;
    }

    /**
     * Method to get a specific grade for an elective
     * @param elect the elective for which the grade should be returned
     * @return the grade or {@code -1.0} if no valid grade exists
     */
    public double getGrade(Elective elect) {
        if (elect == null) return -1.0;
        if (isNotRegisteredForElective(elect)) return -1.0;
        return grades[elect.getBlock() - 3];
    }

    /**
     * Method checks whether a user is not registered to an elective, based on a given elective object
     * @param elective  the elective to check if the student is registered for
     * @return a {@code boolean} representing if the student is <b>NOT</b> registered
     */
    public boolean isNotRegisteredForElective(Elective elective) {
        return isNotRegisteredForElective(elective.getCourseCode());
    }

    /**
     * Method checks whether a user is not registered to an elective, based on a given elective's course code
     * @param courseCode the course code of the elective to check
     * @return a {@code boolean} representing if the student is <b>NOT</b> registered
     */
    private boolean isNotRegisteredForElective(String courseCode) {
        if (electives[0] != null) if (electives[0].getCourseCode().equals(courseCode)) return false;
        if (electives[1] != null) if (electives[1].getCourseCode().equals(courseCode)) return false;
        if (electives[2] != null) return !(electives[2].getCourseCode().equals(courseCode));
        return true;
    }

    /**
     * Method to check whether user may not edit a grade
     * @param them      The user requesting to edit
     * @param elective  The specific elective
     * @return a {@code boolean} representing if the user may <b>NOT</b> edit the grade
     */
    private boolean mayNotEditGrade(User them, Elective elective) {
        return !(elective.getLecturerId() == them.getUserId() && them.isValidLecturer());
    }

    /**
     * Method to check whether user may adapt a registration
     * @param them      The user requesting to edit
     * @return a {@code boolean} representing if the user may <b>NOT</b> edit the registration
     */
    private boolean mayNotAdaptRegistration(User them) {
        if (them.isValidAdmin()) return false;
        return !(them.isValidStudent() && them.getUserId() == student.getUserId());
    }
    //endregion

    //region Private Static Properties
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    /**
     * Local static storage of all the registrations to minimize I/O
     */
    private static Registration[] registrations = null;
    /**
     * Local static flag indicating if the {@code registrations} array is valid or needs to be reloaded
     */
    private static boolean hasValidRegistrations = false;
    /**
     * Local static flag indicating if the {@code registration} array is currently being loaded
     */
    private static boolean isLoading = false;
    //endregion

    //region Constructors
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /**
     * Private base constructor that ensures the {@code registration} array is loaded
     */
    private Registration() {
        hasValidRegistrations = loadRegistrations();
    }

    /**
     * Constructor to create a registration
     * @param std       The student of the registration
     * @param elects    The electives of the registration
     * @param grds      The grades of the student for the electives registered to
     */
    public Registration(Student std, Elective[] elects, double[] grds) {
        this();
        this.student = std;
        this.electives = elects;
        this.grades = grds;
    }

    /**
     * Constructor to create a registration
     * @param relId     The relation id of the registration
     * @param std       The student of the registration
     * @param elects    The electives of the registration
     * @param grds      The grades of the student for the electives registered to
     */
    private Registration(long relId, Student std, Elective[] elects, double[] grds) {
        this();
        this.relationId = relId;
        this.student = std;
        this.electives = elects;
        this.grades = grds;
    }
    //endregion

    //region Public Retrieval 
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /**
     * Method returns a registration of a student, based on a specific student object
     * @param stu   The student for which to retrieve the registration
     * @return The registration or null if none exists
     */
    public static Registration registrationForStudent(Student stu) {
        hasValidRegistrations = loadRegistrations();
        if (registrations == null || stu == null) return null;
        for (Registration registration : registrations) {
            if (registration.student.getUserId() == stu.getUserId()) return registration;
        }
        return null;
    }

    /**
     * Method returns an array of registrations which all include a the elective
     * @param elect the elective
     * @return an array of registrations or null if none exist
     */
    public static Registration[] registrationsForCourse(Elective elect) {
        hasValidRegistrations = loadRegistrations();
        if (registrations == null || elect == null) return null;
        ArrayList<Registration> validRegs = new ArrayList<>();
        for (Registration registration : registrations) {
            if (registration.electives[elect.getBlock() - 3] == null) continue;
            if (registration.electives[elect.getBlock() - 3].getCourseCode().equals(elect.getCourseCode())) validRegs.add(registration);
        }
        if (validRegs.size() < 1) return null;
        Registration[] regsToReturn = new Registration[validRegs.size()];
        return validRegs.toArray(regsToReturn);
    }
    //endregion

    //region Registration
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /**
     * Method to register for an elective
     * @param stu   The student to register
     * @param elect The elective to which a student should be registered
     * @return a {@code boolean} representing if the registration was successful
     */
    public boolean registerForElective(Student stu, Elective elect) {
        if (mayNotAdaptRegistration(stu)) {
            InternalCore.printIssue("Insufficient rights",
                    "This registration is not for the requested student");
            return false;
        }
        if (this.electives[elect.getBlock() - 3] != null) {
            if (this.grades[elect.getBlock() - 3] != -1) {
                InternalCore.println("You're already enrolled in a course in this block and have received a grade. " +
                        "Thus you can no longer change your elective. The course is: \n "
                        + this.electives[elect.getBlock() - 3].toString());
                return false;
            }
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

    /**
     * Method to update the grade a student scored for an elective
     * @param elect     The elective for which to change the grade
     * @param lecturer  The lecturer requesting the change
     * @param grade     The new grade
     * @return a {@code boolean} representing if the change was successful
     */
    public boolean updateGradeForElective(Elective elect, Lecturer lecturer, double grade) {
        if (mayNotEditGrade(lecturer, elect)) {
            InternalCore.printIssue("Invalid access rights",
                    "You are not the professor of this elective and thus cannot edit the grades.");
            return false;
        }

        if (isNotRegisteredForElective(elect.getCourseCode())) {
            InternalCore.printIssue("This registration set does not contain this elective", "");
            return false;
        }

        grades[elect.getBlock() - 3] = grade;

        return saveRegistration(false);
    }

    /**
     * Method to save a student registration to a course
     * @param isnew a {@code boolean} identifying if the registration is new and should thus be added to the file
     *              or if an existing registration should be changed
     * @return a {@code boolean} indicating if the saving was successful
     */
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
            if ((this.relationId = InternalCore.addEntryToInfoFile(SEObjectType.STU_ELECT_RELATION, info)) == -1) {
                InternalCore.printIssue("Failed to save the file",
                        "If the problem persists, please restart the system. Changes were not saved.");
                hasValidRegistrations = false;
            }
        } else {
            if (!InternalCore.updateInfoFile(SEObjectType.STU_ELECT_RELATION, this.relationId, info)) {
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

    /**
     * Method to load all registration entries from the file and create an object for every instance. Furthermore, the
     * instances are stored statically so all others can also access them without loading again from the file
     *
     * @return a {@code boolean} indicating if the load was successful
     */
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
        isLoading = false;
        return true;
    }

    /**
     * Method to add a registration into the static "memory"
     * @param registration The registration to add
     */
    private static void addRegistration(Registration registration) {
        if (alreadyHasLoaded(registration)) {
            hasValidRegistrations = true;
            return;
        }
        int currLength = 0;
        if (registrations != null) {
            currLength = registrations.length;
            registrations = Arrays.copyOf(registrations, currLength + 1);
        } else {
            registrations = new Registration[1];
        }
        registrations[currLength] = registration;
        hasValidRegistrations = true;
    }

    /**
     * Method checks whether a registration has already been loaded into the static "memory"
     * @param registration The registration to check if it has been loaded
     * @return a {@code boolean} indicating if it already is ({@code true}) or not ({@code false})
     */
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

    /**
     * Method checks whether a given registration equals the current registration instance. For this the student
     * for which the registration is as well as the relation id are evaluated to see if they are the same. If the obj
     * is not and instance of the Registration class, the comparison is passed up to the super class
     * @param obj the {@code Object} to compare to the the current instance
     * @return a {@code boolean} indicating if the two instances are equal
     */
    public boolean equals(Object obj) {
        if (obj instanceof Registration) {
            Registration object = (Registration) obj;
            return (this.student.getUserId() == object.student.getUserId() && this.relationId == object.relationId);
        }
        return super.equals(obj);
    }
    //endregion
}
