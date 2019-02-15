public class ESError {
    public static void printError(String className, String function, String type, String message) {
        System.out.println("" +
                "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  " +
                "> An " + type + " error occurred in " + className + ": " + function + "\n" +
                "    " + message + "\n" +
                "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  ");
    }
}
