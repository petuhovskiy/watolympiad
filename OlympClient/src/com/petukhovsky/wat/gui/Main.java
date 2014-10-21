package com.petukhovsky.wat.gui;

public class Main {

    public static void main(String[] args) {
        new Thread(new WatNetwork()).start();
        new WatGUI();
    }
}















