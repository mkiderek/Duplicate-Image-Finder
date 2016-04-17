package app;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by Derek on 4/17/16.
 */
public class ConfigurePage extends SplitPane {
    @FXML
    CheckBox formatBMP;
    @FXML
    CheckBox formatGIF;
    @FXML
    CheckBox formatJPG;
    @FXML
    CheckBox formatPNG;
    @FXML
    Slider sliderImage;
    @FXML
    Slider sliderPixel;
    @FXML
    Slider sliderMetadata;
    @FXML
    Label textImageValue;
    @FXML
    Label textPixelValue;
    @FXML
    Label textMetadataValue;

    private int retval = -1;

    public ConfigurePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ConfigurePage.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public int start() {
        sliderImage.valueProperty().addListener((observable, oldValue, newValue) ->
                textImageValue.setText(String.format("%.1f", sliderImage.getValue())));
        sliderPixel.valueProperty().addListener((observable, oldValue, newValue) ->
                textPixelValue.setText(String.format("%.1f", sliderPixel.getValue())));
        sliderMetadata.valueProperty().addListener((observable, oldValue, newValue) ->
                textMetadataValue.setText(String.format("%.1f", sliderMetadata.getValue())));

        Scene scene = new Scene(this);
        Stage stage = new Stage();
        stage.setScene(scene);
//        stage.setResizable(false);
        stage.showAndWait();
        return retval;
    }

    @FXML
    private void ok(ActionEvent event) {
        retval = 0;
        ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
    }

    @FXML
    private void cancel(ActionEvent event) {
        ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
    }
}
