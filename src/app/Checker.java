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

public class Checker {

    // for the whole Checker
    private ArrayList<File> imageFiles;
    private ArrayList<File> removeList;

    // for comparing current pair of images
    private Boolean end;
    private Double dupPixelCount;
    private Double difPixelCount;
    private Double totalPixels;

    public Checker(ArrayList<String> fileFormats) {
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

        imageFiles = new ArrayList<>(Arrays.asList(dir.listFiles(file -> {
            String ext = file.getName().substring(file.getName().lastIndexOf('.') + 1).toLowerCase();
            return fileFormats.contains(ext);
        })));

        removeList = new ArrayList<>();
    }

    public void start() {

        for (int i = 0; i < imageFiles.size(); i++) {
            for (int j = i + 1; j < imageFiles.size(); j++) {
                File leftImageFile = imageFiles.get(i);
                File rightImageFile = imageFiles.get(j);

                System.out.println("Current File name(L,R): " + leftImageFile.getName() + "  ,  " + rightImageFile.getName());

                if (!leftImageFile.equals(rightImageFile)) {
                    if (!hasSimilarMetadata(leftImageFile, rightImageFile)) {
                        continue;
                    }

                    end = false;
                    dupPixelCount = 0.0;
                    difPixelCount = 0.0;
                    totalPixels = 0.0;

                    long pixelStartTime = System.nanoTime();

                    if (isSimilarImage(leftImageFile, rightImageFile)) {

                        System.out.println("Time taken by pixel comparator: " + (System.nanoTime() - pixelStartTime) / 1000000 + "ms");

                        ChooseImageController chooseImageController = new ChooseImageController();
                        int ret = chooseImageController.start(leftImageFile, rightImageFile);
                        if (ret == 0) {
                            removeList.add(rightImageFile);
                        } else if (ret == 1) {
                            removeList.add(leftImageFile);
                        } else if (ret == -1) {
                            System.out.println("ERROR");
                        } else if (ret == -2) {
                            System.out.println("IOException");
                        } else if (ret != 2) {
                            System.out.println("OMG! What happened!");
                        }
                    } else {
                        System.out.println("Time taken by pixel comparator: " + (System.nanoTime() - pixelStartTime) / 1000000 + "ms");
                    }
                }

                System.out.println();
            }
        }

        removeList.forEach(imageFiles::remove);

        imageFiles.forEach(file -> {
            try {
                Path outDir = Paths.get("output");
                Files.createDirectories(outDir);
                Files.copy(file.toPath(), outDir.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        });
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
                                    differentTags += 2;
                                }
                            }
                        }
                    }
                }
            }
            return differentTags < totalTags * Launcher.metadataMaxDifference;
        } catch (ImageProcessingException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
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

            totalPixels = lci.getWidth() * lci.getHeight();

            int availableProcessors = Runtime.getRuntime().availableProcessors();

            int proportion = (int) Math.ceil(lci.getHeight() / availableProcessors);

            Thread[] threads = new Thread[availableProcessors];
            PixelComparator[] pixelComparators = new PixelComparator[availableProcessors];

            // compare pixel rows in different thread
            for (int i = 0; i < availableProcessors - 1; i++) {
                pixelComparators[i] =
                        new PixelComparator(i * proportion, proportion + i * proportion, lci, rci);
                threads[i] = new Thread(pixelComparators[i]);
                threads[i].start();
            }
            pixelComparators[availableProcessors - 1] =
                    new PixelComparator((availableProcessors - 1) * proportion, (int) lci.getHeight(), lci, rci);
            threads[availableProcessors - 1] = new Thread(pixelComparators[availableProcessors - 1]);
            threads[availableProcessors - 1].start();

            for (int i = 0; i < threads.length; i++) {

                if (threads[i].isAlive()) {
                    i--;

                    // if enough different pixels has been found, stop immediately and return false
                    if (difPixelCount / totalPixels > (1.0 - Launcher.imageSimilarityPercent)) {
                        end = true;
                        for (Thread thread : threads) {
                            try {
                                thread.join();
                            } catch (InterruptedException e) {
                                System.out.println(e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        return false;
                    }
                }
            }

            return dupPixelCount / totalPixels >= Launcher.imageSimilarityPercent;

        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private synchronized void incrementDupPixel() {
        dupPixelCount++;
    }

    private synchronized void incrementDifPixel() {
        difPixelCount++;
    }

    private class PixelComparator implements Runnable {
        final int beginRow;
        final int endRow;
        final Image lci;
        final Image rci;

        PixelComparator(int begin, int end, Image lci, Image rci) {
            this.beginRow = begin;
            this.endRow = end;
            this.lci = lci;
            this.rci = rci;
        }

        @Override
        public void run() {
            PixelReader lpr = lci.getPixelReader();
            PixelReader rpr = rci.getPixelReader();
            for (int i = 0; i < lci.getWidth(); i++) {
                for (int j = beginRow; j < endRow; j++) {
                    if (end) {
                        return;
                    }
                    Double lr = lpr.getColor(i, j).getRed();
                    Double lg = lpr.getColor(i, j).getGreen();
                    Double lb = lpr.getColor(i, j).getBlue();

                    Double rr = rpr.getColor(i, j).getRed();
                    Double rg = rpr.getColor(i, j).getGreen();
                    Double rb = rpr.getColor(i, j).getBlue();
                    if (Math.abs(lr - rr) < Launcher.pixelMaxDifference
                            && Math.abs(lg - rg) < Launcher.pixelMaxDifference
                            && Math.abs(lb - rb) < Launcher.pixelMaxDifference)
                    {
                        incrementDupPixel();
                    } else {
                        incrementDifPixel();
                    }
                }
            }
        }
    }
}
