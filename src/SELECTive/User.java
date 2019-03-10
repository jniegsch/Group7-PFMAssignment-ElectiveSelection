package SELECTive;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
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
    //region Static Private Properties
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    private static User[] allStudents = null;
    private static boolean hasValidStudentUsers = false;
    //endregion
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
    public long getUserId() {
        return this.userId;
    }
    /**
     * Gets the current users {@code USerType}
     * @return {@code UserType} of the current user
     */
    public UserType getUserType() {
        return this.type;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastname() {
        return this.lastname;
    }

    public String getMiddleInitial() {
        return this.middleInitial;
    }
    public String getUsername() { return  this.username; }

    public Date getDateOfBirth() {
        return this.dateOfBirth;
    }
    //endregion

    //region Constructors
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    /**
     * An empty constructor.
     */
    public User() {
        if (!hasOpenUP) {
            uPs = readDictFromAuthFile();
            hasOpenUP = true;
        }
        if (uPs == null) hasOpenUP = false;
    }

    /**
     * The constructor for admins to use to edit either a users' details or password
     * @param uname a {@code String} which is the username of the user who will be edited
     * @param ut    the {@code UserType} defining what type of user it is
     * @param admin the {@code User} who wants to edit the other user
     */
    private User(String uname, UserType ut, User admin) {
        if (admin.getUserType() != UserType.ADMIN) {
            InternalCore.printIssue("Cannot create user", "You do not have the rights to create a user");
            return;
        }

        SEObjectType ot = objectTypeForUserType(ut);
        String[][] users = InternalCore.readInfoFile(ot, null);
        if (users == null) {
            InternalCore.printIssue("No users for the requested type",
                    "No requested " + ut.toString() + "s exist.");
            return;
        }
        int userPosition = -1;
        for (int i = 0; i < users.length; i++) {
            if (!users[i][4].equals(uname)) continue;
            userPosition = i;
            break;
        }
        if (userPosition == -1) {
            InternalCore.printIssue("No such " + ut.toString(),
                    "The requested " + ut.toString() + " does not exists. Please ensure you are requesting a valid user.");
            return;
        }

        this.userId = Long.parseLong(users[userPosition][0]);
        this.firstName = users[userPosition][1];
        this.lastname = users[userPosition][2];
        this.middleInitial = users[userPosition][3];
        this.username = users[userPosition][4];
        try {
            this.dateOfBirth = (users[userPosition][5] != null)? new SimpleDateFormat().parse(users[userPosition][5]) : null;
        } catch (ParseException pe) {
            InternalCore.printError("User",
                    "User(String id, UserType ut)",
                    "ParseException",
                    "Could not parse passed date...");
            this.dateOfBirth = null;
        }
        this.type = ut;
    }

    /**
     * Creates a new user based on their ID and the specific type of user. Returns a default User instance and prints a
     * warning in case no user matching the specified ID and type was found.
     * @param id    a {@code String} representing the id of the user to create
     * @param ut    a {@code UserType} defining the type of the user to create
     */
    private User(String id, UserType ut) {
        SEObjectType ot = objectTypeForUserType(ut);
        String[] ids = {id};
        String[][] userInfo = InternalCore.readInfoFile(ot, ids);

        if (userInfo == null) {
            InternalCore.printIssue("No such " + ut.toString(),
                    "The requested " + ut.toString() + " does not exists. Please ensure you are requesting a valid user.");
            return;
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
        // if non match type; keep default user
        if (copy.type != target) return;
        this.userId = copy.userId;
        this.firstName = copy.firstName;
        this.lastname = copy.lastname;
        this.middleInitial = copy.middleInitial;
        this.username = copy.username;
        this.dateOfBirth = copy.dateOfBirth;
        this.type = copy.type;
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
    public User(String i, String f, String l, String m, String u, String d, UserType t) {
        this.userId = Long.parseLong(i);
        this.firstName = f;
        this.lastname = l;
        this.middleInitial = m;
        this.username = u;
        this.type = t;
        try {
            this.dateOfBirth = new SimpleDateFormat("yyyy-MM-dd").parse(d);
        } catch (ParseException pe) {
            InternalCore.printError("User", "User(...)", "ParseException", pe.getMessage());
            this.dateOfBirth = null;
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

        // check if username is unique
        if (userExists(uname)) {
            InternalCore.printIssue("Username already taken.",
                    "The username you chose is already taken. Please choose another");
            return null;
        }

        InternalCore.println("Please fill in the account details. You can also skip them by pressing `enter`. The user can change these at a later stage.");
        String fnameInp = InternalCore.getUserInput(String.class, "What is their first name: ");
        String fname = (fnameInp != null)? fnameInp : "";
        String lnameInp = InternalCore.getUserInput(String.class, "What is their last name: ");
        String lname = (lnameInp != null)? lnameInp : "";
        String minitInp = InternalCore.getUserInput(String.class, "What is/are their middle initial(s): ");
        String minit = (minitInp != null)? minitInp : "";
        String dobInp = InternalCore.getUserInput(String.class, "What is their date of birth (please enter in the format yyyy-MM-dd: ");
        String dobStr = (dobInp != null)? dobInp : "";

        Lecturer.Title title = null;
        if (utype == UserType.LECTURER) {
            InternalCore.println("Please choose one of the specified titles for the lecturer: ");
            int optId = 1;
            for (Lecturer.Title t : Lecturer.Title.values()) {
                InternalCore.println("(" + optId + ") " + InternalCore.capitalizeString(t.toString()));
                optId++;
            }
            // Even though we can use this class to request an integer, seeing as we allow the user to skip
            // using `enter` we must use `String` in order to not throw errors!
            String titleInp = InternalCore.getUserInput(String.class, "The title choice (1, 2, etc): ");
            String titleIntStr = (titleInp != null)? titleInp : "-1";
            int titleChoice;
            try {
                titleChoice = Integer.parseInt(titleIntStr);
            } catch (NumberFormatException nfe) {
                titleChoice = 0;
            }
            if (titleChoice < 0 || titleChoice >= Lecturer.Title.values().length) {
                title = Lecturer.Title.DEFAULT;
            } else {
                title = Lecturer.Title.values()[titleChoice];
            }
        }

        User newUser = new User();
        newUser.firstName = fname;
        newUser.lastname = lname;
        newUser.middleInitial = minit;
        newUser.username = uname;
        newUser.dateOfBirth = parseDOB(dobStr);
        newUser.type = utype;

        Lecturer newLec = null;
        if (utype == UserType.LECTURER) {
            newLec = new Lecturer(newUser, title);
        }

        if (saveNewUser(pword, (newLec == null)? newUser : newLec) == null) {
            InternalCore.printError("User",
                    "createNew",
                    "FatalError",
                    "Saving the new account failed. Something seems to be seriously wrong. Exiting...");
            System.exit(InternalCore.USER_SAVING_FAILED_INCONSISTENT_INTERNAL_STATE);
        }
        return newUser;
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
     * Methods to check if user is of valid Admin type
     */
    public boolean isValidAdmin() { return (this.type == UserType.ADMIN); }
    /**
     * Methods to check if user is of valid Student type
     */
    public boolean isValidStudent() { return (this.type == UserType.STUDENT); }
    /**
     * Methods to check if user is of valid Lecturer type
     */
    public boolean isValidLecturer() { return (this.type == UserType.LECTURER); }
    /**
     * Methods to check if user is of valid User type
     */
    public boolean isValidUser() { return !(this.type == UserType.DEFAULT); }
    /**
     * A loaded memory copy of the authentication DB
     */
    private static String[][] uPs;
    //endregion

    //region User Management Definitions
    /**
     * Defines the amount of times a user can attempt a login, until precautionary measures should be taken
     */
    public final static int MAX_LOGIN_ATTEMPTS = 3;
    /**
     * Represents if the authentication file has been loaded into memory and is accessible
     */
    private static boolean hasOpenUP = false;
    //endregion

    //region User Instance Editing
    public boolean editUser(boolean lecturerInstance) {
        InternalCore.println("" +
                "What do you want to edit? \n" +
                "(1) First name \n" +
                "(2) Last name \n" +
                "(3) Middle initial name\n" +
                "(4) Date of birth" +
                ((lecturerInstance)? "\n(5) Title" : ""));

        InternalCore.println(InternalCore.consoleLine('-'));
        Integer userChoice = InternalCore.getUserInput(Integer.class, "Please enter your choice: ");
        if (userChoice == null) return false;
        switch (userChoice) {
            case 1:
                if (!editUserFName()) {
                    InternalCore.printIssue("Could not change the first name", "");
                    return false;
                }
                break;
            case 2:
                if (!editUserLName()) {
                    InternalCore.printIssue("Could not change the last name", "");
                    return false;
                }
                break;
            case 3:
                if (!editUserMiddleInitial()) {
                    InternalCore.printIssue("Could not change the middle initial(s)", "");
                    return false;
                }
                break;
            case 4:
                if (!editDateofBirth()) {
                    InternalCore.printIssue("Could not change the DOB", "");
                    return false;
                }
                break;
            case 5:
                if (!lecturerInstance) {
                    InternalCore.printIssue("Invalid option.", "");
                    return false;
                }
                if (this.getUserType() == UserType.LECTURER) {
                    if (!((Lecturer)this).editTitle()) {
                        InternalCore.printIssue("Could not change the title", "");
                        return false;
                    }
                }
                break;
            default:
                InternalCore.printIssue("Invalid option.", "");
                return false;
        }

        return updateUserInfo(lecturerInstance);
    }

    public boolean editUserFName() {
        String newFirstName = InternalCore.getUserInput(String.class, "What is the new first name of this user?");
        if (newFirstName == null) return false;
        this.firstName = newFirstName;
        return true;
    }

    public boolean editUserLName() {
        String newLastName = InternalCore.getUserInput(String.class, "What is the new Last name of this user?");
        if (newLastName == null) return false;
        this.lastname = newLastName;
        return true;
    }

    public boolean editUserMiddleInitial() {
        String newMiddleInitial = InternalCore.getUserInput(String.class, "What is the new middle initial of this user's name?");
        if (newMiddleInitial == null) return false;
        this.middleInitial = newMiddleInitial;
        return true;
    }

    public boolean editDateofBirth() {
        String newDate = InternalCore.getUserInput(String.class, "What is the new date of birth (please enter int he format yyyy-MM-dd: ");
        this.dateOfBirth = parseDOB(newDate);

        return true;
    }

    private Date parseDOB(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException pe) {
            InternalCore.printError("User",
                    "createNewUser",
                    "ParseException",
                    "Could not parse the passed date (" + date +")");
            return null;
        }
    }
    //endregion

    //region Session Management
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    /**
     * Login a user. The login function will continuously call authenticate user, until the maximum login attempts have
     * been reached. If this occurs, it is assumed that the user might have forgotten their password. If the user is
     * of type admin, they can directly change their password. Otherwise they need to request an admin to change their
     * password since they are the only ones allowed to change passwords without knowledge of the previous password.
     * If successful, an active session is defined.
     * @return {@code boolean} representing if the login was successful
     */
    public static User login() {
        int failedAccessCount = 0;
        boolean authed = false;
        String uname = "";
        char[] pword;
        UserType type = UserType.DEFAULT;
        while (failedAccessCount < MAX_LOGIN_ATTEMPTS) {
            InternalCore.printTitle("Attempt " + (failedAccessCount + 1) + " of " + MAX_LOGIN_ATTEMPTS, '-');
            // Get username
            uname = InternalCore.getUserInput(String.class, "Username: ");
            if (uname == null) {
                InternalCore.printIssue("No Username inserted", "Please enter your username!");
                continue;
            }
            // Get password
            String pass = InternalCore.getUserInput(String.class, "Password: ");
            if (pass == null) {
                InternalCore.printIssue("No Password inserted", "Please enter your password!");
                continue;
            }
            pword = pass.toCharArray();
            if ((type = authenticateUser(uname, pword)) != UserType.DEFAULT) {
                authed = true;
                break;
            }
            failedAccessCount++;
            InternalCore.printIssue("Wrong Password or Username",
                    "The password or the username you specified was wrong. Or even both...");
        }
        if (authed) {
            InternalCore.println("> Logged in successfully\n \n ");
            switch (type) {
                case ADMIN:
                    return Admin.getAdminWithUsername(uname);
                case STUDENT:
                    return Student.getStudentWithUsername(uname);
                case LECTURER:
                    return Lecturer.getLecturerWithUsername(uname);
            }
            return null;
        }
        InternalCore.printIssue("Reached maximum login tries.",
                "You have reached the maximum amount of login attempts. Ask an admin to reset your password.");
        return null;
    }

    public static Admin rootLogin(String uname, char[] pword) {
        if (authenticateUser(uname, pword) != UserType.ADMIN) return null;
        return Admin.getAdminWithUsername(rootUserName);
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
    private static UserType authenticateUser(String uname, char[] pword) {
        if (!hasOpenUP) { uPs = readDictFromAuthFile(); hasOpenUP = true; }
        String uhash = hashUsername(uname), phash = hashPassword(pword);
        if (uPs.length == 0) {
            // have to create root >> force
            InternalCore.printIssue("Setting up internal settings", "");
            String[][] addingRoot = {{uhash, phash, UserType.ADMIN.toString()}};
            if (!writeDictToAuthFile(addingRoot, true)){
                InternalCore.printIssue("Fatal Error", "Could not create the root user, crashing in a blazing inferno of failz. <insert pikachu meme>");
                System.exit(InternalCore.INITIAL_STATE_SETUP_FAILED_FATALITY);
            }
            uPs = readDictFromAuthFile();
        }
        for (int i = 0; i < uPs.length; i++) {
            if (uhash.equals(uPs[i][0])) {
                if (phash.equals(uPs[i][1])) {
                    return UserType.valueOf(uPs[i][2]);
                }
                return UserType.DEFAULT;
            }
        }
        return UserType.DEFAULT;
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
        if (invalidPassword(newPassword)) {
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
            if (writeDictToAuthFile(uPs, true)) return true;
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
        if (!hasOpenUP) { uPs = readDictFromAuthFile(); hasOpenUP = true; }
        if (uPs.length > 1) return false;
        return true;
    }

    /**
     * Checks if a specific user exists
     * @param username {@code String} the username of the user to check for existence
     * @return {@code bool} indicating if the user was found
     */
    public static boolean userExists(String username) {
        if (!hasOpenUP) { uPs = readDictFromAuthFile(); hasOpenUP = true; }
        if (uPs == null) return false;
        String uHash = hashUsername(username);
        for (int i = 0; i < uPs.length; i++) if (uPs[i][0].equals(uHash)) return true;
        return false;
    }
    //endregion

    //region File Access & I/O Management
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
        if (dictPairs.length == 0) return null;
        if (dictPairs[0].length() < 3) return null;
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

        // add to internal memory
        for (String[] newAuth : dict) {
            if (newAuth.length < 3) continue;
            addAuth(newAuth[0], newAuth[1].toCharArray(), UserType.valueOf(newAuth[2]));
        }

        return true;
    }

    /**
     * Updates the users record after anything has changed
     * @return {@code bool} indicating if the update was successful
     */
    public boolean updateUserInfo(boolean lecturerInstance) {
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
                (this.dateOfBirth != null)? new SimpleDateFormat("yyyy-MM-dd").format(this.dateOfBirth) : "",
                (this.type == UserType.LECTURER)? ((Lecturer)this).getTitle().toString() : ""
        };
        return InternalCore.updateInfoFile(ot, Long.toString(this.userId), (lecturerInstance)? info : Arrays.copyOfRange(info, 0, 5));
    }

    /**
     * Saves a newly created user to both the appropriate database file and the authentication database. If the latter
     * fails, an exception should be caught resulting in to user info being stored. This not only allows for ensuring
     * redundent users arent stored anywhere in the DB but also that without the "crucial" parts nothing is stored.
     * @param pword {@code char[]} of the password for the user
     * @param them {@code User} representing the new User + subType to store in the subType specific location
     * @return {@code boolean} representing if the saving was entirely complete or failed at any stage
     */
    private User saveNewUser(char[] pword, User them) {
        SEObjectType type = (them.type.equals(UserType.ADMIN))? SEObjectType.ADMIN_USER : (them.type.equals(UserType.LECTURER))? SEObjectType.LECTURER_USER : SEObjectType.STUDENT_USER;
        String[] userInfo = {
                them.firstName,
                them.lastname,
                them.middleInitial,
                them.username,
                (them.dateOfBirth != null)? new SimpleDateFormat("yyyy-MM-dd").format(them.dateOfBirth) : "",
                (them.type == UserType.LECTURER)? ((Lecturer)them).getTitle().toString() : ""
        };
        them.userId = InternalCore.addEntryToInfoFile(type, (them.type == UserType.LECTURER)? userInfo : Arrays.copyOfRange(userInfo, 0, 5));
        if (them.userId == -1) {
            InternalCore.printIssue("Could not create user", "The new user could not be saved");
            return null;
        }
        String[][] auth = {{hashUsername(them.username), hashPassword(pword), them.type.toString()}};
        if (!writeDictToAuthFile(auth, false)) {
            InternalCore.printIssue("Could not create user", "The new user credentials could not be saved");
            return null;
        }

        if (!hasOpenUP) {
            uPs = readDictFromAuthFile();
            hasOpenUP = true;
        }
        if (them.type == UserType.ADMIN) Admin.addAdmin(new Admin(them));
        if (them.type == UserType.STUDENT) Student.addStudent(new Student(them));
        if (them.type == UserType.LECTURER) Lecturer.addLecturer(new Lecturer(them));
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

    public static void addAuth(String unmae, char[] pword, UserType typ) {
        if (hasAlreadyLoaded(unmae)) return;
        int currLength = 0;
        if (uPs != null) {
            currLength = uPs.length;
            uPs = Arrays.copyOf(uPs, currLength + 1);
        } else {
            uPs = new String[1][];
        }
        uPs[currLength] = new String[]{hashUsername(unmae), hashPassword(pword), typ.toString()};
    }

    private static boolean hasAlreadyLoaded(String username) {
        if (!hasOpenUP) return false;
        if (uPs == null) return false;
        for (String[] auth : uPs) {
            if (auth[0].equals(username)) return true;
        }
        return false;
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
    private static final int pwordMinNumCount = 1;
    /**
     * The minimum amount of capital letters a valid password must have
     */
    private static final int pwordMinCapitalCount = 1;

    // Rules - Regex
    /**
     * Regex definition to match to single digits
     */
    private static final String regexNumCount = "\\d";
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
                "- Minimum amount of capitals: " + pwordMinCapitalCount);
    }

    /**
     * Checks if a password is valid. Is always called internally when a new user is created or password changed,
     * but also can be called externally to not have to call either previous functions without knowing if the given
     * password is valid.
     * @param pword {@code char[]} the password to validate
     * @return {@code bool} indicating if the password is valid or not
     */
    public static boolean invalidPassword(char[] pword) {
        if (pword.length < pwordMinLength) return true;
        if (pword.length > pwordMaxLength) return true;
        if (containsLessThan(regexNumCount, pwordMinNumCount, String.copyValueOf(pword))) return true;
        if (containsLessThan(regexCapital, pwordMinCapitalCount, String.copyValueOf(pword))) return true;
        return false;
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
    private static boolean containsLessThan(String regex, int minCount, String str) {
        Pattern regexP = Pattern.compile(regex);
        Matcher regexM = regexP.matcher(str);
        int count = 0;
        while (regexM.find()) {
            count++;
            if (count == minCount) return false;
        }
        return true;
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

    //region Misc Overrides
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    public String toString() {
        StringBuilder strRepresentation = new StringBuilder();
        strRepresentation.append("[id: ").append(this.userId).append("] ");
        strRepresentation.append(InternalCore.capitalizeString(this.firstName)).append(" ");
        strRepresentation.append(InternalCore.capitalizeString((this.middleInitial.equals(" ") ? "" : this.middleInitial))).append(" ");
        strRepresentation.append(InternalCore.capitalizeString(this.lastname)).append(" ");
        return strRepresentation.toString();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof User)) return super.equals(obj);
        User object = (User) obj;
        return this.username.equals(object.username) && this.userId == object.userId && this.type == object.type;
    }
    //endregion

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
    private static void createRootAdmin() {
        User root = new User();
        root.firstName = "Programming";
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
    }
    //endregion
}
