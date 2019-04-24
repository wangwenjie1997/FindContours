package test;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Test1 {

    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    static String imgPath="F:\\opencvPhoto\\photo\\1.jpg";
    static String savePath="F:\\opencvPhoto\\result\\";

    public static void main(String[] args) {
        Point[] points=new Point[4];
        long startTime =  System.currentTimeMillis();
        //读入图片
        Mat src= Imgcodecs.imread(imgPath);
        //灰度化
        Mat gray=toGray(src);
        //二值化
        Mat threshold=toThreshold(gray);

        //轮廓查找
        Rect paperRect=findContours(threshold);
        points[0]=new Point(paperRect.x,paperRect.y);
        points[1]=new Point(paperRect.x+paperRect.width,paperRect.y);
        points[2]=new Point(paperRect.x+paperRect.width,paperRect.y+paperRect.height);
        points[3]=new Point(paperRect.x,paperRect.y+paperRect.height);
        long getPaperEndTime =  System.currentTimeMillis();
        long getPaperUsedTime = (getPaperEndTime-startTime)/1000;
        System.out.println("得到表单所用时间："+getPaperUsedTime);

        //画出财报边界
        Mat temp=src.clone();
        for (int i = 0; i < 4; i++)
            Imgproc.line(temp, points[i], points[(i+1)%4],new Scalar(0,255,0),5);
        Imgcodecs.imwrite(savePath+"PaperContour.jpg",temp);

        //裁剪出财报和财报表格
        Mat paperRect_roi=new Mat(src,paperRect);
        Mat paperRect_roi_result=new Mat();
        paperRect_roi.copyTo(paperRect_roi_result);
        Imgcodecs.imwrite(savePath+"paperRect_roi.jpg",paperRect_roi_result);

        long endTime =  System.currentTimeMillis();
        long usedTime = (endTime-startTime)/1000;
        System.out.println("所用时间："+usedTime);
    }

    //灰度化
    public static Mat toGray(Mat src){
        Mat gray=new Mat();
        Imgproc.cvtColor(src,gray,Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur( gray, gray,new Size(3,3));
        return gray;
    }
    //二值化
    public static Mat toThreshold(Mat src){
        Mat threshold=new Mat();
        Imgproc.threshold(src,threshold,100,255,Imgproc.THRESH_BINARY);
        return threshold;
    }
    //轮廓查找
    public static Rect findContours(Mat src){
        List<MatOfPoint> contours=new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(src,contours,hierarchy,Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);

        //寻找财报(思路：财报的连通区域面积为最大)
        double maxArea=0;
        MatOfPoint paper=contours.get(0);//财报
        for(int i=0;i<contours.size();i++){
            MatOfPoint2f matOfPoint2f=new MatOfPoint2f();
            double contourarea=Imgproc.contourArea(contours.get(i));
            if(contourarea>maxArea){
                maxArea=contourarea;
                paper=contours.get(i);
            }
        }
        //获得财报和财报表格的最小正的外接矩形
        Rect paperRect=Imgproc.boundingRect(paper);

        return paperRect;

    }

}
