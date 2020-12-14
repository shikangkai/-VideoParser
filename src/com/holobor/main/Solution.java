package com.holobor.main;

public class Solution {

    public static void main(String[] args) {
        System.out.println(isValidIpv4("0.0.2.3."));
        System.out.println(isValidIpv4("0.0.2.3321"));
        System.out.println(isValidIpv4("0.0.2..3."));
        System.out.println(isValidIpv4("0.00.2.3"));
        System.out.println(isValidIpv4("0.0.e2.3."));
        System.out.println(isValidIpv4("0.0.2.3"));
    }

    public static boolean isValidIpv4(String str) {
        if (str == null) { return false; }
        if (str.length() < 7 || str.length() > 15) { return false; }

        char[] cs = str.toCharArray();
        int num = -1;
        int segment = 0;

        for (char c : cs) {
            if (c >= '0' && c <= '9') {
                if (num == -1) {
                    segment++;
                    num = c - '0';
                } else {
                    if (num == 0 && c == '0') {
                        return false; // 拒绝 00 02 这种情况
                    }
                    num = num * 10 + (c - '0');
                }

                if (num > 255) {
                    return false;
                }
            } else if (c == '.') {
                if (num == -1) {
                    return false; // 拒绝连续的 .
                }
                num = -1;
            } else {
                return false;
            }
        }

        if (segment != 4 || num == -1 /* 防止最后一个字符是 . */) {
            return false;
        }

        return true;
    }
}
