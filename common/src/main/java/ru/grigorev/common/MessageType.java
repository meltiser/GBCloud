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
    GOODBYE,
    FILE_PART,
    SIGN_IN_REQUEST,
    SIGN_UP_REQUEST,
    AUTH_OK,
    AUTH_FAIL
}
