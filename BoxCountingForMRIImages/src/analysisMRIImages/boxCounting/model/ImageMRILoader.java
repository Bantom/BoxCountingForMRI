package analysisMRIImages.boxCounting.model;

import analysisMRIImages.boxCounting.algorithms.BoxCounting;
import ij.plugin.DICOM;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Anton Bulgakov
 * @since 18.05.2017
 */
public class ImageMRILoader {
    private static final Logger logger = Logger.getLogger(ImageMRILoader.class.getName());

    /**
     * Method creates window for choosing directory with images and load them to the program.
     *
     * @return list of images.
     * @throws IOException
     */
    public static List<ImageMRI> loadFromFolder() throws IOException {
        List<ImageMRI> imageMRIList = new ArrayList<>();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(new java.io.File(".dcm"));
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File choosedDirectory = fileChooser.getSelectedFile();
            List<DICOM> imageList = getImagesFromDirectory(choosedDirectory);
            logger.info("From catalog " + choosedDirectory + " was read " + imageList.size() + " images.");
            imageMRIList = toMRIFormat(imageList);
        }
        return imageMRIList;
    }

    /**
     * Method converts images with format dicom to ImageMRI objects.
     * If imageList is null or empty, method returns null.
     *
     * @param imageList dicom images.
     * @return list of ImageMRI objects.
     */
    private static List<ImageMRI> toMRIFormat(List<DICOM> imageList) {
        logger.info(imageList.toString());
        if (imageList != null || !imageList.isEmpty()) {
            List<ImageMRI> imageMRIList = new ArrayList<>();
            for (int i = 0; i < imageList.size(); i++) {
                imageMRIList.add(new ImageMRI(imageList.get(i)));
            }
            return imageMRIList;
        }
        logger.info("List is empty :" + imageList.isEmpty());
        return null;
    }

    /**
     * Method loads images from directory.
     *
     * @param directory directory
     * @return list of dicom images.
     * @throws IOException can`t get access to directory.
     */
    private static List<DICOM> getImagesFromDirectory(File directory) throws IOException {
        final List<DICOM> imageList = new ArrayList<DICOM>();
        Path path = Paths.get(directory.getPath());
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                File file = path.toFile();
                try {
                    InputStream stream = new FileInputStream(file);
                    DICOM image = new DICOM(stream);
                    if (image != null) {
                        imageList.add(image);
                        logger.info("Read images from file " + file);
                    }
                } catch (Exception ex) {
                    logger.warning("Error during the reading from file " + file + ": " + ex.getLocalizedMessage());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return imageList;
    }
}
