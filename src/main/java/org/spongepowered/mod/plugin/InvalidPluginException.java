package org.spongepowered.mod.plugin;

public class InvalidPluginException extends RuntimeException {

    public InvalidPluginException() {
        super();
    }

    public InvalidPluginException(String message) {
        super(message);
    }

    public InvalidPluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPluginException(Throwable cause) {
        super(cause);
    }

}
