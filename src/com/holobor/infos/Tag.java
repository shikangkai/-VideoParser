package com.holobor.infos;

import com.holobor.helper.DatabaseHelper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Tag {
    public final String name;
    public final String desc;

    public Tag(String name, String desc) {
        this.name = name == null ? "" : name;
        this.desc = desc == null ? "" : desc;
    }

    public boolean insertIfNeeded() {
        try {
            return DatabaseHelper.executeSql(String.format("insert into tag (`name`, `desc`) select '%s', '%s' from dual where not exists (select id from tag where name = '%s')", name, desc, name));
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getId() {
        ResultSet resultSet = null;
        try {
            resultSet = DatabaseHelper.query(String.format("select id from tag where name = '%s'", name));
            if (resultSet != null && resultSet.next()) {
                return resultSet.getInt("id");
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
