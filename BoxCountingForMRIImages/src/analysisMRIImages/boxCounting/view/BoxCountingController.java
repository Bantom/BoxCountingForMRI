package analysisMRIImages.boxCounting.view;

import ij.IJ;
import ij.gui.PlotWindow;
import ij.measure.CurveFitter;
import ij.util.Tools;

/**
 * @author Anton Bulgakov
 * @since 21.05.2017
 */
public class BoxCountingController {

    /**
     * Method builds regression graphic using params of lineal equation and points.
     *
     * @param params          prams of lineal function.
     * @param boxSizes        array of box sizes.
     * @param quantityOfBoxes array of quantity of boxes.
     */
    public static void doPlotGraph(double[] params, double[] boxSizes, double[] quantityOfBoxes) {
        final int pointsQuantity = 100;
        float[] px = new float[pointsQuantity];
        float[] py = new float[pointsQuantity];
        double[] a = Tools.getMinMax(boxSizes);
        double xMin = a[0], xMax = a[1];

        a = Tools.getMinMax(quantityOfBoxes);
        double yMin = a[0], yMax = a[1];
        final double inc = (xMax - xMin) / ((double) pointsQuantity - 1);
        double tmp = xMin;

        for (int i = 0; i < pointsQuantity; i++) {
            px[i] = (float) tmp;
            tmp += inc;
        }
        for (int i = 0; i < pointsQuantity; i++) {
            py[i] = (float) CurveFitter.f(CurveFitter.STRAIGHT_LINE, params, px[i]);
        }

        a = Tools.getMinMax(py);
        yMin = Math.min(yMin, a[0]);
        yMax = Math.max(yMax, a[1]);
        PlotWindow pw = new PlotWindow("Графік", "-log(розмір \"коробки\")", "log(кількість \"коробок\")", px, py);
        pw.setLimits(xMin, xMax * 0.9, yMin, yMax * 1.1);
        pw.addPoints(Tools.toFloat(boxSizes), Tools.toFloat(quantityOfBoxes), PlotWindow.CIRCLE);
        final String plotLabel = "Коефіцієнт нахилу прямої: " + IJ.d2s(params[1], 4);
        pw.addLabel(0.25, 0.25, plotLabel);
        pw.draw();
    }
}
