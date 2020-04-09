package com.km.service;


import java.util.ArrayList;
import java.util.List;

public class test {
    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("aaa");
        list.add("bbb");
        list.add("bbb");
        list.add("ccc");
        list.add("bbb");
        list.removeIf(s -> s.equals("bbb"));
        for(String s:list)
            System.out.println(s);
    }
}
