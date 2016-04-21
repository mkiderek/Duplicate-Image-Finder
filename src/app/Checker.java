package app;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;

public class Checker implements Runnable {

    private class PixelComparator implements Runnable {
        final int beginRow;
        final int endRow;
        final Image image;
        int count;

        PixelComparator(int begin, int end, Image image) {
            this.beginRow = begin;
            this.endRow = end;
            this.image = image;
            count = 0;
        }

        @Override
        public void run() {

        }
    }

    protected int numOfDuplicatePixels;

    public void start() {
        numOfDuplicatePixels = 0;

        // clear old output
        File oldOutDir = (Paths.get("output").toFile());
        File prevFiles[] = oldOutDir.listFiles();
        if (prevFiles != null) {
            for (File prevFile : prevFiles) {
                prevFile.delete();
            }
        }

        // select folder of images to compare
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Please Select a Directory");
        File dir = directoryChooser.showDialog(null);
        if (dir == null) {
            return;
        }
        if (dir.isFile()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error opening directory");
            alert.setHeaderText(null);
            alert.setContentText("A file is chosen.");
            alert.showAndWait();
            return;
        }

        File[] images = dir.listFiles(file -> {
            try {
                String ext = file.getName().substring(file.getName().lastIndexOf('.') + 1);
                return ext.equalsIgnoreCase("bmp")
                        || ext.equalsIgnoreCase("gif")
                        || ext.equalsIgnoreCase("jpeg")
                        || ext.equalsIgnoreCase("jpg")
                        || ext.equalsIgnoreCase("png");
            } catch (ArrayIndexOutOfBoundsException e) {
                return false;
            }
        });

        ArrayList<File> removeList = new ArrayList<>();

        ArrayList<File> imageFiles = new ArrayList<>(Arrays.asList(images));
        for (int i = 0; i < imageFiles.size(); i++) {
            for (int j = i + 1; j < imageFiles.size(); j++) {
                File leftImageFile = imageFiles.get(i);
                File rightImageFile = imageFiles.get(j);
                if (!leftImageFile.equals(rightImageFile)) {

                    if (!hasSimilarMetadata(leftImageFile, rightImageFile)) {
                        continue;
                    }

                    if (isSimilarImage(leftImageFile, rightImageFile)) {
                        ChooseImageController chooseImageController = new ChooseImageController();
                        int ret = chooseImageController.start(leftImageFile, rightImageFile);
                        if (ret == 0) {
//                            System.out.println("keep left");
                            removeList.add(rightImageFile);
                        } else if (ret == 1) {
//                            System.out.println("keep right");
                            removeList.add(leftImageFile);
                        } else if (ret == 2) {
//                            System.out.println("keep both");
                        } else if (ret == -1) {
                            System.out.println("ERROR");
                        } else if (ret == -2) {
                            System.out.println("IOException");
                        } else {
                            System.out.println("OMG! What happened!");
                        }
                    }
                }
            }
        }

        removeList.forEach(imageFiles::remove);

        imageFiles.forEach(file -> {
            try {
                Path outDir = Paths.get("output");
                Files.createDirectories(outDir);
                Files.copy(file.toPath(), outDir.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                // OMG!!!!
                e.printStackTrace();
                System.out.println("File: " + "output/" + file.getName());
                System.out.println(e.getMessage());
                System.out.println("IOException");
            }
        });
    }

    @Override
    public void run() {

    }

    private boolean hasSimilarMetadata(File leftImageFile, File rightImageFile) {
        try {
            Metadata leftImageMetadata = ImageMetadataReader.readMetadata(leftImageFile);
            Metadata rightImageMetadata = ImageMetadataReader.readMetadata(rightImageFile);

            int differentTags = 0;
            int totalTags = 0;
            for (Directory directory : leftImageMetadata.getDirectories()) {
                try {
                    totalTags += directory.getTagCount();
                } catch (NullPointerException e) {
                    break;
                }
            }
            for (Directory directory : rightImageMetadata.getDirectories()) {
                try {
                    totalTags += directory.getTagCount();
                } catch (NullPointerException e) {
                    break;
                }
            }

            for (Directory leftDirectory : leftImageMetadata.getDirectories()) {
                for (Directory rightDirectory : rightImageMetadata.getDirectories()) {
                    if (leftDirectory.equals(rightDirectory)) {
                        for (Tag leftTag : leftDirectory.getTags()) {
                            for (Tag rightTag : rightDirectory.getTags()) {
                                if (leftTag.getTagName().equals(rightTag.getTagName())
                                        && !leftTag.getDescription().equals(rightTag.getDescription())) {
                                    differentTags += 2;}}}}}}
            return differentTags < totalTags * Launcher.metadataMaxDifference;
        } catch (ImageProcessingException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            return true;
        } catch (IOException e) {
            System.err.println("IOException");
            e.printStackTrace();
            return true;
        }
    }

    private boolean isSimilarImage(File leftFile, File rightFile) {
        try {
            Image loi = new Image(new FileInputStream(leftFile));
            Image roi = new Image(new FileInputStream(rightFile));
            if (Math.abs(loi.getHeight() / loi.getWidth() - roi.getHeight() / roi.getWidth()) > 0.2) {
                return false;
            }

            Image lci, rci;
            if (loi.getWidth() < roi.getWidth()) {
                lci = new Image(new FileInputStream(leftFile)
                        , loi.getWidth()
                        , loi.getHeight()
                        , false
                        , false);
                rci = new Image(new FileInputStream(rightFile)
                        , loi.getWidth()
                        , loi.getHeight()
                        , false
                        , false);
            } else {
                lci = new Image(new FileInputStream(leftFile)
                        , roi.getWidth()
                        , roi.getHeight()
                        , false
                        , false);
                rci = new Image(new FileInputStream(rightFile)
                        , roi.getWidth()
                        , roi.getHeight()
                        , false
                        , false);
            }

            double samePixel = 0;
            double totalPixels = lci.getWidth() * lci.getHeight();
            PixelReader lpr = lci.getPixelReader();
            PixelReader rpr = rci.getPixelReader();
            for (int i = 0; i < lci.getWidth(); i++) {
                for (int j = 0; j < lci.getHeight(); j++) {
                    Double lr = lpr.getColor(i, j).getRed();
                    Double lg = lpr.getColor(i, j).getGreen();
                    Double lb = lpr.getColor(i, j).getBlue();

                    Double rr = rpr.getColor(i, j).getRed();
                    Double rg = rpr.getColor(i, j).getGreen();
                    Double rb = rpr.getColor(i, j).getBlue();
                    if (Math.abs(lr - rr) < Launcher.pixelMaxDifference
                            && Math.abs(lg - rg) < Launcher.pixelMaxDifference
                            && Math.abs(lb - rb) < Launcher.pixelMaxDifference) {
                        samePixel++;
                    }
                }
            }

            return samePixel / totalPixels > Launcher.imageSimilarityPercent;

        } catch (IOException e) {
            // OMG!!!!
            return false;
        }
    }
}
