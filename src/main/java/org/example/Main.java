package org.example;


public class Main {
    public static void main(String[] args) {
        System.out.println("Hello and welcome!");
        String a = "Tho";
        a = a.concat("Test");
        for (int i = 1; i <= 5; i++) {
            System.out.println("i = " + i + a);
        }
    }
}