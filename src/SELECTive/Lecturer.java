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

    public static void newGradeEntry(){
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
    public static void showStudents(String courseCode, LectureBlock block){
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
    public static void showStudentGrades(String courseCode, LectureBlock block){
        InternalCore.println("The file contains the following grades : ");

        String[][] electiveGrade = InternalCore.readInfoFile(SEObjectType.STU_ELECT_RELATION, null);
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < electiveGrade.length; i++) {
            if (!electiveGrade[i][block.getBlockNumber() * 2].equals(courseCode)) continue;
            buffer.append(electiveGrade[i][2]).append(" ");
        }

        String[] gradeDump = showStudentsEnrolled(buffer.toString());

        double[] numElectiveGrade = new double[gradeDump.length];
        for (int j = 0; j < gradeDump.length; j++) {
            numElectiveGrade[j] = Integer.parseInt(gradeDump[j]);
        }
    }

    private static String[] showStudentsEnrolled(String buffer) {
        String[] dump = buffer.split(" ");
        for (int i = 0; i < dump.length; i++) {
            Student temp = new Student(new User(dump[i], UserType.STUDENT));
            InternalCore.println(temp.toString());
        }
        return dump;
    }

    // This method prints out the min, max, and the average of the grades
    public static void dataStats(double[] numElectiveGrade){

        // Call the minArray(), maxArray(), and meanArray() methods and use the local array myArray as input to compute the minimum, maximum, and mean values of all heights in the file
        double minGrade = minGrade(numElectiveGrade);
        System.out.println("The minimum value of all grades in the file is: " + minGrade);
        double maxGrade = maxGrade(numElectiveGrade);
        System.out.println("The maximum value of all grades in the file is: " + maxGrade);
        double meanGrade = meanGrade(numElectiveGrade);
        System.out.println("The average value of all grades in the file is: " + meanGrade);
        double failedGrade = failedGrade(numElectiveGrade);
        System.out.println("The number of failed grades is: " + failedGrade);
    }

    // This method gets regStudents as input and returns its minimum value
    public static double minGrade(double[] numElectiveGrade){
        double minGrade = numElectiveGrade[0];
        for (int i = 0; i < numElectiveGrade.length; i++) {
            if (numElectiveGrade[i] < minGrade) {
                minGrade = numElectiveGrade [i];
            }
        }
        return minGrade;
    }

    // This method gets regStudents as input and returns its maximum value
    public static double maxGrade(double[] numElectiveGrade){
        double maxGrade = numElectiveGrade[0];
        for (int i = 0; i < numElectiveGrade.length; i++) {
            if (numElectiveGrade[i] > maxGrade) {
                maxGrade = numElectiveGrade[i];
            }
        }
        return maxGrade;
    }

    // This method gets regStudents as input and returns its average
    public static double meanGrade(double[] numElectiveGrade){
        double sumGrade = 0;
        for (int i = 0; i < numElectiveGrade.length; i++) {
            sumGrade += numElectiveGrade[i];
        }
        return (double) sumGrade / numElectiveGrade.length;
    }

    // This method gets regStudents as input and returns the number of failed grades
    public static double failedGrade(double[] numElectiveGrade){
        double failedGrade = 0;
        for (int i = 0; i < numElectiveGrade.length; i++) {
            if(numElectiveGrade[i] < 5.5) {
                failedGrade ++;
            }
        }
        return failedGrade;
    }
}
