package app;/**
 * Created by Derek on 4/17/16.
 */

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.*;

public class Launcher extends Application {

    private final String TEMPLATE_CONTENT =
            "Used to decide whether two images are the same. higher=higher similarity. Range:0.0-1.0\n"
            + "0.8\n"
            + "Used to compare pixels. lower=higher similarity. Range:0.0-1.0\n"
            +"0.1\n";

    static double imageSimilarityPercent = 0.8;
    static double pixelMaxDifference = 0.1;
    private Checker checker = null;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("LauncherPage.fxml"));

        try {
            BufferedReader br =
                    new BufferedReader(
                            new InputStreamReader(
                                    new FileInputStream(
                                            new File("config"))));
            br.readLine();
            imageSimilarityPercent = Double.parseDouble(br.readLine());
            br.readLine();
            pixelMaxDifference = Double.parseDouble(br.readLine());
        } catch (FileNotFoundException e) {
            // no config file, use default value
            imageSimilarityPercent = 0.8;
            pixelMaxDifference = 0.1;
        } catch (NullPointerException e) {
            System.err.println("Insufficient Information");
            imageSimilarityPercent = 0.8;
            pixelMaxDifference = 0.1;
        } catch (NumberFormatException e) {
            System.err.println("Wrong Information");
            imageSimilarityPercent = 0.8;
            pixelMaxDifference = 0.1;
        }

        AnchorPane pane = (AnchorPane) loader.load();
        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
//        primaryStage.setTitle("Duplicate Image Checker");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @FXML
    private void launch(ActionEvent event) {
        if (checker == null) {
            checker = new Checker();
            checker.start();
            checker = null;
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Duplicate Checker");
            alert.setHeaderText(null);
            alert.setContentText("You cannot launch 2 checker at the same time");
            alert.showAndWait();
        }
    }

    @FXML
    private void createTemplateFile(ActionEvent event) {
        try {
            File templateFile = new File("config");
            BufferedWriter bw =
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(templateFile)));
            bw.write(TEMPLATE_CONTENT);
            bw.close();
        } catch (IOException e) {
            // OMG!!!!
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
