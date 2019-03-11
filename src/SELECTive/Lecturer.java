package SELECTive;

import java.util.Arrays;

public class Lecturer extends User {
    public enum Title {
        NO_TITLE,
    	MR,
        MRS,
        MS,
        DR,
        PROF
    }

    //region Static Properties
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private static Lecturer[] lecturers = null;
    private static boolean hasvalidLecturers = false;
    private static boolean isLoading = false;
    //endregion

    //region Private Properties
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private Title title = Title.NO_TITLE;
    //endregion

    //region Getters
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    //This method returns the title of a specific lecturer user
    public Title getTitle() {
        return this.title;
    }
    //endregion

    //region Constructor
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public Lecturer() {
        hasvalidLecturers = loadLecturers();
    }

    public Lecturer(User base) {
        super(base, UserType.LECTURER);
        hasvalidLecturers = loadLecturers();
    }

    public Lecturer(User base, Title titl) {
        this(base);
        this.title = titl;
    }
    //endregion

    //region Static Init
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    //This method loads the lecturer objects from the file
    private static boolean loadLecturers() {
        if (hasvalidLecturers) return true;
        if (isLoading) return false;
        isLoading = true;
        String[][] lects = InternalCore.readInfoFile(SEObjectType.LECTURER_USER, null);
        if (lects == null) return true;
        lecturers = new Lecturer[lects.length];
        for (int i = 0; i < lects.length; i++) {
            User tmp = new User(
                    lects[i][0],
                    lects[i][1],
                    lects[i][2],
                    lects[i][3],
                    lects[i][4],
                    lects[i][5],
                    UserType.LECTURER);
            lecturers[i] = new Lecturer(tmp);
            lecturers[i].title = Title.valueOf(lects[i][6]);
        }
        return true;
    }

    //This method adds a lecturer object to the lecturer list
    public static void addLecturer(Lecturer lecturer) {
        if (alreadyHasLoaded(lecturer)) return;
        int currLength = 0;
        if (lecturers != null) {
            currLength = lecturers.length;
            lecturers = Arrays.copyOf(lecturers, currLength + 1);
        } else {
            lecturers = new Lecturer[1];
        }
        lecturers[currLength] = lecturer;
    }

    //This method checks whether a lecturer has already been loaded
    private static boolean alreadyHasLoaded(Lecturer lecturer) {
        hasvalidLecturers = loadLecturers();
        if (lecturers == null) return false;
        for (Lecturer lec : lecturers) {
            if (lec.equals(lecturer)) return true;
        }
        return false;
    }
    //endregion

    //region Student Getter
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    //This method returns a specific lecturer object, based on its ID
    public static Lecturer getLecturerWithId(long id) {
        hasvalidLecturers = loadLecturers();
        if (lecturers == null) return null;
        for (Lecturer lec : lecturers) {
            if (lec.getUserId() == id) return lec;
        }
        return null;
    }

    //This method returns a specific lecturer object, based on its user name
    public static Lecturer getLecturerWithUsername(String uname) {
        hasvalidLecturers = loadLecturers();
        if (lecturers == null) return null;
        for (Lecturer lec : lecturers) {
            if (lec.getUsername().equals(uname)) return lec;
        }
        return null;
    }

    //This method returns all lecturer objects
    public static Lecturer[] getAllLecturers(Admin admin) {
        if (!admin.isValidAdmin()) return null;
        hasvalidLecturers = loadLecturers();
        return lecturers;
    }
    //endregion

    //region Instance Editing
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    //This method edits the title of a specific lecturer
    public boolean editTitle() {
        int optId = 0;
        Title[] titles = Title.values();
        InternalCore.println("Possible title choices are:");
        for (Title title : titles) {
            InternalCore.println("(" + (++optId) + ") " + InternalCore.capitalizeString(title.toString()));
        }
        Integer newTitleChoice = InternalCore.getUserInput(Integer.class, "What is the new title of this lecturer?");
        if (newTitleChoice == null) return false;
        if (newTitleChoice < 1 || newTitleChoice > optId) return false;
        this.title = titles[newTitleChoice - 1];
        return true;
    }
    //endregion

    //region Stringify Override
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    //This method turns values into a string
    public String toString() {
        String userRep = super.toString();
        userRep = userRep.replaceAll("]", "] " + InternalCore.capitalizeString(title.toString()) + ".");
        return userRep;
    }
    //endregion

    //region Elective Management
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    //This method returns an elective, based on the given course code
    private Elective getProperElective(String courseCode) {
        Elective elective = Elective.getElectiveWithCourseCode(courseCode);
        if (elective == null) {
            InternalCore.printIssue("The requested elective does not exist!", "Make sure the elective actually exists or request ");
            return null;
        }
        if (elective.mayNotAccessElective(this)) return null;
        return elective;
    }

    private Elective elective = null;

    //This method returns an array of registration objects (registered students), based on the given course code
    private Registration[] getProperRegistrations(String courseCode) {
        this.elective = getProperElective(courseCode);
        if (this.elective == null) return null;

        Registration[] registrations = Registration.registrationsForCourse(elective);
        if (registrations == null) {
            InternalCore.println("> No students are enrolled in your course.");
            return null;
        }
        return registrations;
    }

    //This method returns a registration, based on the given course code and student
    private Registration getProperRegistration(String courseCode, Student student) {
        this.elective = getProperElective(courseCode);
        if (this.elective == null) return null;

        Registration registration = Registration.registrationForStudent(student);
        if (registration == null) {
            InternalCore.printIssue("No such student exists",
                    "A student with such a username does not exist");
            return null;
        }

        return registration;
    }

