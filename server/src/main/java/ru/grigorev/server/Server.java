package ru.grigorev.server;

import ru.grigorev.common.CommonCat;

/**
 * @author Dmitriy Grigorev
 */
public class Server {
    public static void main(String[] args) {
        CommonCat cat = new CommonCat("KOT");
        cat.meow();
    }
}
