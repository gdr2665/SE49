// File: com.softwareengineering.finsage.views.HolidayDialog.java
package com.softwareengineering.finsage.views;

import com.softwareengineering.finsage.controllers.HolidayController;
import com.softwareengineering.finsage.model.Holiday;
import com.softwareengineering.finsage.utils.UserLoginState;
import com.softwareengineering.finsage.validator.Validator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;

public class HolidayDialog {
    private Stage stage;
    private HolidayController controller;
    private ObservableList<Holiday> holidays;
    private ListView<Holiday> holidayListView;
    private TextField nameField;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;

    public HolidayDialog(Stage owner) {
        this.stage = new Stage();
        this.controller = new HolidayController();
        this.holidays = FXCollections.observableArrayList(controller.getHolidays());

        initUI(owner);
    }

    private void initUI(Stage owner) {
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Manage Holidays");

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // Add new holiday section
        GridPane addPane = new GridPane();
        addPane.setHgap(10);
        addPane.setVgap(10);

        Label nameLabel = new Label("Holiday Name:");
        nameField = new TextField();
        nameField.setPromptText("Enter holiday name");

        Label startDateLabel = new Label("Start Date:");
        startDatePicker = new DatePicker(LocalDate.now());

        Label endDateLabel = new Label("End Date:");
        endDatePicker = new DatePicker(LocalDate.now());

        Button addButton = new Button("Add");
        addButton.setOnAction(e -> addHoliday());

        addPane.add(nameLabel, 0, 0);
        addPane.add(nameField, 1, 0);
        addPane.add(startDateLabel, 0, 1);
        addPane.add(startDatePicker, 1, 1);
        addPane.add(endDateLabel, 0, 2);
        addPane.add(endDatePicker, 1, 2);
        addPane.add(addButton, 1, 3);

        // Holiday list section
        holidayListView = new ListView<>(holidays);
        holidayListView.setCellFactory(param -> new ListCell<Holiday>() {
            @Override
            protected void updateItem(Holiday item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s (%s to %s)",
                            item.getName(),
                            item.getStartDate(),
                            item.getEndDate()));
                }
            }
        });

        // Action buttons
        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> deleteSelectedHoliday());

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> stage.close());

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(deleteButton, closeButton);

        root.getChildren().addAll(addPane, holidayListView, buttonBox);

        Scene scene = new Scene(root, 500, 400);
        stage.setScene(scene);
    }

    private void addHoliday() {
        String name = nameField.getText().trim();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (!Validator.validateHolidayName(name)) {
            showAlert("Invalid Input", "Holiday name must be 1-50 characters long.");
            return;
        }

        if (endDate.isBefore(startDate)) {
            showAlert("Invalid Date", "End date must be after start date.");
            return;
        }

        Holiday holiday = new Holiday();
        holiday.setName(name);
        holiday.setStartDate(startDate);
        holiday.setEndDate(endDate);
        holiday.setUserId(UserLoginState.getCurrentUserId());

        if (controller.addHoliday(holiday)) {
            holidays.setAll(controller.getHolidays());
            nameField.clear();
        } else {
            showAlert("Error", "Failed to add holiday. It may already exist.");
        }
    }

    private void deleteSelectedHoliday() {
        Holiday selected = holidayListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a holiday to delete.");
            return;
        }

        if (controller.deleteHoliday(selected.getId())) {
            holidays.setAll(controller.getHolidays());
        } else {
            showAlert("Error", "Failed to delete holiday.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void show() {
        holidays.setAll(controller.getHolidays());
        stage.show();
    }
}
