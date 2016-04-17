package app;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Created by Derek on 4/16/16.
 */
public class ChooseImageController extends BorderPane {
    @FXML
    private ImageView leftImage;
    @FXML
    private ImageView rightImage;
    @FXML
    private SplitPane compareImagePane;
    @FXML
    private VBox leftImageDetails;
    @FXML
    private VBox rightImageDetails;
    @FXML
    private Button keepLeftButton;
    @FXML
    private Button keepBothButton;
    @FXML
    private Button keepRightButton;

    private int retValue = -1;

    public ChooseImageController() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/ChooseImagePage.fxml"));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        } catch (IOException e) {
            // OMG!!!!
        }
    }

    public int start(File leftImageFile, File rightImageFile) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

            leftImage.setImage(new Image(new FileInputStream(leftImageFile)));
            leftImage.setFitHeight(Math.min(compareImagePane.getPrefHeight(), leftImage.getImage().getHeight()));
            leftImage.setPreserveRatio(true);
            leftImageDetails.getChildren().add(new Label("File name:"));
            Label leftNameLabel = new Label(leftImageFile.getName());
            leftNameLabel.setWrapText(true);
            leftImageDetails.getChildren().add(leftNameLabel);
            leftImageDetails.getChildren().add(new Label(""));
            leftImageDetails.getChildren().add(new Label("Width:"));
            leftImageDetails.getChildren().add(new Label(leftImage.getImage().getWidth() + ""));
            leftImageDetails.getChildren().add(new Label(""));
            leftImageDetails.getChildren().add(new Label("Height:"));
            leftImageDetails.getChildren().add(new Label(leftImage.getImage().getHeight() + ""));
            leftImageDetails.getChildren().add(new Label(""));
            leftImageDetails.getChildren().add(new Label("Last Modified Date:"));
            leftImageDetails.getChildren().add(new Label(sdf.format(leftImageFile.lastModified())));

            rightImage.setImage(new Image(new FileInputStream(rightImageFile)));
            rightImage.setFitHeight(Math.min(compareImagePane.getPrefHeight(), rightImage.getImage().getHeight()));
            rightImage.setPreserveRatio(true);
            rightImageDetails.getChildren().add(new Label("File name:"));
            Label rightNameLabel = new Label(rightImageFile.getName());
            rightNameLabel.setWrapText(true);
            rightImageDetails.getChildren().add(rightNameLabel);
            rightImageDetails.getChildren().add(new Label(""));
            rightImageDetails.getChildren().add(new Label("Width:"));
            rightImageDetails.getChildren().add(new Label(rightImage.getImage().getWidth() + ""));
            rightImageDetails.getChildren().add(new Label(""));
            rightImageDetails.getChildren().add(new Label("Height:"));
            rightImageDetails.getChildren().add(new Label(rightImage.getImage().getHeight() + ""));
            rightImageDetails.getChildren().add(new Label(""));
            rightImageDetails.getChildren().add(new Label("Last Modified Date:"));
            rightImageDetails.getChildren().add(new Label(sdf.format(rightImageFile.lastModified())));

            Scene scene = new Scene(this);
            Stage stage = new Stage();
            stage.setTitle("Choose a Image");
            stage.setScene(scene);

//            stage.setResizable(false);

            stage.showAndWait();

            return retValue;
        } catch (IOException e) {
            // OMG!!!!
            return -2;
        }
    }

    @FXML
    private void keepLeftImage(ActionEvent event) {
        retValue = 0;
        ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
    }

    @FXML
    private void keepRightImage(ActionEvent event) {
        retValue = 1;
        ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
    }

    @FXML
    private void keepBothImages(ActionEvent event) {
        retValue = 2;
        ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
    }
}
