package first;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Rect1 {

    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    static String imgPath="F:\\opencvPhoto\\photo\\7.jpg";
    static String savePath="F:\\opencvPhoto\\result\\";

    public static void main(String[] args) {
        long startTime =  System.currentTimeMillis();

        //灰度化+二值化
        Mat src= Imgcodecs.imread(imgPath);
        Mat temp= src.clone();
        Mat gray=new Mat();
        Imgproc.cvtColor(src,gray,Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur( gray, gray,new Size(3,3));
        Imgcodecs.imwrite(savePath+"gray.jpg",gray);
        Mat threshold=new Mat();
        Imgproc.threshold(gray,threshold,100,255,Imgproc.THRESH_BINARY);
        Imgcodecs.imwrite(savePath+"threshold.jpg",threshold);
        System.out.println("通道数="+threshold.channels());
        //黑白转换
//        int num_rows = threshold.rows();
//        int num_col = threshold.cols();
//        for (int i = 0; i < num_rows; i++) {
//            for (int j = 0; j < num_col; j++) {
//                double[] pixel=threshold.get(i,j);
//                for(int k=0;k<pixel.length;k++)
//                    pixel[k]=Math.abs(255-pixel[k]);
//                threshold.put(i,j,pixel);
//            }
//        }
        //显示二值化+黑白转换结果
//        imshow("threshold",threshold);
//        waitKey(0);

        //轮廓查找
        List<MatOfPoint> contours=new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(threshold,contours,hierarchy,Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
        System.out.println("轮廓数量："+contours.size());
        System.out.println("轮廓类型："+hierarchy);


        double maxArea=0;
        int maxContoursNum=0;
        MatOfPoint table=contours.get(0);
        MatOfPoint paper=contours.get(0);
        for(int i=0;i<contours.size();i++){
            MatOfPoint2f matOfPoint2f=new MatOfPoint2f();

//            Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), matOfPoint2f, 3, true);
//            double contourarea=Imgproc.contourArea(matOfPoint2f);
            double contourarea=Imgproc.contourArea(contours.get(i));

            if(contourarea>maxArea){
                maxArea=contourarea;
                table=paper;
                maxContoursNum=i;

                paper=contours.get(i);
//                paper=new MatOfPoint(matOfPoint2f.toArray());
            }
        }

        int max=src.cols()>src.rows()?src.cols()+1:src.rows()+1;
        Point top=new Point(-1,max);
        Point bottom=new Point(-1,-1);
        Point left=new Point(max,-1);
        Point right=new Point(-1,-1);
        Point[] tablePoints=table.toArray();
        for(int i=0;i<tablePoints.length;i++){
            Point tempPoint=tablePoints[i];
            if(tempPoint.y<top.y)
                top=tempPoint;
            if(tempPoint.y>bottom.y)
                bottom=tempPoint;
            if(tempPoint.x<left.x)
                left=tempPoint;
            if(tempPoint.x>right.x)
                right=tempPoint;
        }
        System.out.println(top);
        System.out.println(bottom);
        System.out.println(left);
        System.out.println(right);


        org.opencv.core.Rect paperRect=Imgproc.boundingRect(paper);
        org.opencv.core.Rect tableRect=Imgproc.boundingRect(table);

        Imgproc.rectangle(src,paperRect,new Scalar(255,0,0),9);
        Imgproc.rectangle(src,tableRect,new Scalar(0,255,0),1);

        Mat paperRect_roi=new Mat(temp,paperRect);
        Mat tablerRect_roi=new Mat(temp,tableRect);
        Mat paperRect_roi_result=new Mat();
        Mat tablerRect_roi_result=new Mat();
        paperRect_roi.copyTo(paperRect_roi_result);
        tablerRect_roi.copyTo(tablerRect_roi_result);
        Imgcodecs.imwrite(savePath+"paperRect_roi.jpg",paperRect_roi_result);
        Imgcodecs.imwrite(savePath+"tablerRect_roi.jpg",tablerRect_roi_result);


//        RotatedRect minPaperRect = Imgproc.minAreaRect(new MatOfPoint2f(paper.toArray()));
//        RotatedRect minTableRect = Imgproc.minAreaRect(new MatOfPoint2f(table.toArray()));
//        System.out.println(minPaperRect.angle);
//        System.out.println(minTableRect.angle);
//        Point[] points=new Point[4];
//        minPaperRect.points(points);
//        for (int i = 0; i < 4; i++)
//            Imgproc.line(src, points[i], points[(i+1)%4],new Scalar(0,255,255),6);
//        minTableRect.points(points);
//        for (int i = 0; i < 4; i++)
//            Imgproc.line(src, points[i], points[(i+1)%4],new Scalar(255,0,255),6);

        List<MatOfPoint> maxContour=new ArrayList<MatOfPoint>();
        maxContour.add(contours.get(maxContoursNum));
        Imgproc.drawContours(src,maxContour,-1,new Scalar(0,0,255),3);
//        Imgproc.drawContours(src,contours,-1,new Scalar(0,0,255),3);
        Imgcodecs.imwrite(savePath+"all.jpg",src);

        long endTime =  System.currentTimeMillis();
        long usedTime = (endTime-startTime)/1000;
        System.out.println("所用时间："+usedTime);

    }

}
