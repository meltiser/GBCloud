package ru.grigorev.common;

import java.io.Serializable;

/**
 * @author Dmitriy Grigorev
 */
public class SendObject implements Serializable {
    private ObjectType type;
    private byte[] byteArr;

    public SendObject(ObjectType type, byte[] byteArr) {
        this.type = type;
        this.byteArr = byteArr;
    }

    public SendObject(ObjectType type) {
        this.type = type;
        this.byteArr = null;
    }

    public ObjectType getType() {
        return type;
    }

    public void setType(ObjectType type) {
        this.type = type;
    }

    public byte[] getByteArr() {
        return byteArr;
    }

    public void setByteArr(byte[] byteArr) {
        this.byteArr = byteArr;
    }
}
