/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imageprocess;

import com.atul.JavaOpenCV.Imshow;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

/**
 *
 * @author Calvin He
 */
public class PixelProcessor {

    public void salt(Mat image, int n) {
        for (int k = 0; k < n; k++) {
            int i = (int) (Math.random() * image.cols());
            int j = (int) (Math.random() * image.rows());
            if (image.channels() == 1) {
                image.put(j, i, 255);
            } else if (image.channels() == 3) {
                image.put(j, i, new byte[]{(byte) 255, (byte) 255, (byte) 255});
            }
        }
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        PixelProcessor p = new PixelProcessor();
        Mat image = Highgui.imread("D:\\backup\\opencv\\boldt.jpg");
        p.salt(image,300);
        Imshow im = new Imshow("AAA");
        im.showImage(image);
    }
}
