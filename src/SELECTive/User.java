package SELECTive;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private long userId = 0;
    private UserType type = UserType.DEFAULT;
    private String firstName = "";
    private String lastname = "";
    private String middleInitial = "";
    private String username = "";
    private Date dateOfBirth = new Date();
    //endregion

    //region Property getters
    public UserType getUserType() {
        return this.type;
    }
    //endregion

    //region Constructors
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    /**
     * An empty constructor for simplicity and ease.
     */
    public User() {
    }

    /**
     * An initial constructor to be used in the defined subclasses to create themselves based upon a user
     * @param copy {@code User} denoting the user to initialize with
     * @param target {@code UserType} the type of user to be created, if this is not the same as the one attached to
     *                               the user the type is set to DEFAULT - denoting failure
     */
    public User(User copy, UserType target) {
        this.userId = copy.userId;
        this.firstName = copy.firstName;
        this.lastname = copy.lastname;
        this.middleInitial = copy.middleInitial;
        this.username = copy.username;
        this.dateOfBirth = copy.dateOfBirth;
        this.type = copy.type;
        if (copy.type != target) this.type = UserType.DEFAULT;
    }

    /**
     * Initializer taking all the required information for an initialized User class
     * @param uname  {@code String} representing the username the user wishes to use
     * @param pword     {@code char[]} representing the password the user would like to use
     * @return {@code boolean} indicating if the creation of the user account was successful
     */
    public User createNewUser(String uname, char[] pword, UserType utype, User admin) {
        if (admin.type != UserType.ADMIN) {
            Session.printIssue("Cannot create user", "You do not have the priveleges as a non admin to create a user");
            return null;
        }
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
            Session.printError("User",
                    "createNewUser",
                    "ParseException",
                    "Could not parse the passed date (" + dobStr +")");
        }

        this.firstName = fname;
        this.lastname = lname;
        this.middleInitial = minit;
        this.username = uname;
        this.dateOfBirth = dob;
        this.type = utype;
        if (!saveNewUser(pword, this)) {
            Session.printError("SELECTive.User",
                    "createNew",
                    "FatalError",
                    "Saving the new account failed. Something seems to be seriously wrong. Exiting...");
            System.exit(Session.USER_SAVING_FAILED_INCONSISTENT_INTERNAL_STATE);
        }
        return this;
    }
    //endregion

    //region Private user management & session controls
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // password will never be stored on run time, only checked
    /**
     * a quick check {@code boolean} to see if any user is actually logged in
     */
    private boolean loggedIn = false;
    /**
     * A constant representing an invalid session id. All invalid session should be set to this!
     */
    private String sessionId = invalidID;
    /**
     * The date representing after which a session is invalid and no further actions should be taken using the "loggedin"
     * credentials. As a defualt it takes the current date.
     */
    private Date sessionExpiration = new Date();
    /**
     * A loaded memory copy of the authentication DB
     */
    private String[][] uPs;
    //endregion

    //region User Management Definitions
    /**
     * Defines the amount of times a user can attempt a login, until precautionary measures should be taken
     */
    private final static int MAX_LOGIN_ATTEMPTS = 3;
    /**
     * Represents if the authentication file has been loaded into memory and is accessible
     */
    private boolean hasOpenUP = false;
    /**
     * The location in the DB folder where the authentication file is stored
     */
    private final static String UPLoc = ".db/UPdb.txt";
    /**
     * The location in the DB folder where the Admin User Information file is stored
     */
    private final static String AdminInfoLoc = ".db/adminuinfo.txt";
    /**
     * The location in the DB folder where the Lecturer User Information file is stored
     */
    private final static String LecturerInfoLoc = ".db/lectureruinfo.txt";
    /**
     * The location in the DB folder where the Student User Information file is stored
     */
    private final static String StudentInfoLoc = ".db/studentuinfo.txt";
    //endregion

    //region Public Session Management
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    /**
     * A constant defining what all invalid User Ids should be se to.
     * <pre>
     *     VALUE = "ohno_notvalid"
     * </pre>
     */
    private static final String invalidID = "ohno_notvalid";
    /**
     * Gets the current `invalidID`. This is usually not required outside of the context of the User class, however,
     * it is an important factor for validation, so if pre-validation ever needs to be done a comparison of this value
     * to another UserId could give important insights.
     * @return {@code String} representing the invalid id
     */
    public String getInvalidID() { return invalidID; }
    //endregion

    //region Session Management
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
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
        Session.printIssue("Reached maximum login tries.", "You have reached the maximum amount of login attempts. ");
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
                    Session.printIssue("Invalid Input.", "The password you typed was incorrect!");
                }
            }
            Session.printIssue("Password change declined.", "");
            loggedIn = false;
            return false;
        }
        loggedIn = false;
        return false;
    }

    /**
     * Creates an active session by reading in the user data
     */
    private void createActiveSession() {
        String[] uinfo = readUserInfo(this);
        // only 7 things, hard code them
        this.userId = Long.parseLong(uinfo[0]);
        this.firstName = uinfo[1];
        this.lastname = uinfo[2];
        this.middleInitial = uinfo[3];
        try {
            this.dateOfBirth = new SimpleDateFormat("yyyy-MM-dd").parse(uinfo[5]);
        } catch (ParseException pe) {
            Session.printError("SELECTive.User", "createActiveSession", "ParseException", pe.getMessage());
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
    public boolean isSessionActive() {
        return (sessionId != invalidID && new Date().before(sessionExpiration));
    }

    /**
     * Invalidates a session by changing the ID to the invalidID as well as setting the expiration time to 1 hour prior
     */
    public void invalidateSession() {
        sessionId = invalidID;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -1); // set expiration to an hour prior
        sessionExpiration = cal.getTime();
    }

    /**
     * Not only invalidates a session, but also closes the system if the user does not relogin
     */
    public void forceInvalidSessionBurnout() {
        invalidateSession();
        // see if user would like to login again
        System.out.print("The session has expired. Do you wish to login again (Y/n)?");
        Scanner contScan = new Scanner(System.in);
        String ans = "";
        if (contScan.hasNextLine()) {
            ans = contScan.nextLine();
        } else {
            Session.printIssue("Invalid input.", "The input was invalid, terminating the session");
            System.exit(Session.ALL_GOOD_IN_THE_HOOD);
        }
        if (ans.equals("Y")) Session.main(null);
        System.exit(Session.ALL_GOOD_IN_THE_HOOD);
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
        String uhash = hashUsername(uname), phash = hashPassword(pword);
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
        return false;
    }
    //endregion

    //region User Management
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public boolean changePassword(char[] oldPassword, char[] newPassword) {
        return changePassword(username, oldPassword, newPassword);
    }

    /**
     * Allows a user to change their password. If the user is an admin, then no old password is required.
     * @param uname      {@code String} the username for which a password change should occur
     * @param oldPassword   {@code char[]} the old password
     * @param newPassword   {@code char[]} the new password to overwrite the old one with
     * @return {@code boolean} that indicates if the change was successful
     */
    public boolean changePassword(String uname, char[] oldPassword, char[] newPassword) {
        if (!validPassword(newPassword)) {
            Session.printIssue("New Password is Invalid", "The password you selected in invalid!");
            return false;
        }
        if (!hasOpenUP) { uPs = readDictFromFile(UPLoc); hasOpenUP = true; }
        boolean changed = false;
        String uhash = hashUsername(uname);
        for (int i = 0; i < uPs.length && !changed; i++) {
            if (uhash.equals(uPs[i][0])) {
                String oldPassHash = (type.equals(UserType.ADMIN))? "" : hashPassword(oldPassword);
                String newPassHash = hashPassword(newPassword);
                if (!type.equals(UserType.ADMIN)) {
                    if (oldPassHash.equals(uPs[i][1])) {
                        uPs[i][1] = newPassHash;
                        changed = true;
                    } else {
                        Session.printIssue("Incorrect password.", "The password you supplied is incorrect. Thus the request users password will not be changed.");
                        return false;
                    }
                } else {
                    uPs[i][1] = newPassHash;
                    changed = true;
                }
            }
        }
        if (changed) {
            if (writeDictToFile(uPs, UPLoc, true)) {
                hasOpenUP = false; // has been changed current is invalid
                return true;
            }
            // error saving, try to revert...
            if ((uPs = readDictFromFile(UPLoc)) != null) {
                Session.printIssue("An issue occured saving the new password.", "There was a severe issue trying to save your new password. For this reason the change was not saved, and your password was reverted to the old one.");
            } else {
                Session.printIssue("Fatal Error", "A Fatal error has occured where the current internal user management state is broken. Exiting, as gracefully as possible...");
                System.exit(Session.BROKEN_INTERNAL_STATE_FATAL);
            }
        }
        return false;
    }

    /**
     * A static method that checks if any users exist in the system. Can be used to evaluate if a default admin must be
     * created.
     * @return {@code boolean} indicating if no users are present
     */
    public static boolean hasNoUsers(boolean initRequired) {
        File userFile = new File(UPLoc);
        if (userFile.exists()) {
            String[][] auth = new User().readDictFromFile(UPLoc);
            if (auth.length > ((initRequired)? 0 : 1)) return false;
        }
        return true;
    }
    //endregion

    //region Access
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
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
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
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
            if (!targetFileExists(fname)) {
                Session.printError("SELECTive.User",
                        "readDictFromFile",
                        "File IO Error",
                        "Apparently the requested file (" + fname + ") does not exist and cannot be created.");
                return null;
            }
            BufferedReader reader = new BufferedReader(new FileReader(fname));
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                dump += currentLine + " \n";
            }
            reader.close();
        } catch (IOException ioe) {
            Session.printError("SELECTive.User",
                    "readDictFromFile",
                    "IOException",
                    "Could not read the file " + fname);
        }
        String [] dictPairs = dump.split(" \n");
        String [][] dictToReturn = new String[dictPairs.length][3];
        for (int i = 0; i < dictPairs.length; i++) {
            String[] sp = dictPairs[i].split(" : ");
            if (sp.length != 3) Session.printError("SELECTive.User",
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
            if (!targetFileExists(fname)) {
                Session.printError("User",
                        "writeDictToFile",
                        "File IO Error",
                        "Apparently the requested file (" + fname + ") does not exist and cannot be created.");
                return false;
            }
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
            Session.printError("User",
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
            if (!targetFileExists(loc)) {
                Session.printError("User",
                        "readUserInfo",
                        "File IO Error",
                        "Apparently the requested file (" + loc + ") does not exist and cannot be created.");
                return null;
            }
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
            Session.printError("User", "readUserInfo", "IOException", ioe.getMessage());
            return null;
        }

        if (userInfo.equals("")) {
            Session.printIssue("No user info found.",
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
            if (!targetFileExists(loc)) {
                Session.printError("User",
                        "updateUserInfo",
                        "File IO Error",
                        "Apparently the requested file (" + loc + ") does not exist and cannot be created.");
                return false;
            }
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
            Session.printError("SELECTive.User",
                    "updateUserInfo",
                    "IOException",
                    "Something went wrong updating the user file");
        }
        return false;
    }

    /**
     * Saves a newly created user to both the appropriate database file and the authentication database. If the latter
     * fails, an exception should be caught resulting in to user info being stored. This not only allows for ensuring
     * redundent users arent stored anywhere in the DB but also that without the "crucial" parts nothing is stored.
     * @param pword {@code char[]} of the password for the user
     * @param them {@code User} representing the new User + subType to store in the subType specific location
     * @return {@code boolean} representing if the saving was entirely complete or failed at any stage
     */
    private boolean saveNewUser(char[] pword, User them) {
        String userLoc = (them.type.equals(UserType.ADMIN))? AdminInfoLoc : (them.type.equals(UserType.LECTURER))? LecturerInfoLoc : StudentInfoLoc;
        try {
            // save auth creds
            writeDictToFile(new String[][]{{hashUsername(them.username), hashPassword(pword), them.type.toString()}},
                    UPLoc,
                    false);

            // get last line to increment id
            if (!targetFileExists(userLoc)) {
                Session.printError("User",
                        "saveNewUser",
                        "File IO Error",
                        "Apparently the requested file (" + userLoc + ") does not exist and cannot be created.");
                return false;
            }
            BufferedReader reader = new BufferedReader(new FileReader(userLoc));
            String currentLine = "", prevLine = "";
            int lineCount= 0;
            while ((currentLine = reader.readLine()) != null) {
                prevLine = currentLine;
                lineCount++;
            }
            reader.close();
            long id = (lineCount > 0)? Long.parseLong(prevLine.split(" ; ")[0]) : 0;
            id++;
            // save user to file
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(userLoc, true)));
            writer.println(id + " ; " + them.firstName + " ; " + them.lastname + " ; " + them.middleInitial + " ; " +
                    them.username + " ; " +
                    ((them.dateOfBirth != null)? (new SimpleDateFormat("yyyy-MM-dd").format(them.dateOfBirth)) : " "));
            writer.close();
            return true;
        } catch (IOException ioe) {
            Session.printError("User",
                    "saveNewUser",
                    "UIException",
                    "Something went wrong writing to the user file");
            return false;
        }
    }

    /**
     * Checks if a file exists at the specified path. If it doesn't the function automatically creates the file.
     * <b>
     *     IMPORTANT!
     *     This function does take action if a file is not found, the {@code boolean} return is mainly there to check
     *     if file creation failed in the case it wasn't found. By calling this function a missing file will be created
     *     thus - in a sense - fixing the issue.
     * </b>
     * @param fname {@code String} representing the file name/path relative to the current directory
     * @return {@code boolean} indicating if the file exists after completion of this function
     */
    private boolean targetFileExists(String fname) {
        File tmp = new File(fname);
        if (tmp.exists()) return true;
        try {
            tmp.createNewFile();
        } catch (IOException ioe) {
            Session.printError("SELECTive.User",
                    "targetFileExists",
                    "IOException",
                    "Could not create the file...");
            return false;
        }
        return true;
    }
    //endregion

    //region Password Validation
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // Rules
    private static final int pwordMinLength = 8;
    private static final int pwordMaxLength = 30;
    private static final int pwordMinNumCount = 2;
    private static final int pwordMinSpecialCount = 2;
    private static final int pwordMinCapitalCount = 2;
    // Rules - Regex
    private static final String regexNumCount = "\\d";
    private static final String regexSpecialCount = "[^\\da-zA-Z]";
    private static final String regexCapital = "[A-Z]";

    public static void showPasswordRules() {
        Session.printTitle("Password Rules", '-');
        Session.println("" +
                "The password must abide by these rules:\n" +
                "- Minimum length: " + pwordMinLength + "\n" +
                "- Maximum length: " + pwordMaxLength + "\n" +
                "- Minimum amount of numbers: " + pwordMinNumCount + "\n" +
                "- Minimum amount of special characters: " + pwordMinSpecialCount + "\n" +
                "- Minimum amount of capitals: " + pwordMinCapitalCount);
        Session.println(Session.consoleLine('-'));
    }

    public static boolean validPassword(char[] pword) {
        if (pword.length < pwordMinLength) return false;
        if (pword.length > pwordMaxLength) return false;
        if (!containsAtLeast(regexNumCount, pwordMinNumCount, pword.toString())) return false;
        if (!containsAtLeast(regexCapital, pwordMinCapitalCount, pword.toString())) return false;
        if (!containsAtLeast(regexSpecialCount, pwordMinSpecialCount, pword.toString())) return false;
        return true;
    }

    private static boolean containsAtLeast(String regex, int minCount, String str) {
        Pattern regexP = Pattern.compile(regex);
        Matcher regexM = regexP.matcher(str);
        int count = 0;
        while (regexM.find()) {
            count++;
        }
        if (count >= minCount) return true;
        return false;
    }
    //endregion

    //region Hashing
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    /**
     * Applies the MD5 hash alogrithm to the users username. Eventhough MD5 is no loger considered cryptographically
     * secure, it is good enough for obscuring an actual username. If this were to be bruteforced, to gain access this
     * would still require attacking SHA256 - a cryptographically secure algoritm.
     * @param uname {@code String} of the username to encrypt using MD5
     * @return {@code String} the hash of the passed username
     */
    private String hashUsername(String uname) {

        String hash = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            hash = Base64.getEncoder().encodeToString(md.digest(uname.getBytes())).toUpperCase();
        } catch (NoSuchAlgorithmException nsae) {
            Session.printError("SELECTive.User",
                    "hashUsername",
                    "NoSuchAlgorithmException",
                    "The required algorithm is not available. Crashing...");
            System.exit(Session.REQUIRED_ALGORITHM_NOT_AVAILABLE_CANNOT_CONTINUE);
        }
        return hash;
    }

    /**
     * Applies the secure SHA256 hashing algorithm to the password.
     * @param pword {@code char[]} representing the password to encrypt using SHA-256
     * @return {@code String} representing the encrypted password
     */
    private String hashPassword(char[] pword) {

        String hash = "";
        try {
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            hash = Base64.getEncoder().encodeToString(sh.digest(new String(pword).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException nsae) {
            Session.printError("SELECTive.User",
                    "hashUsername",
                    "NoSuchAlgorithmException",
                    "The required algorithm is not available. Crashing...");
            System.exit(Session.REQUIRED_ALGORITHM_NOT_AVAILABLE_CANNOT_CONTINUE);
        }
        return hash;
    }
    //endregion

    //TODO: implement filtering and searching

    //region Root User
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public static final String rootUserName = "sudo";
    public static final char[] rootUserPass = "masterPAss!_2k19".toCharArray();

    public static User getRootAdmin() {
        User root = new User();
        root.firstName = "Progamming";
        root.lastname = "Managers";
        root.middleInitial = "4";
        root.username = rootUserName;
        try {
            root.dateOfBirth = new SimpleDateFormat("yyyy-MM-dd").parse("2019-01-28");
        } catch (ParseException pe) {
            root.dateOfBirth = null;
        }
        root.type = UserType.ADMIN;
        if (!root.saveNewUser(rootUserPass, root)) {
            Session.printError("User",
                    "createNew",
                    "FatalError",
                    "Saving the new account failed. Something seems to be seriously wrong. Exiting...");
            System.exit(Session.USER_SAVING_FAILED_INCONSISTENT_INTERNAL_STATE);
        }
        return root;
    }
    //endregion
}
