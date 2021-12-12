package com.holobor.main;

import com.holobor.helper.DatabaseHelper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Options {

    public static void main(String[] args) {

        listTags();
    }


    private static void listTags() {
        try {
            ResultSet resultSet = DatabaseHelper.query("select name, id from tag");
            while (resultSet.next()) {
                System.out.println(resultSet.getString(1));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
