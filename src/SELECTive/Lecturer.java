package SELECTive;

public class Lecturer extends User {

    //region Constructor
    public Lecturer(User base) {
        super(base, UserType.LECTURER);
    }
    //endregion
}
