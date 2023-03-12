package com.testingApp.App;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.QueryParameters;


import java.util.*;

import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Route("DBTablesList")
@PageTitle("Управление БД PostgreSQL")
public class DBTablesListPageUI extends VerticalLayout {
    public DBTablesListPageUI() {
        String DBName = new String();
        Button backButton = new Button("Назад", event -> UI.getCurrent().navigate("PostgreSQLBaseControl"));
        List<TableInfo> tableInfoList = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/DB_Kuznetsov", "postgres", "uwgduwdipch3")) {
            Statement statement = connection.createStatement();
            DBName = connection.getCatalog();
            ResultSet resultSet = statement.executeQuery("SELECT table_name, pg_catalog.obj_description(pg_class.oid) as table_comment FROM information_schema.tables LEFT OUTER JOIN pg_class ON pg_class.relname = information_schema.tables.table_name WHERE table_type='BASE TABLE' AND table_schema='public'");
            while (resultSet.next()) {
                String tableName = resultSet.getString("table_name");
                String tableComment = resultSet.getString("table_comment");
                tableInfoList.add(new TableInfo(tableName, tableComment));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Grid<TableInfo> tableGrid = new Grid<>(TableInfo.class);
        tableGrid.setItems(tableInfoList);
        tableGrid.setColumns("tableName", "tableComment");
        tableGrid.getColumnByKey("tableName").setHeader("Имя таблицы");
        tableGrid.getColumnByKey("tableComment").setHeader("Описание таблицы");

        tableGrid.addSelectionListener(event -> {
            TableInfo selected = event.getFirstSelectedItem().orElse(null);
            if (selected != null) {
                String tableName = selected.getTableName();
                Map<String, String> params = new HashMap<>();
                params.put("tableName", tableName);
                UI.getCurrent().navigate("DBTableChange", QueryParameters.simple(params));
            }
        });

        add(new H1("Список таблиц БД " + DBName),backButton, tableGrid);
    }

    public class TableInfo {
        private String tableName;
        private String tableComment;

        public TableInfo(String tableName, String tableComment) {
            this.tableName = tableName;
            this.tableComment = tableComment;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getTableComment() {
            return tableComment;
        }

        public void setTableComment(String tableComment) {
            this.tableComment = tableComment;
        }
    }
}
