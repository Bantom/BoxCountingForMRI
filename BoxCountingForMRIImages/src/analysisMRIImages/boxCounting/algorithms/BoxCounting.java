package analysisMRIImages.boxCounting.algorithms;

import analysisMRIImages.boxCounting.model.ImageMRI;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Anton Bulgakov
 * @since 18.05.2017
 */
public class BoxCounting {
    private FractalCounting fractalCount = new FractalCounting();

    /**
     * Method runs box-counting algorithm for image from params.
     *
     * @param image input image.
     */
    public void run(ImageMRI image) {
        ImagePlus imagePlus = new ImagePlus("Результат", image.getImageMRI().getProcessor().duplicate());
        ImageProcessor imageProcessor = image.getImageMRI().getProcessor();
        fractalCount.setup("", imagePlus);
        fractalCount.run(imageProcessor);
    }
}
