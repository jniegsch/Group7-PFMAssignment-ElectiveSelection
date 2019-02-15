import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.Scanner;

enum UserType {
    ADMIN,
    LECTURER,
    STUDENT,
    DEFAULT
}

public class User {
    //region Private Property Definitions
    private String userId = "invX00nallowed";
    private UserType type = UserType.DEFAULT;
    private String firstName = "";
    private String lastname = "";
    private String middleInitial = "";
    private String username = "";
    private Date dateOfBirth = new Date();
    //endregion

    //region Private user management & session controls
    // password will never be stored on run time, only checked
    private boolean loggedIn = false;
    private String sessionId = invalidID;
    private Date sessionExpiration = new Date();
    private String[][] uPs;
    //endregion

    //region User Management Definitions
    private final static int MAX_LOGIN_ATTEMPTS = 3;
    private boolean hasOpenUP = false;
    private final static String UPLoc = "./UPdb.txt";
    //endregion

    //region Public Session Management
    public static String invalidID = "ohno_notvalid";
    //endregion

    //region Session Management
    public boolean login(String username, char[] password) {
        //todo: change password reading to use java.io.Console
        int failedAccessCount = 0;
        boolean authed = false;
        while (failedAccessCount < MAX_LOGIN_ATTEMPTS) {
            if (authenticateUser(username, password)) { authed = true; break; }
        }
        if (authed) return true;
        ESError.printIssue("Reached maximum login tries.", "You have reached the maximum amount of login attempts. ");
        if (type == UserType.ADMIN) {
            System.out.print("Would you like to change the users password (Y/n)? ");
            Scanner cPass = new Scanner(System.in);
            String choice = "";
            if (cPass.hasNext()) {
                choice = cPass.nextLine();
            }
            cPass.close();
            if (choice.equals("Y")) {
                String npass;
                System.out.print("What should the new password be: ");
                if (cPass.hasNext()) {
                    npass = cPass.nextLine();
                    changePassword(username, null, npass.toCharArray());
                } else {
                    ESError.printIssue("Invalid Input.", "The password you typed was incorrect!");
                }
            }
            ESError.printIssue("Password change declined.", "");
            return false;
        }
        return false;
    }

    private boolean isSessionActive() {
        return false;
    }

    private void invalidateSession() {

    }

    private void forceInvalidSessionBurnout() {

    }

    /**
     * Authenticates a user based on the username and password provided. Username is stored usind MD5 hashing and
     * the password is stored usind SHA256 hashing. All passwords are handled as {@code char[]} since garbage collection
     * for {@code Strings} is not immediate, and thus this could be a security issue.
     * @param uname {@code String}: the username of the user requesting authentication
     * @param pword {@code char[]}: the password, passed as a char array for security reasons [see above]
     * @return {@code boolean}: indicating if authentication was successful
     */
    private boolean authenticateUser(String uname, char[] pword) {
        if (!hasOpenUP) { uPs = readDictFromFile(UPLoc); hasOpenUP = true; }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String uhash = Base64.getEncoder().encodeToString(md.digest(uname.getBytes())).toUpperCase();
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            String phash = Base64.getEncoder().encodeToString(sh.digest(new String(pword).getBytes(StandardCharsets.UTF_8)));
            for (int i = 0; i < uPs.length; i++) {
                if (uhash.equals(uPs[i][0])) {
                    if (phash.equals(uPs[i][1])) return true;
                    return false;
                }
            }
        } catch (NoSuchAlgorithmException nsae) {
            ESError.printError("User",
                    "authenticateUser",
                    "NoSuchAlgorithmException",
                    "The algorithm required to hash the username or password was not available: " + nsae.getMessage());
        }
        return false;
    }
    //endregion

    //region User Management
    public boolean changePassword(String username, char[] oldPassword, char[] newPassword) {
        return false;
    }
    //endregion

    //region Access
    public String getUserId() {
        Date currentDate = new Date();
        if (currentDate.after(sessionExpiration)) return invalidID;
        return sessionId;
    }
    //endregion

    //region File Access
    private String[][] readDictFromFile(String fname) {
        String dump = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fname));
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                dump += currentLine + " \\\\\\\\"; // 4x \
            }
            reader.close();
        } catch (IOException ioe) {
            ESError.printError("User",
                    "readDictFromFile",
                    "IOException",
                    "Could not read the file " + fname);
        }
        String [] dictPairs = dump.split(" \\\\\\\\");
        String [][] dictToReturn = new String[dictPairs.length][2];
        for (int i = 0; i < dictPairs.length; i++) {
            String[] sp = dictPairs[i].split(" : ");
            if (sp.length != 2) ESError.printError("User",
                    "readDictFromFile",
                    "BadDict",
                    "The dictionary entry didis not a key/pair");
            dictToReturn[i] = sp;
        }
        return dictToReturn;
    }
    //endregion
}
