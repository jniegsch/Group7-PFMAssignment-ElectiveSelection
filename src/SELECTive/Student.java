package SELECTive;

public class Student extends User {

    //region Constructor
    public Student(User base) {
        super(base, UserType.STUDENT);
    }

    // TODO: constructor also creates "empty" entry to Student_Elective_Relation file

    // TODO: Create iCal export and check
    // TODO: Register to Elective
    //endregion

    //region Instance Editing
    public boolean editStudent (String uname) {
        if (!userExists((uname))) {
            InternalCore.println("This username does not exist. Please try again or first create this user.");
            return false;
        } else {

            InternalCore.println("" +
                    "What do you want to edit? \n" +
                    "(1) First name \n" +
                    "(2) Last name \n" +
                    "(3) Middle Initial name\n" +
                    "(4) Date of Birth");

            InternalCore.println(InternalCore.consoleLine('-'));
            Integer userChoice = InternalCore.getUserInput(Integer.class, "Please enter your choice (1, 2, 3 or 4): ");

            if (userChoice == null) return false;
            switch (userChoice) {
                case 1:
                    editUserFName();
                    break;
                case 2:
                    editUserLName();
                    break;
                case 3:
                    editUserMiddleInitial();
                    break;
                case 4:
                    editDateofBirth();
                    break;
            }

            updateUserInfo();

        }
        return true;
    }
    //endregion

    //region Enrollment
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
        if (enrolledElectives != null) {
            String userChoice = InternalCore.getUserInput(String.class, "For which elective do you wish to you view your progress?");

            for (int i = 1; i < enrolledElectives.length; i += 2) {
                if (enrolledElectives[0][i].equals(userChoice))
                    InternalCore.println("Your progress for " + userChoice + "is: " + enrolledElectives[0][i + 1]);
            }
        } else {
            InternalCore.println("You are not yet enrolled in any elective");
        }
    }
    //endregion

    //region Time Management
    public boolean exportCalForElectives() {
        return false; //TODO
    }


    private long hasRegistedForElectives() {
        return -1; //TODO
    }
    //endregion
}
