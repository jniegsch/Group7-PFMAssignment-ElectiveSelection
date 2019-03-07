package SELECTive;

import java.util.Arrays;

public class Lecturer extends User {
    public enum Title {
        MR,
        MRS,
        DR,
        DEFAULT
    }

    //region Static Properties
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private static Lecturer[] lecturers = null;
    private static boolean hasvalidLecturers = false;
    //endregion

    //region Private Properties
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private Title title = Title.DEFAULT;
    //endregion

    //region Getters
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
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
    //endregion

    //region Static Init
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private static boolean loadLecturers() {
        if (hasvalidLecturers && lecturers != null) return true;
        String[][] lects = InternalCore.readInfoFile(SEObjectType.LECTURER_USER, null);
        if (lects.length < 1) return false;
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

    public static void addLecturer(Lecturer lecturer) {
        int currLength = lecturers.length;
        lecturers = Arrays.copyOf(lecturers, currLength + 1);
        lecturers[currLength] = lecturer;
    }
    //endregion

    //region Student Getter
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public static Lecturer getLecturerWithId(long id) {
        hasvalidLecturers = loadLecturers();
        for (Lecturer lec : lecturers) {
            if (lec.getUserId() == id) return lec;
        }
        return null;
    }

    public static Lecturer getLecturerWithUsername(String uname) {
        hasvalidLecturers = loadLecturers();
        for (Lecturer lec : lecturers) {
            if (lec.getUsername().equals(uname)) return lec;
        }
        return null;
    }
    //endregion

    //region Instance Editing
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public boolean editTitle() {
        Title newTitle = InternalCore.getUserInput(Title.class, "What is the new title of this lecturer?");
        if (newTitle == null) return false;
        this.title = newTitle;
        return true;
    }
    //endregion

    //region Grade Management
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public void newGradeEntry(){
        // Loop to enter student grades until break
        Elective elective = null;
        while (true) {
            if (elective == null) {
                String courseCode = InternalCore.getUserInput(String.class,
                        "Please enter the elective course code for which you would like to add a grade:");
                elective = Elective.getElectiveWithCourseCode(courseCode);
            }



            String studentUsername = InternalCore.getUserInput(String.class,
                    "Please enter the student username for which you would like to add a grade:");

            Student student = Student.getStudentWithUsername(studentUsername);
            Registration registration = Registration.registrationForStudent(student);

            if (registration.isNotRegistrationForElective(elective)) {
                InternalCore.printIssue("Student is not registered",
                        "Looks like the student is not registered for this elective. " +
                                "If they should be, contact an admin.");
                continue;
            }

            Double newGrade = InternalCore.getUserInput(Double.class,
                    "Please enter the student's grade for the elective: ");
            if (newGrade == null) {
                InternalCore.printIssue("Too many invalid inputs",
                        "Please try registering a new grade again");
                continue;
            }

            if (!registration.updateGradeForElective(elective, this, newGrade)) {
                String retry = InternalCore.getUserInput(String.class,
                        "Seeing as there was an issue, would you like to retry? (y/n)");
                if (retry.toLowerCase().equals("y")) continue;
            }

            String addMoreGrades = InternalCore.getUserInput(String.class,
                    "Would you like to add another grade? (y/n)");

            if (addMoreGrades.toLowerCase().equals("y")) {
                String sameCourse = InternalCore.getUserInput(String.class,
                        "Is this for the same elective? (y/n))");
                if (sameCourse.toLowerCase().equals("y")) continue;
                elective = null;
                continue;
            }
            break;
        }
    }

    // This method prints out a list of registered students for a particular elective
    public void showStudents(String courseCode){
        Elective elective = Elective.getElectiveWithCourseCode(courseCode);
        Registration[] registrations = Registration.registrationsForCourse(elective);
        InternalCore.println("The following students are enrolled in the course " + courseCode + ": ");
        for (Registration registration : registrations) {
            InternalCore.println("> " + registration.getStudent().toString());
        }
    }

    public void showAllStudents() {
        Elective[] electives = Elective.getAllElectivesForLecturer(this);
        for (Elective elective : electives) {
            showStudents(elective.getCourseCode());
        }
    }

    // This method prints out a list of student grades for a particular elective
    public void showStudentGrades(String courseCode){

        InternalCore.println("The grades for the following course are: ");

    }

    private double[] getGradesForElective(String courseCode) {
        String[][] electiveGrade = InternalCore.readInfoFile(SEObjectType.STU_ELECT_RELATION, null);
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < electiveGrade.length; i++) {
            if (electiveGrade[i][1].equals(courseCode)) {
                buffer.append(electiveGrade[i][2]).append(" ");
                continue;
            }
            if (electiveGrade[i][3].equals(courseCode)) {
                buffer.append(electiveGrade[i][4]).append(" ");
                continue;
            }
            if (electiveGrade[i][5].equals(courseCode)) {
                buffer.append(electiveGrade[i][6]).append(" ");
                continue;
            }
        }
        String[] gradeDump = buffer.toString().split(" ");

        double[] numElectiveGrade = new double[gradeDump.length];
        for (int j = 0; j < gradeDump.length; j++) {
            numElectiveGrade[j] = Integer.parseInt(gradeDump[j]);
        }

        return numElectiveGrade;
    }

