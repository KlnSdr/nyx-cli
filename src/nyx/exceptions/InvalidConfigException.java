package nyx.exceptions;

public class InvalidConfigException extends Exception {
    public InvalidConfigException() {
        super("Invalid configuration file!");
    }
}
