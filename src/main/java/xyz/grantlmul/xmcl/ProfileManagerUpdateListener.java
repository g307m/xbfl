package xyz.grantlmul.xmcl;

/**
 * @implNote all methods have a default so anonymous impls are like ok and also not fucking massive
 */
public interface ProfileManagerUpdateListener {
    default void profileAdded(Profile profile) {}
    default void profileRemoved(Profile profile) {}
    default void profileUpdated(Profile profile) {}
}
