package SELECTive;

public class TestMethod {

    //region Test Type Enum Declaration
    public enum TestType {
        EXAM,
        ASSIGNMENT,
        PARTICIPATION,
        GROUP_PROJECT,
        INDIVIDUAL_PROJECT,
        PRESENTATION,
        CASE_PRESENTATION,
        YtestYOLOjustGoForIt_BREAKSHITTHENFIXIT //default option, definitely - and clearly - invalid
    }
    //endregion

    //region Private Properties
    private TestType type = TestType.YtestYOLOjustGoForIt_BREAKSHITTHENFIXIT;
    private double gradeWeight = 0.0;
    //endregion
}