    public boolean viewStatsForElective(Elective elective) {
        if (this.getUserType() != UserType.ADMIN) {
            if (this.getUserType() == UserType.LECTURER) {
                if (elective.getLecturerId() != this.getUserId()) {
                    InternalCore.printIssue("Insufficient rights", "You do not have the rights to view the elective statistics of a course you do not teach.");
                    return false;
                }
            } else {
                InternalCore.printIssue("Insufficient rights", "You do not have the rights to view elective statistics.");
                return false;
            }
        }

        double[] grades = getGradesForElective(elective.getCourseCode());
        InternalCore.println("The grade statistics for " + elective.getElectiveName() + " are:");
        dataStats(grades);
        return true;
    }

    public boolean viewStatsForElective(Elective[] electives) {
        for (Elective e : electives) {
            if (!viewStatsForElective(e)) return false;
            InternalCore.println(" ");
        }
        return true;
    }

    // This method prints out the min, max, and the average of the grades
    private void dataStats(double[] numElectiveGrade){
        if (this.getUserType() != UserType.LECTURER && this.getUserType() != UserType.ADMIN) {
            InternalCore.printIssue("Insufficient rights", "You do not have the rights to view elective statistics.");
            return;
        }

        // Call the minArray(), maxArray(), and meanArray() methods and use the local array myArray as input to compute the minimum, maximum, and mean values of all heights in the file
        double minGrade = minGrade(numElectiveGrade);
        InternalCore.println("The minimum value of all grades is: " + minGrade);
        double maxGrade = maxGrade(numElectiveGrade);
        InternalCore.println("The maximum value of all grades is: " + maxGrade);
        double meanGrade = meanGrade(numElectiveGrade);
        InternalCore.println("The average value of all grades is: " + meanGrade);
        double failedGrade = failedGrade(numElectiveGrade);
        InternalCore.println("The number of failed grades are: " + failedGrade);
    }

    // This method gets regStudents as input and returns its minimum value
    private double minGrade(double[] numElectiveGrade){
        double minGrade = numElectiveGrade[0];
        for (int i = 0; i < numElectiveGrade.length; i++) {
            if (numElectiveGrade[i] < minGrade) {
                minGrade = numElectiveGrade [i];
            }
        }
        return minGrade;
    }

    // This method gets regStudents as input and returns its maximum value
    private double maxGrade(double[] numElectiveGrade){
        double maxGrade = numElectiveGrade[0];
        for (int i = 0; i < numElectiveGrade.length; i++) {
            if (numElectiveGrade[i] > maxGrade) {
                maxGrade = numElectiveGrade[i];
            }
        }
        return maxGrade;
    }

    // This method gets regStudents as input and returns its average
    private double meanGrade(double[] numElectiveGrade){
        double sumGrade = 0;
        for (int i = 0; i < numElectiveGrade.length; i++) {
            sumGrade += numElectiveGrade[i];
        }
        return (double) sumGrade / numElectiveGrade.length;
    }

    // This method gets regStudents as input and returns the number of failed grades
    private double failedGrade(double[] numElectiveGrade){
        double failedGrade = 0;
        for (int i = 0; i < numElectiveGrade.length; i++) {
            if(numElectiveGrade[i] < 5.5) {
                failedGrade ++;
            }
        }
        return failedGrade;
    }
}
