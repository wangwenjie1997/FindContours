import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

//获得正的最小外围矩形
public class ContourArea {

    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    static String imgPath="F:\\opencvPhoto\\photo\\1.jpg";
    static String savePath="F:\\opencvPhoto\\result\\";

    public static void main(String[] args) {
        long startTime =  System.currentTimeMillis();
        //读入图片
        Mat src= Imgcodecs.imread(imgPath);

        Mat temp=src.clone();//在此Mat中做标记

        //灰度化
        Mat gray=new Mat();
        Imgproc.cvtColor(src,gray,Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur( gray, gray,new Size(3,3));
        Imgcodecs.imwrite(savePath+"gray.jpg",gray);

        //二值化
        Mat threshold=new Mat();
        Imgproc.threshold(gray,threshold,100,255,Imgproc.THRESH_BINARY);
        Imgcodecs.imwrite(savePath+"threshold.jpg",threshold);

        //轮廓查找
        List<MatOfPoint> contours=new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(threshold,contours,hierarchy,Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
        System.out.println("轮廓数量："+contours.size());
        System.out.println("轮廓类型："+hierarchy);
        //画出所有的边界
        Imgproc.drawContours(temp,contours,-1,new Scalar(0,0,255),3);
        Imgcodecs.imwrite(savePath+"allContour.jpg",temp);


        //寻找财报和财报的表格(思路：财报的连通区域面积为最大，财报中的表格连通区域为次大)
        double maxArea=0;
        MatOfPoint paper=contours.get(0);//财报
        MatOfPoint table=contours.get(0);//财报中的表格
        for(int i=0;i<contours.size();i++){
            MatOfPoint2f matOfPoint2f=new MatOfPoint2f();
            double contourarea=Imgproc.contourArea(contours.get(i));
            if(contourarea>maxArea){
                maxArea=contourarea;
                table=paper;
                paper=contours.get(i);
            }
        }
        //获得财报和财报表格的最小正的外接矩形
        Rect paperRect=Imgproc.boundingRect(paper);
        Rect tableRect=Imgproc.boundingRect(table);

        //画出财报和财报表格的边界
        Imgproc.rectangle(temp,paperRect,new Scalar(0,255,0),9);
        Imgproc.rectangle(temp,tableRect,new Scalar(255,0,0),1);
        Imgcodecs.imwrite(savePath+"paperAndTableContour.jpg",temp);

        //裁剪出财报和财报表格
        Mat paperRect_roi=new Mat(src,paperRect);
        Mat tablerRect_roi=new Mat(src,tableRect);
        Mat paperRect_roi_result=new Mat();
        Mat tablerRect_roi_result=new Mat();
        paperRect_roi.copyTo(paperRect_roi_result);
        tablerRect_roi.copyTo(tablerRect_roi_result);
        Imgcodecs.imwrite(savePath+"paperRect_roi.jpg",paperRect_roi_result);
        Imgcodecs.imwrite(savePath+"tablerRect_roi.jpg",tablerRect_roi_result);

        long endTime =  System.currentTimeMillis();
        long usedTime = (endTime-startTime)/1000;
        System.out.println("所用时间："+usedTime);
    }

}
