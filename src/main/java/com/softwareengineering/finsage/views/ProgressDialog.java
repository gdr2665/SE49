package com.softwareengineering.finsage.views;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ProgressDialog extends Alert {
    public ProgressDialog(String message) {
        super(AlertType.NONE);
        setTitle("Please Wait");
        setHeaderText(null);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

        Label label = new Label(message);

        VBox vbox = new VBox(10, label, progressBar);
        vbox.setPrefSize(300, 100);

        getDialogPane().setContent(vbox);
    }
}
