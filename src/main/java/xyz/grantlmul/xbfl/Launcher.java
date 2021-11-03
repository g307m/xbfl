package xyz.grantlmul.xbfl;

/**
 * Workaround main class for some weirdness with Shade and JavaFX
 * @see App - The 'real' main class
 */
public class Launcher {
    public static void main(String[] args) throws Exception {
        App.main(args);
    }
}
