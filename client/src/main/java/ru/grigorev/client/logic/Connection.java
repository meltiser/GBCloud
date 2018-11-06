package ru.grigorev.client.logic;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import ru.grigorev.common.Info;
import ru.grigorev.common.Message;

import java.io.IOException;
import java.net.Socket;

/**
 * @author Dmitriy Grigorev
 */
public class Connection {
    private static ObjectEncoderOutputStream out;
    private static ObjectDecoderInputStream in;
    private static Socket socket;

    public static void init() {
        try {
            socket = new Socket(Info.HOST, Info.PORT);
            in = new ObjectDecoderInputStream(socket.getInputStream());
            out = new ObjectEncoderOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        try {
            socket.close();
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Message receiveMessage() {
        Message message = null;
        try {
            message = (Message) in.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return message;
    }

}
