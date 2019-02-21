package SELECTive;

public class Student extends User {

    //region Constructor
    public Student(User base) {
        super(base, UserType.STUDENT);
    }

    // TODO: constructor also creates "empty" entry to Student_Elective_Relation file
    //endregion
}
