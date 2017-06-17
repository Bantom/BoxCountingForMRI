package analysisMRIImages.boxCounting.model;

import ij.plugin.DICOM;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Anton Bulgakov
 * @since 18.05.2017
 */
public class ImageMRI implements ImageProcessor{

    private DICOM imageMRI;

    public ImageMRI() {
    }

    public ImageMRI(DICOM imageMRI) {
        this.imageMRI = imageMRI;
    }

    public ImageMRI(String imagePath) {
        try {
            InputStream stream = this.getClass().getResource(imagePath).openStream();
            this.imageMRI = new DICOM(stream);
        } catch (IOException e) {
            System.out.println("Cannot found image using this path.");
        }
    }

    public DICOM getImageMRI() {
        return imageMRI;
    }

    public void setImageMRI(DICOM imageMRI) {
        this.imageMRI = imageMRI;
    }

    @Override
    public void show() {
        imageMRI.show();
    }

    @Override
    public void load() {
        imageMRI.run("Image");
    }

    @Override
    public String toString() {
        return "ImageMRI{" +
                "imageMRI=" + imageMRI +
                '}';
    }
}
