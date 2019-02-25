package SELECTive;

import java.io.File;
import java.nio.file.Path;

final public class Calendar {
    //region Static Declarations
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private static final String version = "VERSION:1.0";
    private static final String prodid = "PRODID:-//SELECTive//NONSGML v1.0/EN";
    private static final String beginCal = "BEGIN:VCALENDAR";
    private static final String endCal = "END:VCALENDAR";
    private static final String beginEvent = "BEGIN:VEVENT";
    private static final String endEvent = "END:VEVENT";
    private static final String organizer = "ORGANIZER;CN=RSM SELECTive Team:MAILTO:selecting.electing@rsm.nl";
    //private static final String lineEnd = "\x0d\x0a"; // \r\n
    private static final String internalOutput = "output/";
    //endregion

    //region Creation
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public static boolean createCalendarForElectives(Elective[] electives) {
        // Approximate downloads folder location
        String homeFolder = System.getProperty("user.home");
        // Check if the folder exists
        File targetFolder = new File(homeFolder + "/downloads/");
        if (!targetFolder.exists()) {
            InternalCore.printIssue("Downloads Folder doesn't exist", "It looks like the downloads folder isn't were we thought it was. We will try and play the file in your home folder here: " + homeFolder);
            targetFolder = new File(homeFolder + "/");
            if (!targetFolder.exists()) {
                InternalCore.printIssue("Home Folder doesn't exist", "It looks like the home folder isn't were we thought it was. We will put it in the internal output.");
                targetFolder = new File(internalOutput);
            }
        }

        return false;
    }
    //endregion

}
