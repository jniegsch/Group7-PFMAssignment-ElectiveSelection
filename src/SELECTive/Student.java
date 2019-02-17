package SELECTive;

public class Student extends User {

    //region Constructor
    public Student(User base) {
        super(base, UserType.STUDENT);
    }
    //endregion
}
