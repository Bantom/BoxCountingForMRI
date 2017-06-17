package analysisMRIImages;

import analysisMRIImages.boxCounting.algorithms.BoxCounting;
import analysisMRIImages.boxCounting.model.ImageMRI;


/**
 * @author Anton Bulgakov
 * @since 18.05.2017
 */
public class Main {

    private static String path = "/resources/image.dcm";

    public static void main(String[] args) {
        ImageMRI image = new ImageMRI(path);
        BoxCounting algorithm = new BoxCounting();
        image.load();
        image.show();
        algorithm.run(image);
    }
}
