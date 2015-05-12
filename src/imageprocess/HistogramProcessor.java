/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imageprocess;

import com.atul.JavaOpenCV.Imshow;
import java.util.Arrays;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_32S;
import static org.opencv.core.CvType.CV_8U;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

/**
 *
 * @author Calvin He
 */
public class HistogramProcessor {

    public static Mat getGrayHistogram(Mat image) {

        Mat grayHist = new Mat();

        // Compute histogram
        Imgproc.calcHist(Arrays.asList(image), //histogram of 1 image only
                new MatOfInt(0), // the channel used
                new Mat(), // no mask is used
                grayHist, // the resulting histogram
                new MatOfInt(256), // number of bins, hist size
                new MatOfFloat(0.0f, 255.0f) // BRG range
        );

        return grayHist;
    }

    public static Mat getHistogram(Mat image) {

        Mat hist = new Mat();

        // Compute histogram
        Imgproc.calcHist(Arrays.asList(image), //histogram of 1 image only
                new MatOfInt(0, 1, 2), // the channel used
                new Mat(), // no mask is used
                hist, // the resulting histogram
                new MatOfInt(256, 256, 256), // number of bins, hist size
                new MatOfFloat(0.0f, 255.0f, 0.0f, 255.0f, 0.0f, 255.0f) // BRG range
        );

        return hist;
    }

    public static Mat getHueHistogram(Mat image) {

        Mat hue = new Mat();

        // Compute histogram
        Imgproc.calcHist(Arrays.asList(image), //histogram of 1 image only
                new MatOfInt(0, 1, 2), // the channel used
                new Mat(), // no mask is used
                hue, // the resulting histogram
                new MatOfInt(256, 256, 256), // number of bins, hist size
                new MatOfFloat(0.0f, 255.0f, 0.0f, 255.0f, 0.0f, 255.0f) // BRG range
        );

        return hue;
    }

    public static Mat getHistogramImage(Mat image) {

        // Compute histogram first
        Mat hist = getGrayHistogram(image);
        // Get min and max bin values

        MinMaxLocResult locPeak = Core.minMaxLoc(hist);
        double maxVal = locPeak.maxVal;
        double minVal = locPeak.minVal;

        // Image on which to display histogram
        Mat histImg = new Mat(image.rows(), image.rows(), CV_8U, new Scalar(255));

        // set highest point at 90% of nbins
        int hpt = (int) (0.9 * 256);

        // Draw vertical line for each bin 
        for (int h = 0; h < 256; h++) {

            double[] f = hist.get(h, 0);
            float binVal = (float) f[0];
            int intensity = (int) (binVal * hpt / maxVal);
            Core.line(histImg, new Point(h, 256.0d), new Point(h, 256.0d - intensity), Scalar.all(0));
        }
        return histImg;
    }

    // Stretches the source image.
    public static Mat stretch(Mat image, int minValue) {
        // Compute histogram first
        Mat hist = getGrayHistogram(image);

        // find left extremity of the histogram
        int imin = 0;
        for (; imin < 256; imin++) {
            System.out.println(String.format("[%d] = %f", imin, hist.get(imin, 0)[0]));
            if (hist.get(imin, 0)[0] > minValue) {
                break;
            }
        }
        // find right extremity of the histogram
        int imax = 255;
        for (; imax >= 0; imax--) {
            if (hist.get(imax, 0)[0] > minValue) {
                break;
            }
        }

        // Create lookup table
        Mat lookup = new Mat(256, 1, CV_8U);

        for (int i = 0; i < 256; i++) {
            if (i < imin) {
                lookup.put(i, 0, 0);
            } else if (i > imax) {
                lookup.put(i, 0, 255);
            } else {
                lookup.put(i, 0, 255.0 * (i - imin) / (imax - imin) + 0.5);
            }
        }
        // Apply lookup table
        Mat result;
        result = applyLookUp(image, lookup);

        return result;
    }
    
//    public static Mat RGBSwap(Mat image) {
//
//        // Create lookup table
//        Mat lookup = new Mat((int)Math.pow(256,3), 1, CV_32S);
//
//        for (int i = 0; i < 256; i++) {
//            if (i < imin) {
//                lookup.put(i, 0, 0);
//            } else if (i > imax) {
//                lookup.put(i, 0, 255);
//            } else {
//                lookup.put(i, 0, 255.0 * (i - imin) / (imax - imin) + 0.5);
//            }
//        }
//        // Apply lookup table
//        Mat result;
//        result = applyLookUp(image, lookup);
//
//        return result;
//    }
    
    public static Mat applyLookUp(Mat image, Mat lookup) {
        // Set output image (always 1-channel)
        Mat result = new Mat(image.rows(), image.cols(), CV_8U);

//        for (int i = 0; i < image.cols(); i++) {
//            for (int j = 0; j < image.rows(); j++) {
//                double[] data = image.get(j, i);
//                double newIntensity = lookup.get((int)data[0], 0)[0];
//                result.put(j, i, newIntensity);
//            }
//        }
        Core.LUT(image, lookup, result);
        return result;
    }
    
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat image = Highgui.imread("D:\\backup\\opencv\\group.jpg",0);
        Imshow origIm = new Imshow("Origin");
        origIm.showImage(image);
        Mat streched = stretch(image,50);
        Imshow strechedIm = new Imshow("Streched");
        strechedIm.showImage(streched);
//        Mat thresholded = new Mat();
//        Imgproc.threshold(image, thresholded, 60, 255, THRESH_BINARY);
//        Mat hist = getHistogramImage(image);
//        Imshow groupIm = new Imshow("Group");
//        groupIm.showImage(thresholded);
//        Imshow histIm = new Imshow("GrayHistogram");
//        histIm.showImage(hist);
    }
}
