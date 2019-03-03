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

    public void viewElectiveProgress(Elective elective) {
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
