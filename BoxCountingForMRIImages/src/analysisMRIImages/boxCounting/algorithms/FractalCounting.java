package analysisMRIImages.boxCounting.algorithms;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.measure.CurveFitter;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.util.ArrayList;

import static analysisMRIImages.boxCounting.view.BoxCountingController.doPlotGraph;

/**
 * @author Anton Bulgakov
 * @since 17.05.2017
 */
class FractalCounting implements PlugInFilter {
    private double fractalDimension;
    // Main image
    ImagePlus imRef;
    // flag for error situations
    boolean noGo = false;
    // default params for dividing onto blocks
    final int autoDiv = 4;
    final int autoMin = 2;

    // input params
    boolean graphic = false;
    boolean consoleOutput = false;
    boolean usingNoise = true;
    int maxBox = 24;
    int minBox = 2;
    int numberOfSets = 1;

    /**
     * Method runs box-counting algorithm.
     *
     * @param arg system params.
     * @param imp image.
     * @return status of running.
     */
    public int setup(String arg, ImagePlus imp) {
        imRef = imp;
        noGo = (imp == null);
        getParams();
        return 1;
    }

    /**
     * Method gets params from view.
     */
    private void getParams() {
        GenericDialog gd = new GenericDialog("Fractal dimension ");

        gd.addCheckbox("Build plot", graphic);
        gd.addCheckbox("Consider noise", usingNoise);
        gd.addCheckbox("Write results to console", consoleOutput);
        gd.addMessage("");
        gd.addNumericField("Quantity of iterations", numberOfSets, 0);
        gd.showDialog();

        if (gd.wasCanceled()) {
            if (imRef != null)
                imRef.unlock();
            noGo = true;
        }

        graphic = gd.getNextBoolean();
        usingNoise = gd.getNextBoolean();
        consoleOutput = gd.getNextBoolean();

        numberOfSets = (int) gd.getNextNumber();
        if (numberOfSets < 1) {
            IJ.write("Quantity must be at least 1. Please enter another value.");
            noGo = true;
        }
    }

    /**
     * Method uses algorithm of fractal dimension for finding fractal dimension and building graph.
     *
     * @param ip image for counting
     */
    public void run(ImageProcessor ip) {
        if (noGo) return;

        try {
            final int width = ip.getWidth();
            final int height = ip.getHeight();

            maxBox = Math.max(width, height) / autoDiv;
            minBox = autoMin;

            if (width <= 0 || height <= 0) {
                IJ.write("\n Empty image.");
                return;
            }

            float min = Float.MAX_VALUE;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    float value = ip.getPixelValue(x, y);
                    if (value < min)
                        min = value;
                }
            }

            // Create variables we need and set them
            int bestCount;
            int count = 0;
            ArrayList<Double> xList = new ArrayList<Double>();
            ArrayList<Double> yList = new ArrayList<Double>();
            int xPosition, yPosition;
            int xGrid, yGrid;
            int xStart, yStart;
            int xEnd, yEnd;
            double boxZMin, boxZMax;

            for (int boxSize = maxBox; boxSize >= minBox; boxSize--) {
                bestCount = Integer.MAX_VALUE; // Init count for this boxSize
                final int increment = Math.max(1, boxSize / numberOfSets);

                for (int gridOffsetX = 0; (gridOffsetX < width) && (gridOffsetX < boxSize); gridOffsetX += increment) {
                    for (int gridOffsetY = 0; (gridOffsetY < height) && (gridOffsetY < boxSize); gridOffsetY += increment) {
                        count = 0;
                        final int iMax = width + gridOffsetX;
                        final int jMax = height + gridOffsetY;

                        // Iterate over box-grid
                        for (int i = 0; i <= iMax; i += boxSize) {
                            xGrid = -gridOffsetX + i;
                            for (int j = 0; j <= jMax; j += boxSize) {
                                yGrid = -gridOffsetY + j;

                                xStart = 0;
                                if (xGrid < 0) {
                                    xStart = -xGrid;
                                }
                                if ((boxSize + xGrid) >= width) {
                                    xEnd = Math.min(width, (width - xGrid));
                                } else {
                                    xEnd = boxSize;
                                }

                                yStart = 0;
                                if (yGrid < 0) {
                                    yStart = -yGrid;
                                }
                                yEnd = boxSize;
                                if ((boxSize + yGrid) >= height) {
                                    yEnd = Math.min(height, (height - yGrid));
                                }

                                boxZMin = Float.POSITIVE_INFINITY;
                                boxZMax = Float.NEGATIVE_INFINITY;

                                // Inspect box
                                for (int x = xStart; x < xEnd; x++) {
                                    xPosition = x + xGrid;

                                    for (int y = yStart; y < yEnd; y++) {
                                        yPosition = y + yGrid;
                                        double zValue = ip.getPixelValue(xPosition, yPosition);
                                        if (zValue < boxZMin) boxZMin = zValue;
                                        if (zValue > boxZMax) boxZMax = zValue;
                                    }
                                }
                                int boxes = 0;
                                // If a box is entirely outside image edges,
                                // ignore this box
                                if (boxZMax == Float.NEGATIVE_INFINITY) continue;
                                if (usingNoise) {
                                    boxes = 1 + (int) ((boxZMax - min + 1) / boxSize);
                                } else {
                                    boxes = 1 + (int) ((boxZMax - boxZMin + 1) / boxSize);
                                }
                                count += boxes;
                            }
                        }
                        if (count < bestCount) {
                            bestCount = count;
                        }
                    }
                }

                xList.add((double) boxSize / (double) width);
                yList.add((double) bestCount);

                if (consoleOutput) {
                    IJ.write("Quantity of \"boxes\" " + bestCount + " with size " + boxSize);
                }
            }

            if (xList.size() == 0) {
                IJ.write("\nError! Please enter another params.");
                return;
            }

            double[] boxSizes = new double[xList.size()];
            double[] boxCounts = new double[yList.size()];
            for (int i = 0; i < boxSizes.length; i++) {
                boxSizes[i] = -Math.log((Double) xList.get(i));
                boxCounts[i] = Math.log((Double) yList.get(i));
            }

            if (consoleOutput) {
                IJ.write("Used " + boxSizes.length + " different \"boxes\" from "
                        + maxBox + " till " + minBox +
                        " with " + numberOfSets + " iterations for every \"box\".");
            }
            // defining regression function using coordinates of points
            CurveFitter cf = new CurveFitter(boxSizes, boxCounts);
            cf.doFit(CurveFitter.STRAIGHT_LINE);
            double[] p = cf.getParams();
            final String label = imRef.getTitle() + ": Fractal dimension: " + IJ.d2s(p[1], 4);
            fractalDimension = p[1];
            IJ.write(label);

            if (graphic) doPlotGraph(p, boxSizes, boxCounts);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (imRef != null) imRef.unlock();
    }

    /**
     * Method returns fractal dimension of image.
     *
     * @return fractal dimension.
     */
    public double getFractalDimension() {
        return fractalDimension;
    }
}
