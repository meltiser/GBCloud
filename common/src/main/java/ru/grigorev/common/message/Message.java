package ru.grigorev.common.message;

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
    private int currentPart;
    private String fileName;
    private byte[] byteArr;
    private String login;
    private int partsCount;
    private List<String> listFileNames;

    public Message(MessageType type) {
        this.type = type;
        this.byteArr = null;
    }

    public Message(MessageType type, byte[] byteArr, int partsCount, int currentPart, String fileName) {
        this.type = type;
        this.byteArr = byteArr;
        this.partsCount = partsCount;
        this.currentPart = currentPart;
        this.fileName = fileName;
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

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public List<String> getListFileNames() {
        return listFileNames;
    }

    public void setListFileNames(List<String> listFileNames) {
        this.listFileNames = listFileNames;
    }

    public int getPartsCount() {
        return partsCount;
    }

    public void setPartsCount(int partsCount) {
        this.partsCount = partsCount;
    }

    public int getCurrentPart() {
        return currentPart;
    }

    public void setCurrentPart(int currentPart) {
        this.currentPart = currentPart;
    }
}
