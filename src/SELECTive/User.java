package SELECTive;

import java.io.*;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.LongStream;

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
    /**
     * User ID of the current user
     */
    private long userId = 0;
    /**
     * {@code UserType} of the current user
     */
    private UserType type = UserType.DEFAULT;
    /**
     * The first name of the current user
     */
    private String firstName = "";
    /**
     * The last name of the current user
     */
    private String lastname = "";
    /**
     * The middle initials of the current user, i.e P.
     */
    private String middleInitial = "";
    /**
     * The username of the current user
     */
    private String username = "";
    /**
     * The date of birth of the current user
     */
    private Date dateOfBirth = new Date();
    //endregion

    //region Property getters

    /**
     * Gets the current users {@code USerType}
     * @return {@code UserType} of the current user
     */
    public UserType getUserType() {
        return this.type;
    }
    //endregion

    //region Constructors
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    /**
     * An empty constructor.
     */
    public User() {
        // Do nothing
    }

    /**
     * Creates a new user based on their ID and the specific type of user. Returns a default User instance and prints a
     * warning in case no user matching the specified ID and type was found.
     * @param id    a {@code String} representing the id of the user to create
     * @param ut    a {@code UserType} defining the type of the user to create
     */
    public User(String id, UserType ut) {
        SEObjectType ot = objectTypeForUserType(ut);
        String[] ids = {id};
        String[][] userInfo = InternalCore.readInfoFile(ot, ids);

        if (userInfo == null) {
            InternalCore.printIssue("No such " + ut.toString(),
                    "The requested " + ut.toString() + " does not exists. Please ensure you are requesting a valid user.");
            new User();
        } else {
            String[] reqUser = userInfo[0];
            this.userId = Long.parseLong(reqUser[0]);
            this.firstName = reqUser[1];
            this.lastname = reqUser[2];
            this.middleInitial = reqUser[3];
            this.username = reqUser[4];
            try {
                this.dateOfBirth = (reqUser[5] != null)? new SimpleDateFormat().parse(reqUser[5]) : null;
            } catch (ParseException pe) {
                InternalCore.printError("User",
                        "User(String id, UserType ut)",
                        "ParseException",
                        "Could not parse passed date...");
                this.dateOfBirth = null;
            }
            this.type = ut;
        }
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
     * Initial constructor that fully initializes an new User class instance. Should not be used to create a new user
     * but only after information has been retrieved about said user from the database.
     * @param i {@code String} the users id
     * @param f {@code String} the users first name
     * @param l {@code String} the users last name
     * @param m {@code String} the users middle initials
     * @param u {@code String} the users username
     * @param d {@code String} the users date of birth (must be 'yyyy-MM-dd' format)
     * @param t {@code String} the users type
     */
    public User(String i, String f, String l, String m, String u, String d, String t) {
        this.userId = Long.parseLong(i);
        this.firstName = f;
        this.lastname = l;
        this.middleInitial = m;
        this.username = u;
        try {
            this.dateOfBirth = new SimpleDateFormat("yyyy-MM-dd").parse(d);
            this.type = UserType.valueOf(t);
        } catch (ParseException pe) {
            InternalCore.printError("User", "User(...)", "ParseException", pe.getMessage());
            this.dateOfBirth = null;
        } catch (IllegalArgumentException iae) {
            InternalCore.printError("User", "User(...", "IllegalArgumentException", iae.getMessage());
            this.type = UserType.DEFAULT;
        }
    }

    /**
     * Initializer taking all the required information for an initialized User class. Only an admin can call this function
     * @param uname     {@code String} representing the username the user wishes to use
     * @param pword     {@code char[]} representing the password the user would like to use
     * @param utype     {@code UserType} representing the type of user to create
     * @return {@code boolean} indicating if the creation of the user account was successful
     */
    public User createNewUser(String uname, char[] pword, UserType utype) {
        if (this.type != UserType.ADMIN) {
            InternalCore.printIssue("Cannot create user", "You do not have the rights to create a user");
            return null;
        }
        Scanner newAdminScanner = new Scanner(System.in);
        InternalCore.println("Please fill in the account details. You can also skip them by pressing `enter`. The user can change these at a later stage");
        InternalCore.print("What is your first name: ");
        String fname = (newAdminScanner.hasNextLine())? newAdminScanner.nextLine() : "";
        InternalCore.print("What is your last name: ");
        String lname = (newAdminScanner.hasNextLine())? newAdminScanner.nextLine() : "";
        InternalCore.print("What is/are your middle initial(s): ");
        String minit = (newAdminScanner.hasNextLine())? newAdminScanner.nextLine() : "";
        InternalCore.print("What is your date of birth (please enter in the format yyyy-MM-dd: ");
        String dobStr = (newAdminScanner.hasNextLine())? newAdminScanner.nextLine() : "";
        Date dob = null;
        try {
            dob = new SimpleDateFormat("yyyy-MM-dd").parse(dobStr);
        } catch (ParseException pe) {
            InternalCore.printError("User",
                    "createNewUser",
                    "ParseException",
                    "Could not parse the passed date (" + dobStr +")");
        }
        User newUser = new User();
        newUser.firstName = fname;
        newUser.lastname = lname;
        newUser.middleInitial = minit;
        newUser.username = uname;
        newUser.dateOfBirth = dob;
        newUser.type = utype;
        if (saveNewUser(pword, newUser) == null) {
            InternalCore.printError("User",
                    "createNew",
                    "FatalError",
                    "Saving the new account failed. Something seems to be seriously wrong. Exiting...");
            System.exit(InternalCore.USER_SAVING_FAILED_INCONSISTENT_INTERNAL_STATE);
        }
        return newUser;
    }
    //endregion

    //region User Stringify
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public String toString() {
        StringBuilder strRepresentation = new StringBuilder();
        strRepresentation.append(capitalizeString(this.type.toString()));
        strRepresentation.append(" ").append(this.userId).append(": ");
        strRepresentation.append(capitalizeString(this.firstName)).append(" ");
        strRepresentation.append(capitalizeString(this.middleInitial)).append(" ");
        strRepresentation.append(capitalizeString(this.lastname)).append(" ");
        return strRepresentation.toString();
    }

    private static String capitalizeString(String str) {
        StringBuilder capitalizedString = new StringBuilder();
        char[] strArray = str.toLowerCase().toCharArray();
        capitalizedString.append(Character.toString(strArray[0]).toUpperCase());
        capitalizedString.append(Arrays.copyOfRange(strArray, 1, strArray.length).toString());
        return capitalizedString.toString();
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
     * Login a user. The login function will continuously call authenticate user, until the maximum login attempts have
     * been reached. If this occurs, it is assumed that the user might have forgotten their password. If the user is
     * of type admin, they can directly change their password. Otherwise they need to request an admin to change their
     * password since they are the only ones allowed to change passwords without knowledge of the previous password.
     * If successful, an active session is defined.
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
            loggedIn = true;
            return true;
        }
        InternalCore.printIssue("Reached maximum login tries.", "You have reached the maximum amount of login attempts. ");
        if (type == UserType.ADMIN) {
            InternalCore.print("Since you are an Admin, would you like to change the users password (Y/n)? ");
            Scanner cPass = new Scanner(System.in);
            String choice = "";
            if (cPass.hasNextLine()) {
                choice = cPass.nextLine();
            }
            cPass.close();
            if (choice.equals("Y")) {
                String npass;
                InternalCore.print("What should the new password be: ");
                if (cPass.hasNextLine()) {
                    npass = cPass.nextLine();
                    changePassword(username, null, npass.toCharArray());
                } else {
                    InternalCore.printIssue("Invalid Input.", "The password you typed was incorrect!");
                }
            }
            InternalCore.printIssue("Password change declined.", "");
            loggedIn = false;
            return false;
        }
        loggedIn = false;
        return false;
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
        if (!hasOpenUP) { uPs = readDictFromAuthFile(); hasOpenUP = true; }
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
    /**
     * Allows the current user to change their password
     * @param oldPassword {@code char[]} the old password to change
     * @param newPassword {@code char[]} the new password to overwrite the old one with
     * @return {@code bool} indicating if the change was successful
     */
    public boolean changePassword(char[] oldPassword, char[] newPassword) {
        return changePassword(username, oldPassword, newPassword);
    }

    /**
     * Allows a user to change their password. If the user is an admin, then no old password is required. If an admin
     * is not specifically changing another users password and the password change should only occur for the current
     * user - use {@code changePassword(char[] oldPassword, char[] newPassword)}
     * @param uname      {@code String} the username for which a password change should occur
     * @param oldPassword   {@code char[]} the old password
     * @param newPassword   {@code char[]} the new password to overwrite the old one with
     * @return {@code bool} that indicates if the change was successful
     */
    public boolean changePassword(String uname, char[] oldPassword, char[] newPassword) {
        if (!validPassword(newPassword)) {
            InternalCore.printIssue("New Password is Invalid", "The password you selected in invalid!");
            return false;
        }
        if (!hasOpenUP) { uPs = readDictFromAuthFile(); hasOpenUP = true; }
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
                        InternalCore.printIssue("Incorrect password.", "The password you supplied is incorrect. Thus the request users password will not be changed.");
                        return false;
                    }
                } else {
                    uPs[i][1] = newPassHash;
                    changed = true;
                }
            }
        }
        if (changed) {
            if (writeDictToAuthFile(uPs, true)) {
                hasOpenUP = false; // has been changed current is invalid
                return true;
            }
            // error saving, try to revert...
            if ((uPs = readDictFromAuthFile()) != null) {
                InternalCore.printIssue("An issue occured saving the new password.", "There was a severe issue trying to save your new password. For this reason the change was not saved, and your password was reverted to the old one.");
            } else {
                InternalCore.printIssue("Fatal Error", "A Fatal error has occured where the current internal user management state is broken. Exiting, as gracefully as possible...");
                System.exit(InternalCore.BROKEN_INTERNAL_STATE_FATAL);
            }
        }
        return false;
    }

    /**
     * A static method that checks if any users exist in the system. Can be used to evaluate if a default admin must be
     * created. Also checks if a root exists, if not create it.
     * @return {@code bool} indicating if no users are present
     */
    public static boolean hasNoUsers() {
        // check if root needs to be created
        if (!userExists(rootUserName)) createRootAdmin();
        File userFile = new File(InternalCore.UPLoc);
        if (userFile.exists()) {
           String[][] auth = new User().readDictFromAuthFile();
           if (auth.length > 1) return false;
           return true;
        }
        return true;
    }

    /**
     * Checks if a specific user exists
     * @param username {@code String} the username of the user to check for existence
     * @return {@code bool} indicating if the user was found
     */
    public static boolean userExists(String username) {
        File userFile = new File(InternalCore.UPLoc);
        if (!userFile.exists()) return false;
        String[][] auth = new User().readDictFromAuthFile();
        String uHash = hashUsername(username);
        for (int i = 0; i < auth.length; i++) if (auth[i][0].equals(uHash)) return true;
        return false;
    }

    /**
     * Gets all the the users of a specific type defined by the ids. If null is specified, all are returned
     * @param userIDs       {@code long[]} the user ids for which to get the users (null for all)
     * @param typeOfUser    {@code UserType} the type of users to get
     * @return {@code User[]} the created user objects
     */
    public static User[] getUsers(long[] userIDs, UserType typeOfUser) {
        String[] ids = new String[userIDs.length];
        for (int i = 0; i < userIDs.length; i++) ids[i] = Long.toString(userIDs[i]);

        String[][] userInfo = getUserInfo(typeOfUser, ids);
        User[] users = new User[userInfo.length];
        for (int i = 0; i < users.length; i++) {
            User n = new User(
                    (userInfo[i][0] != null)? userInfo[i][0] : "",
                    (userInfo[i][1] != null)? userInfo[i][1] : "",
                    (userInfo[i][2] != null)? userInfo[i][2] : "",
                    (userInfo[i][3] != null)? userInfo[i][3] : "",
                    (userInfo[i][4] != null)? userInfo[i][4] : "",
                    (userInfo[i][5] != null)? userInfo[i][5] : "",
                    (userInfo[i][6] != null)? userInfo[i][6] : ""
            );
            users[i] = n;
        }
        if (users.length > 0) return users;
        return null;
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
     * @return {@code String[][]} representing the occurances split into their respective sections denoted by the format
     */
    private static String[][] readDictFromAuthFile() {
        String fname = InternalCore.fileLocationForObjectType(SEObjectType.USER_AUTH);
        String dump = "";
        try {
            if (!InternalCore.fileExists(SEObjectType.USER_AUTH)) {
                InternalCore.printError("User",
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
            InternalCore.printError("User",
                    "readDictFromFile",
                    "IOException",
                    "Could not read the file " + fname);
        }
        String [] dictPairs = dump.split(" \n");
        String [][] dictToReturn = new String[dictPairs.length][3];
        for (int i = 0; i < dictPairs.length; i++) {
            String[] sp = dictPairs[i].split(" : ");
            if (sp.length != 3) InternalCore.printError("User",
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
     * @see #readDictFromAuthFile()
     * @param dict      {@code String[][]} which is the system dict to write
     * @param overwrite {@code boolean} that indicates if the file should be overwritten or appended to
     * @return {@code bool} representing if the write was successful
     */
    private static boolean writeDictToAuthFile(String[][] dict, boolean overwrite) {
        String fname = InternalCore.fileLocationForObjectType(SEObjectType.USER_AUTH);
        try {
            if (!InternalCore.fileExists(SEObjectType.USER_AUTH)) {
                InternalCore.printError("User",
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
            InternalCore.printError("User",
                    "writeDictTofile",
                    "IOException",
                    "Could not write to the file " + fname);
            return false;
        }
        return true;
    }

    /**
     * Reads the data connected to certain user(s)
     * @param type  {@code UserType} the UserType from which information should be read
     * @param ids   {@code long[]} representing the user ids who's information should be returned
     * @return {@code String[]} representing the user(s) information
     */
    public static String[][] getUserInfo(UserType type, String[] ids) {
        SEObjectType ot = objectTypeForUserType(type);
        return InternalCore.readInfoFile(ot, ids);
    }

    /**
     * Updates the users record after anything has changed
     * @return {@code bool} indicating if the update was successful
     */
    public boolean updateUserInfo() {
        SEObjectType ot = objectTypeForUserType(this.type);
        if (ot == null) {
            InternalCore.printError("User",
                    "updateUserInfo()",
                    "User with default type",
                    "The user has the default type, please pass a valid user");
            return false;
        }
        String[] info = {
                this.firstName,
                this.lastname,
                this.middleInitial,
                this.username,
                (this.dateOfBirth != null)? new SimpleDateFormat("yyyy-MM-dd").format(this.dateOfBirth) : ""
        };
        return InternalCore.updateInfoFile(ot, Long.toString(this.userId), info);
    }

    /**
     * Saves a newly created user to both the appropriate database file and the authentication database. If the latter
     * fails, an exception should be caught resulting in to user info being stored. This not only allows for ensuring
     * redundent users arent stored anywhere in the DB but also that without the "crucial" parts nothing is stored.
     * @param pword {@code char[]} of the password for the user
     * @param them {@code User} representing the new User + subType to store in the subType specific location
     * @return {@code boolean} representing if the saving was entirely complete or failed at any stage
     */
    private static User saveNewUser(char[] pword, User them) {
        SEObjectType type = (them.type.equals(UserType.ADMIN))? SEObjectType.ADMIN_USER : (them.type.equals(UserType.LECTURER))? SEObjectType.LECTURER_USER : SEObjectType.STUDENT_USER;
        String[] userInfo = {
                them.firstName,
                them.lastname,
                them.middleInitial,
                them.username,
                (them.dateOfBirth != null)? new SimpleDateFormat("yyyy-MM-dd").format(them.dateOfBirth) : ""
        };
        them.userId = InternalCore.addEntryToInfoFile(type, userInfo);
        return them;
    }

    private static SEObjectType objectTypeForUserType(UserType ut) {
        switch (ut) {
            case ADMIN:
                return SEObjectType.ADMIN_USER;
            case STUDENT:
                return SEObjectType.STUDENT_USER;
            case LECTURER:
                return SEObjectType.LECTURER_USER;
            case DEFAULT:
                return null;
        }
        return null;
    }
    //endregion

    //region Password Validation
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // Rules
    /**
     * The minimum length a valid password must be
     */
    private static final int pwordMinLength = 8;
    /**
     * The maximum length a valid password must be
     */
    private static final int pwordMaxLength = 30;
    /**
     * The minimum amount of numbers (digits) a valid password must have
     */
    private static final int pwordMinNumCount = 2;
    /**
     * The minimum amount of special characters a valid password must have
     */
    private static final int pwordMinSpecialCount = 2;
    /**
     * The minimum amount of capital letters a valid password must have
     */
    private static final int pwordMinCapitalCount = 2;

    // Rules - Regex
    /**
     * Regex definition to match to single digits
     */
    private static final String regexNumCount = "\\d";
    /**
     * Regex definition to match to single special characters (anything that is not a digit, lower case character,
     * or a upper case character)
     */
    private static final String regexSpecialCount = "[^\\da-zA-Z]";
    /**
     * Regex definition to match to single upper case characters
     */
    private static final String regexCapital = "[A-Z]";

    /**
     * Prints out the rules for the user to see - should be called before any account creation or password change
     */
    public static void showPasswordRules() {
        InternalCore.printTitle("Password Rules", '-');
        InternalCore.println("" +
                "The password must abide by these rules:\n" +
                "- Minimum length: " + pwordMinLength + "\n" +
                "- Maximum length: " + pwordMaxLength + "\n" +
                "- Minimum amount of numbers: " + pwordMinNumCount + "\n" +
                "- Minimum amount of special characters: " + pwordMinSpecialCount + "\n" +
                "- Minimum amount of capitals: " + pwordMinCapitalCount);
        InternalCore.println(InternalCore.consoleLine('-'));
    }

    /**
     * Checks if a password is valid. Is always called internally when a new user is created or password changed,
     * but also can be called externally to not have to call either previous functions without knowing if the given
     * password is valid.
     * @param pword {@code char[]} the password to validate
     * @return {@code bool} indicating if the password is valid or not
     */
    public static boolean validPassword(char[] pword) {
        if (pword.length < pwordMinLength) return false;
        if (pword.length > pwordMaxLength) return false;
        if (!containsAtLeast(regexNumCount, pwordMinNumCount, pword.toString())) return false;
        if (!containsAtLeast(regexCapital, pwordMinCapitalCount, pword.toString())) return false;
        if (!containsAtLeast(regexSpecialCount, pwordMinSpecialCount, pword.toString())) return false;
        return true;
    }

    /**
     * This function checks, based on a regex, how many times a match is found. It then checks if this amount is equal
     * to or greater than the required minimum.
     * Mainly only for use in the {@code validatePassword(char[] pword)} function and is thus private to the
     * {@code User} class.
     * @param regex     {@code String} representing the pattern (regex) to use
     * @param minCount  {@code int} representing the minimum amount of matches required
     * @param str       {@code String} representing the string to search
     * @return {@code bool} indicating if the `minCount` of matches occured
     */
    private static boolean containsAtLeast(String regex, int minCount, String str) {
        Pattern regexP = Pattern.compile(regex);
        Matcher regexM = regexP.matcher(str);
        int count = 0;
        while (regexM.find()) {
            count++;
            if (count == minCount) return true;
        }
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
    private static String hashUsername(String uname) {

        String hash = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            hash = Base64.getEncoder().encodeToString(md.digest(uname.getBytes())).toUpperCase();
        } catch (NoSuchAlgorithmException nsae) {
            InternalCore.printError("SELECTive.User",
                    "hashUsername",
                    "NoSuchAlgorithmException",
                    "The required algorithm is not available. Crashing...");
            System.exit(InternalCore.REQUIRED_ALGORITHM_NOT_AVAILABLE_CANNOT_CONTINUE);
        }
        return hash;
    }

    /**
     * Applies the secure SHA256 hashing algorithm to the password.
     * @param pword {@code char[]} representing the password to encrypt using SHA-256
     * @return {@code String} representing the encrypted password
     */
    private static String hashPassword(char[] pword) {

        String hash = "";
        try {
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            hash = Base64.getEncoder().encodeToString(sh.digest(new String(pword).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException nsae) {
            InternalCore.printError("SELECTive.User",
                    "hashUsername",
                    "NoSuchAlgorithmException",
                    "The required algorithm is not available. Crashing...");
            System.exit(InternalCore.REQUIRED_ALGORITHM_NOT_AVAILABLE_CANNOT_CONTINUE);
        }
        return hash;
    }
    //endregion

    //TODO: implement filtering and searching

    //region Root User
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    /**
     * The hardcoded root username
     */
    public static final String rootUserName = "sudo";
    /**
     * The hardcoded root password
     */
    public static final char[] rootUserPass = "masterPAss!_2k19".toCharArray();

    /**
     * Creates a new root user
     * @return {@code bool} indicating if the creation was a success
     */
    private static User createRootAdmin() {
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
        if (root.saveNewUser(rootUserPass, root) == null) {
            InternalCore.printError("User",
                    "createNew",
                    "FatalError",
                    "Saving the new account failed. Something seems to be seriously wrong. Exiting...");
            System.exit(InternalCore.USER_SAVING_FAILED_INCONSISTENT_INTERNAL_STATE);
        }
        return root;
    }
    //endregion
}
