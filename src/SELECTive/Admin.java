package SELECTive;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Admin extends User {

    //region Constructor
    public Admin(User base) {
        super(base, UserType.ADMIN);
    }
    //endregion


    public boolean addElective() {
        Scanner inpScanner = new Scanner(System.in);

        InternalCore.printTitle("Adding an Elective", '*');

        // There are 6 properties to set for an elective
        String electiveCourseID = "", electiveName = "", electiveProgramName = "";
        int electiveECTS = 0;
        String[] electiveKeywords;
        LectureTime[] electiveTimes;
        for (int prop = 0; prop < 6; prop++) {
            boolean successfulSet = false;
            switch (prop) {
                case 0:
                    InternalCore.print("> Elective course ID: ");
                    if (inpScanner.hasNextLine()) {
                        electiveCourseID = inpScanner.nextLine();
                        successfulSet = true;
                    }
                    break;
                case 1:
                    InternalCore.print("> Elective name: ");
                    if (inpScanner.hasNextLine()) {
                        electiveName = inpScanner.nextLine();
                        successfulSet = true;
                    }
                    break;
                case 2:
                    InternalCore.print("> Elective program name: ");
                    if (inpScanner.hasNextLine()) {
                        electiveProgramName = inpScanner.nextLine();
                        successfulSet = true;
                    }
                    break;
                case 3:
                    InternalCore.print("> Elective ECTS: ");
                    if (inpScanner.hasNextInt()) {
                        electiveECTS = inpScanner.nextInt();
                        successfulSet = true;
                    }
                    break;
                case 4:
                    InternalCore.println("> Elective Keywords (separate each keyword using a ';'): ");
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
                    InternalCore.println("" +
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
        InternalCore.println("\nCreating elective...");

        // TODO: create new elective instance using passed values

        InternalCore.println("Elective successfully created!");
        InternalCore.println(InternalCore.consoleLine('*'));
        return false;
    }
}
