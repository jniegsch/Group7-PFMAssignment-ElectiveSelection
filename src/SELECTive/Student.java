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
        } else {

            InternalCore.println("" +
                    "What do you want to edit? \n" +
                    "(1) First name \n" +
                    "(2) Last name \n" +
                    "(3) Middle Initial name\n" +
                    "(4) Username\n" +
                    "(5) Date of Birth");

            InternalCore.println(InternalCore.consoleLine('-'));
            Integer userChoice = InternalCore.getUserInput(Integer.class, "Please enter your choice (1, 2, 3, 4 or 5): ");

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
                    editUsername();
                    break;
                case 5:
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
        InternalCore.println("You are enrolled in the following elective(s):");

        String[] ids = {Long.toString(this.getUserId())};
        String[][] enrolledElectives = InternalCore.readInfoFile(SEObjectType.STU_ELECT_RELATION, ids);
        if (enrolledElectives != null) {
            //TODO: print them out
        } else {
            //TODO: looks like the student has no electives yet
        }
    }

    public void viewElectiveProgress(String courseCode) {
        //TODO
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
