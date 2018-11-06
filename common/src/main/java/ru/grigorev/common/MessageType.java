package ru.grigorev.common;

/**
 * @author Dmitriy Grigorev
 */
public enum MessageType {
    FILE,
    FILE_REQUEST,
    REFRESH_REQUEST,
    REFRESH_RESPONSE,
    DELETE_FILE,
    FILE_PART,
    AUTH_REQUEST,
    AUTH_OK,
    AUTH_FAIL
}