    //This method enables a lecturer to enter or edit the grade of a registered student for one of his/her electives
    public void newGradeEntry(){
        // Loop to enter student grades until break
        String courseCode = null;
        while (true) {
            if (this.elective == null) {
                courseCode = InternalCore.getUserInput(String.class,
                        "Please enter the elective course code for which you would like to add a grade:");
                this.elective = getProperElective(courseCode);
                if (this.elective == null) break;
            }

            String studentUsername = InternalCore.getUserInput(String.class,
                    "Please enter the student username for which you would like to add a grade:");

            Student student = Student.getStudentWithUsername(studentUsername);
            if (student == null) {
                InternalCore.printIssue("No such student exists",
                        "A student with such a username does not exist");
                break;
            }

            Registration registration = getProperRegistration(courseCode, student);
            if (registration == null) continue;

            if (registration.isNotRegisteredForElective(this.elective)) {
                InternalCore.printIssue("Student is not registered",
                        "Looks like the student is not registered for this elective. " +
                                "If they should be, contact the user.");
                continue;
            }

            Double newGrade = InternalCore.getUserInput(Double.class,
                    "Please enter the student's grade for the elective: ");
            if (newGrade == null) {
                InternalCore.printIssue("Too many invalid inputs",
                        "Please try registering a new grade again");
                continue;
            }

            if (!registration.updateGradeForElective(this.elective, this, newGrade)) {
                String retry = InternalCore.getUserInput(String.class,
                        "Seeing as there was an issue, would you like to retry? (y/n)");
                if (retry != null) if (retry.toLowerCase().equals("y")) continue;
            }

            //Allowing the lecturer to add additional grades for other students
            String addMoreGrades = InternalCore.getUserInput(String.class,
                    "Would you like to add another grade? (y/n)");
            if (addMoreGrades == null) break;
            if (addMoreGrades.toLowerCase().equals("y")) {
                String sameCourse = InternalCore.getUserInput(String.class,
                        "Is this for the same elective? (y/n))");
                if (sameCourse != null) if (sameCourse.toLowerCase().equals("y")) continue;
                elective = null;
                continue;
            }
            break;
        }
        this.elective = null;
    }

    // This method prints out a list of registered students for a particular elective
    public void showStudents(String courseCode){
        Registration[] registrations = getProperRegistrations(courseCode);
        if (registrations == null) return;

        InternalCore.println("The following students are enrolled in the course " + courseCode + ": ");
        for (Registration registration : registrations) {
            InternalCore.println("> " + registration.getStudent().toString());
        }
    }

    // This method prints out a list of student grades for a particular elective
    public void showStudentGrades(String courseCode){
        Registration[] registrations = getProperRegistrations(courseCode);
        if (registrations == null) return;

        InternalCore.println("The grades for the students enrolled in the course " + courseCode + " are: ");
        for (Registration registration : registrations) {
            InternalCore.print("> " + registration.getStudent().toString());
            double grade = registration.getGrade(this.elective);
            if (grade == -1) {
                InternalCore.print(" did not receive a grade yet.");
            } else {
                InternalCore.print(" got a " + grade);
            }
            InternalCore.println();
        }
        this.elective = null;
    }

    //This method prints out an overview over the grade statistics of a given course
    public void viewStatsForElective(String courseCode) {
        Registration[] registrations = getProperRegistrations(courseCode);
        if (registrations == null) return;

        double[] grades = new double[registrations.length];
        for (int i = 0; i < grades.length; i++) {
            grades[i] = registrations[i].getGrade(elective);
        }

        InternalCore.printTitle("Stats for " + this.elective.getCourseCode(), '-');
        double minGrade = minGrade(grades);
        InternalCore.println("The minimum value of all grades is: " + minGrade);
        double maxGrade = maxGrade(grades);
        InternalCore.println("The maximum value of all grades is: " + maxGrade);
        double meanGrade = meanGrade(grades);
        InternalCore.println("The average value of all grades is: " + meanGrade);
        double failedGrade = failedGrade(grades);
        InternalCore.println("The number of failed grades are: " + failedGrade);

        this.elective = null;
    }

    // This method gets regStudents as input and returns its minimum value
    private double minGrade(double[] numElectiveGrade){
        double minGrade = numElectiveGrade[0];
        for (int i = 1; i < numElectiveGrade.length; i++) {
            if (numElectiveGrade[i] < minGrade) {
                minGrade = numElectiveGrade [i];
            }
        }
        return minGrade;
    }

    // This method gets regStudents as input and returns its maximum value
    private double maxGrade(double[] numElectiveGrade){
        double maxGrade = numElectiveGrade[0];
        for (int i = 1; i < numElectiveGrade.length; i++) {
            if (numElectiveGrade[i] > maxGrade) {
                maxGrade = numElectiveGrade[i];
            }
        }
        return maxGrade;
    }

    // This method gets regStudents as input and returns its average
    private double meanGrade(double[] numElectiveGrade){
        double sumGrade = numElectiveGrade[0];
        for (int i = 1; i < numElectiveGrade.length; i++) {
            sumGrade += numElectiveGrade[i];
        }
        return sumGrade / numElectiveGrade.length;
    }

    // This method gets regStudents as input and returns the number of failed grades
    private double failedGrade(double[] numElectiveGrade){
        double failedGrade = 0;
        for (double grade : numElectiveGrade) {
            if (grade < 5.5) {
                failedGrade ++;
            }
        }
        return failedGrade;
    }
}
