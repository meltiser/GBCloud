package ru.grigorev.common;

public class CommonCat {
    private String name;

    public CommonCat(String name) {
        this.name = name;
    }

    public void meow() {
        System.out.println(name + ": meow");
    }
}
