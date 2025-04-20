// File: com.softwareengineering.finsage.views.CategoryDialog.java
package com.softwareengineering.finsage.views;

import com.softwareengineering.finsage.controllers.CategoryController;
import com.softwareengineering.finsage.model.Category;
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

public class CategoryDialog {
    private Stage stage;
    private CategoryController controller;
    private ObservableList<Category> categories;
    private ListView<Category> categoryListView;
    private TextField nameField;

    public CategoryDialog(Stage owner) {
        this.stage = new Stage();
        this.controller = new CategoryController();
        this.categories = FXCollections.observableArrayList(controller.getCategories());

        initUI(owner);
    }

    private void initUI(Stage owner) {
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Manage Categories");

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // Add new category section
        GridPane addPane = new GridPane();
        addPane.setHgap(10);
        addPane.setVgap(10);

        Label nameLabel = new Label("Category Name:");
        nameField = new TextField();
        nameField.setPromptText("Enter category name");

        Button addButton = new Button("Add");
        addButton.setOnAction(e -> addCategory());

        addPane.add(nameLabel, 0, 0);
        addPane.add(nameField, 1, 0);
        addPane.add(addButton, 2, 0);

        // Category list section
        categoryListView = new ListView<>(categories);
        categoryListView.setCellFactory(param -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        // Action buttons
        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> deleteSelectedCategory());

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> stage.close());

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(deleteButton, closeButton);

        root.getChildren().addAll(addPane, categoryListView, buttonBox);

        Scene scene = new Scene(root, 400, 400);
        stage.setScene(scene);
    }

    private void addCategory() {
        String name = nameField.getText().trim();
        if (!Validator.validateCategoryName(name)) {
            showAlert("Invalid Input", "Category name must be 1-50 characters long and can only contain letters, numbers, Chinese characters and spaces.");
            return;
        }

        Category category = new Category();
        category.setName(name);
        category.setUserId(UserLoginState.getCurrentUserId());

        if (controller.addCategory(category)) {
            categories.setAll(controller.getCategories());
            nameField.clear();
        } else {
            showAlert("Error", "Failed to add category. It may already exist.");
        }
    }

    private void deleteSelectedCategory() {
        Category selected = categoryListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a category to delete.");
            return;
        }

        if (controller.deleteCategory(selected.getId())) {
            categories.setAll(controller.getCategories());
        } else {
            showAlert("Error", "Failed to delete category. It may be in use by transactions.");
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
        categories.setAll(controller.getCategories());
        stage.show();
    }
}
