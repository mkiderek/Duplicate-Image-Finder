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
import javafx.stage.WindowEvent;

import java.io.*;
import java.util.ArrayList;

public class Launcher extends Application {

    private final String TEMPLATE_CONTENT =
            "Used to compare images. Higher->Higher similarity. Range:0.0-1.0\n"
            + "0.9\n"
            + "Used to compare pixels. Lower->Higher similarity. Range:0.0-1.0\n"
            + "0.2\n"
            +"Used to compare Metadata. Lower->Higher similarity. Range:0.0-1.0\n"
            + "0.5\n";

    static double imageSimilarityPercent = 0.9;
    static double pixelMaxDifference = 0.2;
    static double metadataMaxDifference = 0.5;
    static ArrayList<String> fileFormats;
    private Checker checker = null;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        fileFormats = new ArrayList<>();
        fileFormats.add("bmp");
        fileFormats.add("gif");
        fileFormats.add("jpg");
        fileFormats.add("jpeg");
        fileFormats.add("png");
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
            br.readLine();
            metadataMaxDifference = Double.parseDouble(br.readLine());
        } catch (FileNotFoundException e) {
            // no config file, use default value
            imageSimilarityPercent = 0.8;
            pixelMaxDifference = 0.1;
            metadataMaxDifference = 0.5;
        } catch (NullPointerException e) {
            System.err.println("Insufficient Information");
            imageSimilarityPercent = 0.8;
            pixelMaxDifference = 0.1;
            metadataMaxDifference = 0.5;
        } catch (NumberFormatException e) {
            System.err.println("Wrong Information");
            imageSimilarityPercent = 0.8;
            pixelMaxDifference = 0.1;
            metadataMaxDifference = 0.5;
        }

        AnchorPane pane = (AnchorPane) loader.load();
        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
//        primaryStage.setTitle("Duplicate Image Checker");
        primaryStage.setResizable(false);
        primaryStage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, event -> saveConfigFile());
        primaryStage.show();
    }

    private void saveConfigFile() {
        try {
            File templateFile = new File("config");
            BufferedWriter bw =
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(templateFile)));
            String content =
                    "Used to compare images. Higher->Higher similarity. Range:0.0-1.0\n"
                            + imageSimilarityPercent + "\n"
                            + "Used to compare pixels. Lower->Higher similarity. Range:0.0-1.0\n"
                            + pixelMaxDifference + "\n"
                            +"Used to compare Metadata. Lower->Higher similarity. Range:0.0-1.0\n"
                            + metadataMaxDifference + "\n";
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            // OMG!!!!
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void configure(ActionEvent event) {
        ConfigurePage configurePage = new ConfigurePage();
        int retval = configurePage.start(fileFormats);
        if (retval == 0) {
            imageSimilarityPercent = configurePage.sliderImage.getValue();
            pixelMaxDifference = configurePage.sliderPixel.getValue();
            metadataMaxDifference = configurePage.sliderMetadata.getValue();
            fileFormats.clear();
            if (configurePage.formatBMP.isSelected()) {
                fileFormats.add("bmp");
            }
            if (configurePage.formatGIF.isSelected()) {
                fileFormats.add("gif");
            }
            if (configurePage.formatJPG.isSelected()) {
                fileFormats.add("jpg");
                fileFormats.add("jpeg");
            }
            if (configurePage.formatPNG.isSelected()) {
                fileFormats.add("png");
            }
        }
    }

    @FXML
    private void launch(ActionEvent event) {
        if (checker == null) {
            checker = new Checker();
            checker.start(fileFormats);
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
