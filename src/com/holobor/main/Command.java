package com.holobor.main;

public class Command {

    public static void main(String[] args) {
        String option = args[0];
        switch (option) {
            case "recreate":
                long marginStartMs = Long.parseLong(args[1]);
                long marginEndMs = Long.parseLong(args[2]);

                break;
        }
    }
}
