package nyx.exceptions;

public class InvalidConfigException extends Exception {
    public InvalidConfigException(String reason) {
        super("Invalid configuration file! Reason: " + reason);
    }
}
