import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Calendar;
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
    private final static String UInfoLoc = "./uinfo.txt";
    //endregion

    //region Public Session Management
    private static String invalidID = "ohno_notvalid";
    public String getInvalidID() { return invalidID; }
    //endregion

    //region Session Management

    /**
     * Login a user. The login function will continously call authenticate user, until the maximum login attempts have
     * been reached. If this occurs, it is assumed that the user might have forgotten their password. If the user is
     * of type admin, they can directly change their password. Otherwise they need to request an admin to change their
     * password since they are the only ones allowed to change passwords without knowledge of the previous password.
     * If successful, and active session is defined.
     * @param username {@code String} the username of the user attempting to login
     * @param password {@code char[]} the password of the user attempting to login (char[] for security purposes)
     * @return {@code boolean} representing if the login was successful
     */
    public boolean login(String username, char[] password) {
        //TODO: change password reading to use java.io.Console
        int failedAccessCount = 0;
        boolean authed = false;
        while (failedAccessCount < MAX_LOGIN_ATTEMPTS) {
            if (authenticateUser(username, password)) { authed = true; break; }
        }
        if (authed) {
            createActiveSession();
            loggedIn = true;
            return true;
        }
        ESError.printIssue("Reached maximum login tries.", "You have reached the maximum amount of login attempts. ");
        if (type == UserType.ADMIN) {
            System.out.print("Would you like to change the users password (Y/n)? ");
            Scanner cPass = new Scanner(System.in);
            String choice = "";
            if (cPass.hasNextLine()) {
                choice = cPass.nextLine();
            }
            cPass.close();
            if (choice.equals("Y")) {
                String npass;
                System.out.print("What should the new password be: ");
                if (cPass.hasNextLine()) {
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

    private void createActiveSession() {
        String[] uinfo = readUserInfo(username);
        // only 7 things, hard code them
        userId = uinfo[0];
        type = UserType.valueOf(uinfo[1]);
        firstName = uinfo[2];
        lastname = uinfo[3];
        middleInitial = uinfo[4];
        try {
            dateOfBirth = new SimpleDateFormat("yyyy-MM-dd").parse(uinfo[6]);
        } catch (ParseException pe) {
            ESError.printError("User", "createActiveSession", "ParseException", pe.getMessage());
        }
        sessionId = userId + "_" + type.toString().toLowerCase();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 1);
        sessionExpiration = cal.getTime();
    }

    /**
     * A simple function that is used internally to check if a session is active. Activeness is tested by ensuring the
     * session ID is not the specified `invaldID` as well as the fact that the current time has not passed the
     * expiration time.
     * @return {@code boolean} indicating if the session is active - or not
     */
    private boolean isSessionActive() {
        return (sessionId != invalidID && new Date().before(sessionExpiration));
    }

    /**
     * Invalidates a session by changing the ID to the invalidID as well as setting the expiration time to 1 hour prior
     */
    private void invalidateSession() {
        sessionId = invalidID;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -1); // set expiration to an hour prior
        sessionExpiration = cal.getTime();
    }

    private void forceInvalidSessionBurnout() {
        // see if user would like to login again
        System.out.print("The session has expired. Do you wish to login again (Y/n)?");
        Scanner contScan = new Scanner(System.in);
        String ans = "";
        if (contScan.hasNextLine()) {
            ans = contScan.nextLine();
        } else {
            ESError.printIssue("Invalid input.", "The input was invalid, terminating the session");
            System.exit(0);
        }
        if (ans.equals("Y")) Session.main(null);
        System.exit(0);
    }

    /**
     * Authenticates a user based on the username and password provided. Username is stored usind MD5 hashing and
     * the password is stored usind SHA256 hashing. All passwords are handled as {@code char[]} since garbage collection
     * for {@code Strings} is not immediate, and thus this could be a security issue. If the user is authenticated,
     * the user name is stored. Password is cleared.
     * @param uname {@code String}: the username of the user requesting authentication
     * @param pword {@code char[]}: the password, passed as a char array for security reasons (see above)
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
                    if (phash.equals(uPs[i][1])) {
                        username = uname;
                        pword = null;
                        return true;
                    }
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
        if (!isSessionActive()) return invalidID;
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

    private String[] readUserInfo(String uname) {
        String userInfo = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(UInfoLoc));
            String currentLine;
            while((currentLine = reader.readLine()) != null) {
                if (currentLine.contains(uname)) {
                    userInfo = currentLine;
                    break;
                }
            }
        } catch (IOException ioe) {
            ESError.printError("User", "readUserInfo", "IOException", ioe.getMessage());
        }

        if (userInfo.equals("")) {
            ESError.printIssue("No user info found.",
                    "No user info for the user ("+ uname +") was found. Please ensure this user actually exists.");
            return null;
        }

        return userInfo.split(";");
    }

    private boolean updateUserInfo(String uname) {
        return false;
    }
    //endregion
}
