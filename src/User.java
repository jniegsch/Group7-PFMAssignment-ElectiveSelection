import java.io.*;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Calendar;
import java.util.Scanner;

/**
 * An enum defining what type of user the subclass is. If something as gone wrong, or the class is just being initialized,
 * then the type must be `DEFAULT`
 */
enum UserType {
    ADMIN,
    LECTURER,
    STUDENT,
    DEFAULT
}

/**
 * The core class for any user of the system. It handles all basic functionalities shared by all user types. Some
 * functions are prepared for extra functionality of specific uber classes, but the class does ensure rights are checked.
 */
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
    private final static String AdminInfoLoc = "./adminuinfo.txt";
    private final static String LecturerInfoLoc = "./lectureruinfo.txt";
    private final static String StudentInfoLoc = "./studentuinfo.txt";
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
            password = null;
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

    /**
     * Creates an active session by reading in the user data
     */
    private void createActiveSession() {
        String[] uinfo = readUserInfo(this);
        // only 7 things, hard code them
        this.userId = uinfo[0];
        this.firstName = uinfo[1];
        this.lastname = uinfo[2];
        this.middleInitial = uinfo[3];
        try {
            this.dateOfBirth = new SimpleDateFormat("yyyy-MM-dd").parse(uinfo[5]);
        } catch (ParseException pe) {
            ESError.printError("User", "createActiveSession", "ParseException", pe.getMessage());
        }
        this.sessionId = userId + "_" + type.toString().toLowerCase();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 1);
        this.sessionExpiration = cal.getTime();
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

    /**
     * Not only invalidates a session, but also closes the system if the user does not relogin
     */
    private void forceInvalidSessionBurnout() {
        invalidateSession();
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
                        type = UserType.valueOf(uPs[i][2]);
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
    /**
     * Allows a user to change their password. If the user is an admin, then no old password is required.
     * @param username      {@code String} the username for which a password change should occur
     * @param oldPassword   {@code char[]} the old password
     * @param newPassword   {@code char[]} the new password to overwrite the old one with
     * @return {@code boolean} that indicates if the change was successful
     */
    public boolean changePassword(String username, char[] oldPassword, char[] newPassword) {
        if (!hasOpenUP) { uPs = readDictFromFile(UPLoc); hasOpenUP = true; }
        boolean changed = false;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String uhash = Base64.getEncoder().encodeToString(md.digest(username.getBytes())).toUpperCase();
            for (int i = 0; i < uPs.length && !changed; i++) {
                if (uhash.equals(uPs[i][0])) {
                    MessageDigest sh = MessageDigest.getInstance("SHA-256");
                    String oldPassHash = (type.equals(UserType.ADMIN))? "" : Base64.getEncoder().encodeToString(sh.digest(new String(oldPassword).getBytes(StandardCharsets.UTF_8)));
                    String newPassHash = Base64.getEncoder().encodeToString(sh.digest(new String(newPassword).getBytes(StandardCharsets.UTF_8)));
                    if (!type.equals(UserType.ADMIN)) {
                        if (oldPassHash.equals(uPs[i][1])) {
                            uPs[i][1] = newPassHash;
                            changed = true;
                        } else {
                            ESError.printIssue("Incorrect password.", "The password you supplied is incorrect. Thus the request users password will not be changed.");
                            return false;
                        }
                    } else {
                        uPs[i][1] = newPassHash;
                        changed = true;
                    }
                }
            }
        } catch (NoSuchAlgorithmException nsae) {
            ESError.printError("User",
                    "changePassword",
                    "NoSuchAlgorithmException",
                    "The algorithm required to hash the username or password was not available: " + nsae.getMessage());
        }
        if (changed) {
            if (writeDictToFile(uPs, UPLoc, true)) return true;
            // error saving, try to revert...
            if ((uPs = readDictFromFile(UPLoc)) != null) {
                ESError.printIssue("An issue occured saving the new password.", "There was a severe issue trying to save your new password. For this reason the change was not saved, and your password was reverted to the old one.");
            } else {
                ESError.printIssue("Fatal Error", "A Fatal error has occured where the current internal user management state is broken. Exiting, as gracefully as possible...");
                System.exit(12);
            }
        }
        return false;
    }

    /**
     * A static method that checks if any users exist in the system. Can be used to evaluate if a default admin must be
     * created.
     * @return {@code boolean} indicating if no users are present
     */
    public static boolean noUsers() {
        return (new User().readDictFromFile(UPLoc).length == 0);
    }
    //endregion

    //region Access

    /**
     * Returns the user id
     * @return {@code String} the user ID
     */
    public String getUserId() {
        if (!isSessionActive()) return invalidID;
        return sessionId;
    }
    //endregion

    //region File Access

    /**
     * Reads a form of dictionary used by the system. The format is used for the user authentication. Each line of the
     * authentication file is in the form:
     * <pre>
     *     [MD5 hash of username] : [SHA256 hash of password] : [Type of user]
     * </pre>
     * @param fname {@code String} the filename from which to read the specific dictionary
     * @return {@code String[][]} representing the occurances split into their respective sections denoted by the format
     */
    private String[][] readDictFromFile(String fname) {
        String dump = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fname));
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                dump += currentLine + " \n";
            }
            reader.close();
        } catch (IOException ioe) {
            ESError.printError("User",
                    "readDictFromFile",
                    "IOException",
                    "Could not read the file " + fname);
        }
        String [] dictPairs = dump.split(" \n");
        String [][] dictToReturn = new String[dictPairs.length][3];
        for (int i = 0; i < dictPairs.length; i++) {
            String[] sp = dictPairs[i].split(" : ");
            if (sp.length != 3) ESError.printError("User",
                    "readDictFromFile",
                    "BadDict",
                    "The dictionary entry are not a key/pair & type form");
            dictToReturn[i] = sp;
        }
        return dictToReturn;
    }

    /**
     * Writes a system dict to the specified file. Usually used to update the authentication file. For the format check
     * the reading function defined in `see`
     * @see #writeDictToFile(String[][], String, boolean)
     * @param dict      {@code String[][]} which is the system dict to write
     * @param fname     {@code String} representing the file to write to
     * @param overwrite {@code boolean} that indicates if the file should be overwritten or appended to
     * @return {@code boolean} representing if the write was successful
     */
    private boolean writeDictToFile(String[][] dict, String fname, boolean overwrite) {
        try {
            PrintWriter printer = new PrintWriter(new BufferedWriter( new FileWriter(fname, !overwrite)));
            for (int i = 0; i < dict.length; i++) {
                String line = "";
                for (int j = 0; j < dict[i].length; j++) {
                    line += dict[i][j];
                    line += (j != dict[i].length - 1)? " : " : "";
                }
                printer.println(line);
            }
            printer.close();
        } catch (IOException ioe) {
            ESError.printError("User",
                    "writeDictTofile",
                    "IOException",
                    "Could not write to the file " + fname);
            return false;
        }
        return true;
    }

    /**
     * Reads the data connected to a certain user
     * @param someone {@code User} the user to get the information for
     * @return {@code String[]} representing the users information
     */
    private String[] readUserInfo(User someone) {
        String userInfo = "";
        String loc = (someone.type.equals(UserType.ADMIN))? AdminInfoLoc : (someone.type.equals(UserType.LECTURER))? LecturerInfoLoc : StudentInfoLoc;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(loc));
            String currentLine;
            while((currentLine = reader.readLine()) != null) {
                if (currentLine.split(" ; ")[4].equals(someone.username)) {
                    userInfo = currentLine;
                    break;
                }
            }
            reader.close();
        } catch (IOException ioe) {
            ESError.printError("User", "readUserInfo", "IOException", ioe.getMessage());
            return null;
        }

        if (userInfo.equals("")) {
            ESError.printIssue("No user info found.",
                    "No user info for the user ("+ someone.username +") was found. Please ensure this user actually exists.");
            return null;
        }

        return userInfo.split(" ; ");
    }

    /**
     * Updates a users record after anythin has changed
     * @param you {@code User} the user whose data should be changed
     * @return {@code boolean} indicating if the update was successful
     */
    private boolean updateUserInfo(User you) {
        String loc = (you.type.equals(UserType.ADMIN))? AdminInfoLoc : (you.type.equals(UserType.LECTURER))? LecturerInfoLoc : StudentInfoLoc;
        try {
            final File temp = File.createTempFile("tmp", "txt");
            BufferedReader reader = new BufferedReader(new FileReader(loc));
            String currentLine;
            StringBuffer newBuffer = new StringBuffer();

            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.split(" ; ")[0].equals(you.userId)) {
                    newBuffer.append(you.userId + " ; ");
                    newBuffer.append(you.firstName + " ; ");
                    newBuffer.append(you.lastname + " ; ");
                    newBuffer.append(you.middleInitial + " ; ");
                    newBuffer.append(you.username + " ; ");
                    newBuffer.append(new SimpleDateFormat("yyyy-MM-dd").format(dateOfBirth));
                    newBuffer.append("\n");
                } else {
                    newBuffer.append(currentLine);
                    newBuffer.append("\n");
                }
            }
            reader.close();

            BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
            writer.write(newBuffer.toString());
            writer.close();

            temp.renameTo(new File(loc));
        } catch (IOException ioe) {
            ESError.printError("User",
                    "updateUserInfo",
                    "IOException",
                    "Something went wrong updating the user file");
        }
        return false;
    }
    //endregion

    //TODO: implement filtering and searching
}
