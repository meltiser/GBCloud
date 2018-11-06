package ru.grigorev.common;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Dmitriy Grigorev
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 7420902541490628041L;
    private MessageType type;
    private byte[] byteArr;
    private String text;
    private String fileName;

    public List<String> getListFileNames() {
        return listFileNames;
    }

    public void setListFileNames(List<String> listFileNames) {
        this.listFileNames = listFileNames;
    }

    private List<String> listFileNames;

    public Message(String text) {
        this.text = text;
    }

    public Message(MessageType type, byte[] byteArr) {
        this.type = type;
        this.byteArr = byteArr;
    }

    public Message(MessageType type) {
        this.type = type;
        this.byteArr = null;
    }

    public Message(MessageType type, Path path) throws IOException {
        this.type = type;
        fileName = path.getFileName().toString();
        byteArr = Files.readAllBytes(path);
    }

    public Message(MessageType type, String fileName) {
        this.type = type;
        this.fileName = fileName;
    }

    public MessageType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public byte[] getByteArr() {
        return byteArr;
    }

    public void setByteArr(byte[] byteArr) {
        this.byteArr = byteArr;
    }

    public String getFileName() {
        return fileName;
    }
}
