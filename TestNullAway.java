package com.uber.test;

public class TestNullAway {
    public static void main(String[] args) {
        String str = null;
        // This should trigger a NullAway warning
        System.out.println(str.length());
        
        // This should also trigger a warning
        String str2 = getString();
        System.out.println(str2.length());
    }
    
    private static String getString() {
        return null;
    }
} 