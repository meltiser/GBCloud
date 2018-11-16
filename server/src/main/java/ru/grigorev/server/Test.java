package ru.grigorev.server;

import ru.grigorev.common.AuthMessage;
import ru.grigorev.common.Message;
import ru.grigorev.common.MessageType;
import ru.grigorev.server.db.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Dmitriy Grigorev
 */
public class Test {
    static List<User> users = new ArrayList<>();

    public static void main(String[] args) {
        users.add(new User(1,"bob","bob"));
        users.add(new User(2,"tom","bob"));
        users.add(new User(3,"nick","bob"));
        AuthMessage message = new AuthMessage(MessageType.SIGN_IN_REQUEST,"bob","bob");
        //Optional<String> login = users.stream().map((u) -> u.getLogin()).filter((l) -> l.equals(message.getLogin())).findAny();
        boolean login = users.stream().map(User::getLogin).filter((l) -> l.equals(message.getLogin())).anyMatch(t -> true);
        System.out.println(login);
    }
}
