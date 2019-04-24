//
//
//import org.opencv.core.*;
//import org.opencv.highgui.HighGui;
//import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.imgproc.Imgproc;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class HightlightRemove {
//
//    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
//
//    static String imgPath="F:\\opencvPhoto\\photo\\test3.jpg";
//    static String savePath="F:\\opencvPhoto\\result\\";
//
//    public static void main(String[] args) {
//        //灰度化+二值化
////        Mat src= Imgcodecs.imread(imgPath);
//        Mat src=highlightRemove(Imgcodecs.imread(imgPath));
//        Mat temp=src.clone();
//        Mat gray=new Mat();
//        Imgproc.cvtColor(src,gray,Imgproc.COLOR_BGR2GRAY);
//        Imgproc.blur( gray, gray,new Size(3,3));
//        Imgcodecs.imwrite(savePath+"gray.jpg",gray);
//        Mat threshold=new Mat();
//        Imgproc.threshold(gray,threshold,100,255,Imgproc.THRESH_BINARY_INV);
//        Imgcodecs.imwrite(savePath+"threshold.jpg",threshold);
//        System.out.println("通道数="+threshold.channels());
//        //黑白转换
////        int num_rows = threshold.rows();
////        int num_col = threshold.cols();
////        for (int i = 0; i < num_rows; i++) {
////            for (int j = 0; j < num_col; j++) {
////                double[] pixel=threshold.get(i,j);
////                for(int k=0;k<pixel.length;k++)
////                    pixel[k]=Math.abs(255-pixel[k]);
////                threshold.put(i,j,pixel);
////            }
////        }
//        //显示二值化+黑白转换结果
////        imshow("threshold",threshold);
////        waitKey(0);
//
//        //轮廓查找
//        List<MatOfPoint> contours=new ArrayList<MatOfPoint>();
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(threshold,contours,hierarchy,Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
//        System.out.println("轮廓数量："+contours.size());
//        System.out.println("轮廓类型："+hierarchy);
//
//
//        double maxArea=0;
//        int maxContoursNum=0;
//        MatOfPoint table=contours.get(0);
//        MatOfPoint paper=contours.get(0);
//        for(int i=0;i<contours.size();i++){
//            MatOfPoint2f matOfPoint2f=new MatOfPoint2f();
////            Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), matOfPoint2f, 3, true);
////            double contourarea=Imgproc.contourArea(matOfPoint2f);
//            double contourarea=Imgproc.contourArea(contours.get(i));
//
//            if(contourarea>maxArea){
//                maxArea=contourarea;
//                table=paper;
//                paper=contours.get(i);
//                maxContoursNum=i;
////                paper=new MatOfPoint(matOfPoint2f.toArray());
//            }
//        }
//
//        Rect paperRect=Imgproc.boundingRect(paper);
//        Rect tableRect=Imgproc.boundingRect(table);
//        Imgproc.rectangle(src,paperRect,new Scalar(255,0,0),9);
//        Imgproc.rectangle(src,tableRect,new Scalar(0,255,0),10);
//
//        Mat paperRect_roi=new Mat(src,paperRect);
//        Mat tablerRect_roi=new Mat(src,tableRect);
//        Mat paperRect_roi_result=new Mat();
//        Mat tablerRect_roi_result=new Mat();
//        paperRect_roi.copyTo(paperRect_roi_result);
//        tablerRect_roi.copyTo(tablerRect_roi_result);
//        Imgcodecs.imwrite(savePath+"paperRect_roi.jpg",paperRect_roi_result);
//        Imgcodecs.imwrite(savePath+"tablerRect_roi.jpg",tablerRect_roi_result);
//
//
////        RotatedRect minPaperRect = Imgproc.minAreaRect(new MatOfPoint2f(paper.toArray()));
////        RotatedRect minTableRect = Imgproc.minAreaRect(new MatOfPoint2f(table.toArray()));
////        System.out.println(minPaperRect.angle);
////        System.out.println(minTableRect.angle);
////        Point[] points=new Point[4];
////        minPaperRect.points(points);
////        for (int i = 0; i < 4; i++)
////            Imgproc.line(src, points[i], points[(i+1)%4],new Scalar(0,255,255),6);
////        minTableRect.points(points);
////        for (int i = 0; i < 4; i++)
////            Imgproc.line(src, points[i], points[(i+1)%4],new Scalar(255,0,255),6);
//
//        List<MatOfPoint> maxContour=new ArrayList<MatOfPoint>();
//        maxContour.add(contours.get(maxContoursNum));
//        Imgproc.drawContours(src,maxContour,-1,new Scalar(0,0,255),3);
////        Imgproc.drawContours(src,contours,-1,new Scalar(0,0,255),3);
//        Imgcodecs.imwrite(savePath+"all.jpg",src);
//
//    }
//
//    static public Mat highlightRemove(Mat src) {
//        Mat dst = new Mat(src.size(), src.type());
//        for (int i = 0; i < src.rows(); i++) {
//
//            for (int j = 0; j < src.cols(); j++) {
//                double B = src.get(i, j)[0];
//                double G = src.get(i, j)[1];
//                double R = src.get(i, j)[2];
//
//                double alpha_r = R / (R + G + B);
//                double alpha_g = G / (R + G + B);
//                double alpha_b = B / (R + G + B);
//
//                double alpha = Math.max(Math.max(alpha_r, alpha_g), alpha_b);
//                double MaxC = Math.max(Math.max(R, G), B);
//                double minalpha = Math.min(Math.min(alpha_r, alpha_g), alpha_b);
//                double beta_r = 1 - (alpha - alpha_r) / (3 * alpha - 1);
//                double beta_g = 1 - (alpha - alpha_g) / (3 * alpha - 1);
//                double beta_b = 1 - (alpha - alpha_b) / (3 * alpha - 1);
//                double beta = Math.max(Math.max(beta_r, beta_g), beta_b);
//                double gama_r = (alpha_r - minalpha) / (1 - 3 * minalpha);
//                double gama_g = (alpha_g - minalpha) / (1 - 3 * minalpha);
//                double gama_b = (alpha_b - minalpha) / (1 - 3 * minalpha);
//                double gama = Math.max(Math.max(gama_r, gama_g), gama_b);
//
//                double temp = (gama * (R + G + B) - MaxC) / (3 * gama - 1);
//
//                double[] data = new double[3];
//                data[0] = B - (temp + 0.5);
//                data[1] = G - (temp + 0.5);
//                data[2] = R - (temp + 0.5);
//                dst.put(i, j, data);
//            }
//        }
//        return dst;
//    }
//
//}
