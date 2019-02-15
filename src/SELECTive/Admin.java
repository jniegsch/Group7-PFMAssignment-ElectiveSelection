package SELECTive;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Admin extends User {

    //region Constructor
    public Admin() {
        super(UserType.ADMIN);
    }
    //endregion

    //region Account Creation
    public boolean createAdminUser(String uname, char[] pword) {
        Scanner newAdminScanner = new Scanner(System.in);
        Session.println("Please fill in the account details. You can also skip them by pressing `enter`");
        Session.print("What is your first name: ");
        String fname = (newAdminScanner.hasNextLine())? newAdminScanner.nextLine() : "";
        Session.print("What is your last name: ");
        String lname = (newAdminScanner.hasNextLine())? newAdminScanner.nextLine() : "";
        Session.print("What is/are your middle initial(s): ");
        String minit = (newAdminScanner.hasNextLine())? newAdminScanner.nextLine() : "";
        Session.print("What is your date of birth (please enter in the format yyyy-MM-dd: ");
        String dobStr = (newAdminScanner.hasNextLine())? newAdminScanner.nextLine() : "";
        Date dob = null;
        try {
            dob = new SimpleDateFormat("yyyy-MM-dd").parse(dobStr);
        } catch (ParseException pe) {
            Session.printError("SELECTive.Admin",
                    "createAdminUser",
                    "ParseException",
                    "Could not parse the passed date (" + dobStr +")");
            return false;
        }
        return createNew(fname, lname, minit, uname, dob, pword);
    }
    //endregion

    public boolean addElective() {
        Scanner inpScanner = new Scanner(System.in);

        Session.printTitle("Adding an Elective", '*', false);

        // There are 6 properties to set for an elective
        String electiveCourseID = "", electiveName = "", electiveProgramName = "";
        int electiveECTS = 0;
        String[] electiveKeywords;
        LectureTime[] electiveTimes;
        for (int prop = 0; prop < 6; prop++) {
            boolean successfulSet = false;
            switch (prop) {
                case 0:
                    Session.print("> Elective course ID: ");
                    if (inpScanner.hasNextLine()) {
                        electiveCourseID = inpScanner.nextLine();
                        successfulSet = true;
                    }
                    break;
                case 1:
                    Session.print("> Elective name: ");
                    if (inpScanner.hasNextLine()) {
                        electiveName = inpScanner.nextLine();
                        successfulSet = true;
                    }
                    break;
                case 2:
                    Session.print("> Elective program name: ");
                    if (inpScanner.hasNextLine()) {
                        electiveProgramName = inpScanner.nextLine();
                        successfulSet = true;
                    }
                    break;
                case 3:
                    Session.print("> Elective ECTS: ");
                    if (inpScanner.hasNextInt()) {
                        electiveECTS = inpScanner.nextInt();
                        successfulSet = true;
                    }
                    break;
                case 4:
                    Session.println("> Elective Keywords (separate each keyword using a ';'): ");
                    if (inpScanner.hasNextLine()) {
                        String in = inpScanner.nextLine();
                        electiveKeywords = in.split(";");
                        // loop through and strip starting or ending whitespace
                        for (int i = 0 ; i < electiveKeywords.length; i++) {
                            char[] temp = electiveKeywords[i].toCharArray();
                            if (temp[0] == ' ') electiveKeywords[i] = electiveKeywords[i].substring(1);
                            if (temp[electiveKeywords[i].length() - 1] == ' ') electiveKeywords[i] = electiveKeywords[i].substring(0, electiveKeywords[i].length() - 2);
                        }
                        successfulSet = true;
                    }
                    break;
                case 5:
                    Session.println("" +
                            "> Lesson days and times (separate the list with ';' using the format <week-day code> @ <hh:mm>): \n" +
                            "   codes: 1 = mon, 2 = tues, 3 = wed, 4 = thurs, 5 = fri, 6 = sat, 7 = sun");
                    if (inpScanner.hasNextLine()) {
                        String in = inpScanner.nextLine().replaceAll(" ", ""); // remove all whitespaces
                        String[] dateTimes = in.split(";");
                        electiveTimes = new LectureTime[dateTimes.length];
                        for (int i = 0 ; i < dateTimes.length; i++) {
                            String[] tmp = dateTimes[i].split("@");
                            electiveTimes[i] = new LectureTime(tmp[1], Integer.parseInt(tmp[0]));
                        }
                        successfulSet = true;
                    }
            }

            if (!successfulSet) prop--;
        }
        Session.println("\nCreating elective...");

        // TODO: create new elective instance using passed values

        Session.println("Elective successfully created!");
        Session.println(Session.consoleLine('*'));
        return false;
    }
}
