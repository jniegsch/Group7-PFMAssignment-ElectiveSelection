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
        //TODO
    }

    public void viewElectiveProgress(Elective elective) {
        //TODO
    }
    //endregion

    //region Time Management
    public boolean exportCalForElectives() {
        return false; //TODO
    }

    private boolean hasRegistedForElectives() {
        return false; //TODO
    }
    //endregion
}
