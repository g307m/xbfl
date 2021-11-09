package xyz.grantlmul.xmcl.backend;

/**
 * The core backend class.
 */
public class Launcher {
    public Launcher() { }

    ProfileManager profileManager = new ProfileManager();
    AuthenticationManager authenticationManager = new AuthenticationManager();

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }
}
