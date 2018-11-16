package ru.grigorev.common;

import java.io.Serializable;

/**
 * @author Dmitriy Grigorev
 */
public class AuthMessage implements Serializable {
    private MessageType type;
    private String login;
    private String password;
    private String message;
    private boolean isAuthorized;

    public AuthMessage(MessageType type, String message) {
        this.type = type;
        this.message = message;
    }

    public AuthMessage(MessageType type, String login, String password) {
        this.type = type;
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public MessageType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
