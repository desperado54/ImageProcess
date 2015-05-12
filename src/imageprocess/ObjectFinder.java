package imageprocess;

import com.atul.JavaOpenCV.Imshow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2HSV;
import org.opencv.core.Core;
import static org.opencv.core.Core.NORM_L2;
import static org.opencv.core.CvType.CV_8U;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.TermCriteria;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import org.opencv.video.Video;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Calvin He
 */
public class ObjectFinder {

    private float threshold;
    private boolean isSparse;
    private Mat ROIHistogram;
    //Mat shistogram;

    ObjectFinder(boolean isSparse, float threshold) {
        this.threshold = threshold;
        this.isSparse = isSparse;
    }

    public Mat getHueHistogram(final Mat image, int minSaturation) {

        Mat hist = new Mat();

        // Convert to Lab color space
        Mat hsv = new Mat();
        Imgproc.cvtColor(image, hsv, CV_BGR2HSV);
        Mat mask = new Mat();
        if (minSaturation > 0) {
            // Spliting the 3 channels into 3 images
            List<Mat> v = new ArrayList<>();
            Core.split(hsv, v);
            // Mask out the low saturated pixels
            Imgproc.threshold(v.get(1), mask, minSaturation, 255, THRESH_BINARY);
        }
        // Compute histogram
        Imgproc.calcHist(Arrays.asList(image),
                new MatOfInt(0), // the hue channel used
                mask, // no mask is used
                hist, // the resulting histogram
                new MatOfInt(256), // number of bins
                new MatOfFloat(0.0f, 180.0f) // pixel value range
        );

        return hist;
    }

    public Mat find(final Mat image, MatOfInt channels, MatOfFloat ranges) {

        Mat result = new Mat();

        if (isIsSparse()) { // call the right function based on histogram type

            Imgproc.calcBackProject(Arrays.asList(image),
                    channels, // vector specifying what histogram dimensions belong to what image channels
                    ROIHistogram, // the histogram we are using
                    result, // the resulting back projection image
                    ranges, // the range of values, for each dimension
                    255.0 // the scaling factor is chosen such that a histogram value of 1 maps to 255
            );

        } else {
            Imgproc.calcBackProject(Arrays.asList(image),
                    channels, // vector specifying what histogram dimensions belong to what image channels
                    ROIHistogram, // the histogram we are using
                    result, // the resulting back projection image
                    ranges, // the range of values, for each dimension
                    255.0 // the scaling factor is chosen such that a histogram value of 1 maps to 255
            );
        }

        // Threshold back projection to obtain a binary image
        Mat thresholded = new Mat(result.rows(), result.cols(), result.type());
        if (getThreshold() > 0.0) {
            Imgproc.threshold(result, thresholded, 255 * getThreshold(), 255, THRESH_BINARY);
        }

        return thresholded;
    }

    /**
     * @return the ROIHistogram
     */
    public Mat getROIHistogram() {
        return ROIHistogram;
    }

    /**
     * @param ROIHistogram the ROIHistogram to set
     */
    public void setROIHistogram(Mat ROIHistogram) {
        this.ROIHistogram = ROIHistogram;
    }
    
    /**
     * @return the threshold
     */
    public float getThreshold() {
        return threshold;
    }

    /**
     * @param threshold the threshold to set
     */
    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    /**
     * @return the isSparse
     */
    public boolean isIsSparse() {
        return isSparse;
    }

    /**
     * @param isSparse the isSparse to set
     */
    public void setIsSparse(boolean isSparse) {
        this.isSparse = isSparse;
    }
    
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat image = Highgui.imread("D:\\backup\\opencv\\baboon1.jpg");
        // Define ROI
        Rect rect = new Rect(110, 260, 35, 40);
        Mat imageROI = new Mat(image, rect);
        Core.rectangle(image, new Point(110, 260), new Point(145, 300), new Scalar(0, 0, 255));

        Imshow origIm = new Imshow("Origin");
        origIm.showImage(image);

        ObjectFinder finder = new ObjectFinder(false, 0.2f);

        // Get the Hue histogram
        int minSat = 65;
        Mat hist = finder.getHueHistogram(imageROI, minSat);
        Mat norm = new Mat();
        Core.normalize(hist, norm, 1, 0, NORM_L2);

        finder.setROIHistogram(norm);

        // Convert to HSV space
        Mat hsv = new Mat();
        Imgproc.cvtColor(image, hsv, CV_BGR2HSV);
        // Split the image
        List<Mat> v = new ArrayList<>();
        Core.split(hsv, v);
        
        // Eliminate pixels with low saturation
	Imgproc.threshold(v.get(1), v.get(1),minSat,255,THRESH_BINARY);
	Imshow satIm = new Imshow("Saturation");
	satIm.showImage(v.get(1));
        // Get back-projection of hue histogram
	Mat result= finder.find(hsv,new MatOfInt(0),new MatOfFloat(0.0f,180.0f));

	Imshow resultHueIm = new Imshow("Result Hue");
	resultHueIm.showImage(result);

	Core.bitwise_and(result, v.get(1),result);
	Imshow resultHueAndIm = new Imshow("Result Hue and raw");
	resultHueAndIm.showImage(result);
        
        	// Second image
        Mat image2 = Highgui.imread("D:\\backup\\opencv\\baboon3.jpg");

	// Display image
	Imshow img2Im = new Imshow("Imgage2");
	img2Im.showImage(image2);

	// Convert to HSV space
	Imgproc.cvtColor(image2, hsv, CV_BGR2HSV);

	// Split the image
	Core.split(hsv,v);

	// Eliminate pixels with low saturation
	Imgproc.threshold(v.get(1), v.get(1),minSat,255,THRESH_BINARY);
	Imshow satIm2 = new Imshow("Saturation2");
	satIm2.showImage(v.get(1));

	// Get back-projection of hue histogram
        finder.setThreshold(-1.0f);
	result = finder.find(hsv,new MatOfInt(0),new MatOfFloat(0.0f,180.0f));

	Imshow resultHueIm2 = new Imshow("Result Hue2");
	resultHueIm2.showImage(result);
        
        Core.bitwise_and(result, v.get(1),result);
	Imshow resultHueAndIm2 = new Imshow("Result Hue and raw2");
	resultHueAndIm2.showImage(result);

        Rect rect2 = new Rect(110, 260, 35, 40);
        Core.rectangle(image2, new Point(110, 260), new Point(145, 300), new Scalar(0, 0, 255));

	TermCriteria criteria = new TermCriteria(TermCriteria.MAX_ITER| TermCriteria.EPS,100,0.01);
        int steps = Video.meanShift(result, rect2, criteria);
        
        Core.rectangle(image2, new Point(rect2.x, rect2.y), new Point(rect2.x + rect2.width, rect2.y + rect2.height), new Scalar(0, 255, 0));
        
	Imshow meanshiftIm = new Imshow("Meanshift result");
	meanshiftIm.showImage(image2);
        

    }


}
