package SELECTive;

import java.util.Scanner;

public class Session {
    private static final int consoleCharWidth = 120;
    private static final String systemName = "SELECTive ";

    public static final boolean systemPrintsErrors = true;

    public static void main(String[] args) {
        //TODO: The actual program...
    }

    // region General printing
    public static void printTitle(String title, char c, boolean supressName) {
        if (title.length() > consoleCharWidth - 9) return;
        String beginning = c + " " + c + " ";
        int setWidth = beginning.length() + ((supressName)? 0 : systemName.length()) + 2; // +2 for the ': ' that is appended later
        int leftToFill = 0;
        System.out.print(beginning + ((supressName)? "" : systemName) + ": ");
        if (title.length() + setWidth > consoleCharWidth - 3) {
            String nLine = "\n      " + title;
            System.out.print(nLine);
            leftToFill = consoleCharWidth - nLine.length() + 2; // -2 for the '\n'
        } else {
            String nLine = beginning + ((supressName)? "" : systemName) + ": " + title;
            System.out.print(nLine);
            leftToFill = consoleCharWidth - nLine.length();
        }
        for (int i = 0; i < leftToFill; i += 2) System.out.print(c + " ");
        System.out.print("\n");
    }

    public static String consoleLine(char c) {
        String line = "";
        for (int i = 0; i < consoleCharWidth + 2; i += 2) line += c + " ";
        return line;
    }

    public static void println(String str) {
        String[] sections = str.split("\n");
        for (int i = 0; i < sections.length; i++) {
            while (sections[i].length() > consoleCharWidth) {
                String sub = sections[i].substring(0, consoleCharWidth - 1);
                System.out.println(sub);
                sections[i] = sections[i].substring(consoleCharWidth);
            }
            System.out.println(sections[i]);
        }
    }

    public static void print(String str) {
        String[] sections = str.split("\n");
        for (int i = 0; i < sections.length; i++) {
            while (sections[i].length() > consoleCharWidth) {
                String sub = sections[i].substring(0, consoleCharWidth - 1);
                System.out.print(sub + "\n");
                sections[i] = sections[i].substring(consoleCharWidth);
            }
            if (i == sections.length - 1) {
                System.out.print(sections[i]);
            } else {
                System.out.print(sections[i] + "\n");
            }
        }
    }
    //endregion

    //region Error and Issue handling
    /**
     * Print an error
     * @param className {@code String} defining the class in which the error occurred
     * @param function  {@code String} defining in which function the error occurred for better debugging
     * @param type      {@code String} defining the type of error usually used to define the exception that was caught
     * @param message   {@code String} the additional message to print in order to further clarify things
     */
    public static void printError(String className, String function, String type, String message) {
        if (systemPrintsErrors) {
            printTitle("Error", '-', true);
            println("> An " + type + " error occurred in " + className + ": " + function + "\n" +
                    "    " + message + "\n");
            println(consoleLine('-'));
        }
    }

    /**
     * Prints an issue or warning for the user. It is mainly meant to be informative to the user telling them about an
     * issue
     * @param title     {@code String} the title of the issue to be shown
     * @param message   {@code String} the message further describing the issue along with potential actions the user
     *                                could take and or actions the system will take on behalf of the user due to
     *                                certian conditions
     */
    public static void printIssue(String title, String message) {
        System.out.println("" +
                ">> Warning: " + title);
        if (!message.equals("")) System.out.println("" +
                "    " + message);
    }
    //endregion
}
