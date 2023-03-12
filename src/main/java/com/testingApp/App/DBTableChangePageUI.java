package com.testingApp.App;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Route("DBTableChange")
@PageTitle("Управление БД PostgreSQL")
public class DBTableChangePageUI extends VerticalLayout implements HasUrlParameter<String> {

    private JdbcTemplate jdbcTemplate;
    private String tableName;
    private String primaryKey;

    @Autowired
    public DBTableChangePageUI(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        Button dbListButton = new Button("Список таблиц БД", eventDBList -> UI.getCurrent().navigate("DBTablesList"));
        Button mainPgButton = new Button("Начальная страница", eventMainPg -> UI.getCurrent().navigate("PostgreSQLBaseControl"));

        QueryParameters parameters = event.getLocation().getQueryParameters();
        tableName = parameters.getParameters().getOrDefault("tableName", Collections.emptyList()).stream().findFirst().orElse("");

        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, tableName);
            primaryKey = primaryKeys.next() ? primaryKeys.getString("COLUMN_NAME") : null;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM " + tableName);

        Grid<Map<String, Object>> grid = new Grid<>();
        grid.setItems(rows);
        for (String key : rows.get(0).keySet()) {
            grid.addColumn(item -> item.get(key).toString()).setHeader(key);
        }

        grid.addItemClickListener(e -> {
            Map<String, Object> item = e.getItem();
            Dialog dialog = new Dialog();
            FormLayout form = new FormLayout();
            for (String key : item.keySet()) {
                TextField textField = new TextField(key);
                if (!key.equalsIgnoreCase(primaryKey)) {
                    textField.setValue(item.get(key).toString());
                } else {
                    textField.setValue(item.get(key).toString());
                    textField.setReadOnly(true);
                }
                form.add(textField);
            }

            Button saveButton = new Button("Сохранить", eventSave -> {
                Map<String, Object> newItem = new HashMap<>(item);
                form.getChildren().forEach(component -> {
                    TextField textField = (TextField) component;
                    String key = textField.getLabel();
                    String value = textField.getValue();
                    newItem.put(key, value);
                });
                jdbcTemplate.update(getUpdateQuery(newItem));
                dialog.close();
                grid.setItems(jdbcTemplate.queryForList("SELECT * FROM " + tableName));
            });

            Button cancelButton = new Button("Отмена", eventCancel -> dialog.close());

            HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);
            form.add(buttonsLayout);

            dialog.add(new H3("Редактирование записи"), form);
            dialog.open();
        });


        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        Button addButton = new Button("Добавить запись");
        addButton.addClickListener(e -> {
            Dialog dialog = new Dialog();
            FormLayout form = new FormLayout();
            for (String key : rows.get(0).keySet()) {
                if (!key.equalsIgnoreCase(primaryKey)) {
                    TextField textField = new TextField(key);
                    form.add(textField);
                }
            }

            Button saveButton = new Button("Сохранить", eventSave -> {
                Map<String, Object> newItem = new HashMap<>();
                form.getChildren().forEach(component -> {
                    TextField textField = (TextField) component;
                    String key = textField.getLabel();
                    String value = textField.getValue();
                    newItem.put(key, value);
                });
                jdbcTemplate.update(getInsertQuery(newItem));
                dialog.close();
                grid.setItems(jdbcTemplate.queryForList("SELECT * FROM " + tableName));
            });

            Button cancelButton = new Button("Отмена", eventCancel -> dialog.close());

            HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);
            form.add(buttonsLayout);

            dialog.add(new H3("Добавление новой записи"), form);
            dialog.open();
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout(dbListButton, mainPgButton, addButton);
        buttonsLayout.setMargin(true);
        add(new H1("Содержимое таблицы " + tableName), buttonsLayout, grid);
    }

    private String getUpdateQuery(Map<String, Object> row) {
        StringBuilder builder = new StringBuilder("UPDATE " + tableName + " SET ");
        List<String> setList = new ArrayList<>();
        List<String> whereList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.equalsIgnoreCase(primaryKey)) {
                whereList.add(key + " = " + value);
            } else {
                setList.add(key + " = '" + value + "'");
            }
        }
        builder.append(String.join(", ", setList));
        builder.append(" WHERE ");
        builder.append(String.join(" AND ", whereList));
        return builder.toString();
    }

    private String getInsertQuery(Map<String, Object> row) {
        StringBuilder builder = new StringBuilder("INSERT INTO " + tableName + " (");
        List<String> keyList = new ArrayList<>();
        List<String> valueList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!key.equalsIgnoreCase(primaryKey)) {
                keyList.add(key);
                valueList.add("'" + value + "'");
            }
        }
        builder.append(String.join(", ", keyList));
        builder.append(") VALUES (");
        builder.append(String.join(", ", valueList));
        builder.append(")");
        return builder.toString();
    }
}
