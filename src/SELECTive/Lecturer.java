package SELECTive;

public class Lecturer extends User {
    //region Constructor
    public Lecturer(User base) {
        super(base, UserType.LECTURER);
    }

    public enum Title {
        MR,
        MRS,
        DR,
        DEFAULT
    }
    private Title title = Title.DEFAULT;

    public void newGradeEntry(){
        // Loop to enter student grades until break
        while (true) {
            String studentIdInput = InternalCore.getUserInput(String.class,
                    "Please enter the student ID for which you would like to add a grade:");
            String [] studentToChangeGradeFor = {studentIdInput};
            String [][] localCopy = InternalCore.readInfoFile(SEObjectType.STU_ELECT_RELATION, studentToChangeGradeFor);

            String courseCode = InternalCore.getUserInput(String.class,
                    "Please enter the elective for which you would like to add a grade:");

            InternalCore.println("Please enter the student's grade for the elective:");
            String studentGradeToChange = InternalCore.getUserInput(String.class,
                    "Please enter the student's grade for the elective:");


            if (localCopy [0][0] == null) {
                InternalCore.println("The student you looked up has not registered to the course.");
            } else if (localCopy [0][1].equals(courseCode)) {
                localCopy [0][2] = studentGradeToChange;
            } else if (localCopy [0][3].equals(courseCode)) {
                localCopy [0][4] = studentGradeToChange;
            } else if (localCopy [0][5].equals(courseCode)) {
                localCopy [0][6] = studentGradeToChange;
            } else {
                InternalCore.println("The course code you entered does not exist.");
            }

            InternalCore.updateInfoFile(SEObjectType.STU_ELECT_RELATION, studentToChangeGradeFor[0], localCopy[0]);

            String addMoreGrades = InternalCore.getUserInput(String.class,
                    "Would you like to add another student grade? (Y/N)");

            if (addMoreGrades.toLowerCase().equals("y")) continue;
            break;
        }
    }

    // This method prints out a list of registered students for a particular elective
    public void showStudents(String courseCode, LectureBlock block){
        InternalCore.println("The file contains the following registered students: ");

        String[][] electiveStudent = InternalCore.readInfoFile(SEObjectType.STU_ELECT_RELATION, null);
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < electiveStudent.length; i++) {
            if (!electiveStudent[i][1].equals(courseCode)) continue;
            buffer.append(electiveStudent[i][0]).append(" ");
        }

        showStudentsEnrolled(buffer.toString());
    }

    // This method prints out a list of student grades for a particular elective
    public void showStudentGrades(String courseCode, LectureBlock block){
        InternalCore.println("The file contains the following grades : ");

        double[] grades = getGradesForElective(courseCode, block);
    }

    private double[] getGradesForElective(String courseCode, LectureBlock block) {
        String[][] electiveGrade = InternalCore.readInfoFile(SEObjectType.STU_ELECT_RELATION, null);
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < electiveGrade.length; i++) {
            if (!electiveGrade[i][block.getBlockNumber() * 2].equals(courseCode)) continue;
            buffer.append(electiveGrade[i][block.getBlockNumber() * 2 + 1]).append(" ");
        }
        String[] gradeDump = buffer.toString().split(" ");

        double[] numElectiveGrade = new double[gradeDump.length];
        for (int j = 0; j < gradeDump.length; j++) {
            numElectiveGrade[j] = Integer.parseInt(gradeDump[j]);
        }

        return numElectiveGrade;
    }

    private String[] showStudentsEnrolled(String buffer) {
        String[] dump = buffer.split(" ");
        for (int i = 0; i < dump.length; i++) {
            Student temp = new Student(new User(dump[i], UserType.STUDENT));
            InternalCore.println(temp.toString());
        }
        return dump;
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

        double[] grades = getGradesForElective(elective.getCourseCode(), elective.getElectiveBlock());
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
