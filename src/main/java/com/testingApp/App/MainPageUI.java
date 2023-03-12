package com.testingApp.App;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Route("PostgreSQLBaseControl")
@PageTitle("Управление БД PostgreSQL")
public class MainPageUI extends VerticalLayout {

    private final DataSource dataSource;

    public MainPageUI(DataSource dataSource) {
        this.dataSource = dataSource;

        H1 pageTitle = new H1("Управление БД PostgreSQL. Автор: Кузнецов В.А., У-188. Титульная страница.");
        Button startButton = new Button("Таблицы подключенной БД", event -> UI.getCurrent().navigate("DBTablesList"));
        Button manualButton = new Button("Ручной режим: перейти к таблице...");
        manualButton.addClickListener(event -> openDialog());
        add(pageTitle, startButton, manualButton);
    }

    private void openDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("350px");
        TextField tableNameField = new TextField("Введите название таблицы");
        tableNameField.setWidth("300px");

        Button okButton = new Button("Ок", event -> {
            String tableName = tableNameField.getValue();
            if (!tableName.isEmpty() && tableExists(tableName)) {
                Map<String, String> params = new HashMap<>();
                params.put("tableName", tableName);
                UI.getCurrent().navigate("DBTableChange", QueryParameters.simple(params));
            } else {
                Dialog errorDialog = new Dialog();
                errorDialog.add(new H2("Ошибка! Таблицы с названием \"" + tableName + "\" нет в базе."));
                errorDialog.open();
            }
            dialog.close();
        });

        Button cancelButton = new Button("Отмена", event -> dialog.close());

        HorizontalLayout buttonsLayout = new HorizontalLayout(okButton, cancelButton);

        dialog.add(tableNameField, buttonsLayout);
        dialog.open();
    }

    private boolean tableExists(String tableName) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, tableName, new String[]{"TABLE"});
            return tables.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}