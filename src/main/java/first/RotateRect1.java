package first;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;

public class RotateRect1 {

    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    static String imgPath="F:\\opencvPhoto\\photo\\3.jpg";
    //    static String imgPath="C:\\Users\\Administrator\\Desktop\\photo4\\6-2.jpg";
    static String savePath="F:\\opencvPhoto\\result\\";

    public static void main(String[] args) {
        long startTime =  System.currentTimeMillis();

        Mat src= Imgcodecs.imread(imgPath);

        int length=(int)Math.sqrt(src.cols()*src.cols()+src.rows()*src.rows())+1;
        Mat maxSrc=new Mat(length,length,src.type(),new Scalar(255,255,255));
        Rect rect=new Rect((length-src.cols())/2,(length-src.rows())/2,src.cols(),src.rows());

        Mat roi_img = new Mat(maxSrc,rect);
        src.copyTo(roi_img);

        imwrite(savePath+"maxsrc.jpg",maxSrc);

        src=maxSrc.clone();

        Mat temp= src.clone();

        //灰度化+二值化
        Mat gray=new Mat();
        Imgproc.cvtColor(src,gray,Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur( gray, gray,new Size(3,3));
        imwrite(savePath+"gray.jpg",gray);
        Mat threshold=new Mat();
        Imgproc.threshold(gray,threshold,127,255,Imgproc.THRESH_BINARY);
        imwrite(savePath+"threshold.jpg",threshold);
        System.out.println("二值化通道数="+threshold.channels());

        //轮廓查找
        List<MatOfPoint> contours=new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(threshold,contours,hierarchy,Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
        System.out.println("轮廓数量："+contours.size());
        System.out.println("轮廓类型："+hierarchy);

        //查找最大和次大边框(一般最大边框即为所拍的纸，次大为纸中的表格)
        double maxArea=0;
        int maxContoursNum=0;
        MatOfPoint table=contours.get(0);
        MatOfPoint paper=contours.get(0);
        for(int i=0;i<contours.size();i++){
            MatOfPoint2f matOfPoint2f=new MatOfPoint2f();
            double contourarea=Imgproc.contourArea(contours.get(i));
            if(contourarea>maxArea){
                maxArea=contourarea;
                table=paper;
                paper=contours.get(i);
                maxContoursNum=i;
            }
        }

        //求最大和次大边框的最小外接矩形
        RotatedRect minPaperRect = Imgproc.minAreaRect(new MatOfPoint2f(paper.toArray()));
        RotatedRect minTableRect = Imgproc.minAreaRect(new MatOfPoint2f(table.toArray()));
        double minPaperRectAnglr=minPaperRect.angle;
        double minTableRectAnglr=minTableRect.angle;
        Point[] points=new Point[4];
        minPaperRect.points(points);
        for (int i = 0; i < 4; i++)
            Imgproc.line(src, points[i], points[(i+1)%4],new Scalar(0,255,255),6);
        minTableRect.points(points);
        for (int i = 0; i < 4; i++)
            Imgproc.line(src, points[i], points[(i+1)%4],new Scalar(255,0,255),6);


        //最大边框的最小外接矩形旋转--start
        System.out.println("Paper最小外接矩形 width"+minPaperRect.size.width+"   height  "+minPaperRect.size.height+"  angle"+minPaperRect.angle);
        String paperPath=test(minPaperRect,"minPaperRect",temp);
        //最大边框的最小外接矩形旋转--end



        //次大边框的最小外接矩形旋转--start
        System.out.println("Table最小外接矩形 width"+minTableRect.size.width+"   height  "+minTableRect.size.height+"  angle"+minTableRect.angle);
        String tablePath=test(minTableRect,"minTableRect",imread(paperPath));
        //次大边框的最小外接矩形旋转--end


        /*
        Point minTableCenter = minTableRect.center;//外接矩形中心点坐标
        System.out.println(minTableRect.angle+"-----------");
        double minTableAngle = minTableRect.angle;
        int minTableRectWidht=(int)minTableRect.size.width;
        int minTableRectHeight=(int)minTableRect.size.height;
        if(minTableAngle<=-45) {
            minTableAngle += 90;
            minTableRectWidht=minTableRectWidht^minTableRectHeight;
            minTableRectHeight=minTableRectWidht^minTableRectHeight;
            minTableRectWidht=minTableRectWidht^minTableRectHeight;
        }
        else{
            minTableAngle=Math.abs(minTableAngle);
        }
        Mat rot_mat = Imgproc.getRotationMatrix2D(minTableCenter, minTableAngle, 1.0);//求旋转矩阵

        Mat rot_image=new Mat();

        Imgproc.warpAffine(temp, rot_image, rot_mat, src.size());//原图像旋转

        Rect rect=new Rect((int)(minTableCenter.x - (minTableRectWidht / 2)),(int)(minTableCenter.y - (minTableRectHeight/2)),minTableRectWidht,minTableRectHeight);
        Imgproc.rectangle(src,rect,new Scalar(255,255,0),3);
        Imgcodecs.imwrite(savePath+"rot_image.jpg",rot_image);
        Mat minTableRect_roi=new Mat(rot_image,rect);
        Mat minTableRect_roi_result=new Mat();
        minTableRect_roi.copyTo(minTableRect_roi_result);
        Imgcodecs.imwrite(savePath+"minTableRect_roi_result.jpg",minTableRect_roi_result);
        */
        //次大边框的最小外接矩形旋转--end

        List<MatOfPoint> maxContour=new ArrayList<MatOfPoint>();
        maxContour.add(contours.get(maxContoursNum));
//        Imgproc.drawContours(src,maxContour,-1,new Scalar(0,0,255),3);
        Imgproc.drawContours(src,contours,-1,new Scalar(0,0,255),3);
        imwrite(savePath+"all.jpg",src);

        long endTime =  System.currentTimeMillis();
        long usedTime = (endTime-startTime)/1000;
        System.out.println("所用时间："+usedTime);

    }

    public static String test(RotatedRect minTableRect,String imgName,Mat src){
        System.out.println(imgName+"--------------------------------------------------");

        System.out.println("原图像 width"+src.cols()+"   height  "+src.rows());
        Mat temp= src.clone();
        Point minTableCenter = minTableRect.center;//外接矩形中心点坐标

        double minTableAngle = minTableRect.angle;

        int minTableRectWidht=(int)minTableRect.size.width;
        int minTableRectHeight=(int)minTableRect.size.height;

        Mat rot_image=new Mat();
        if(minTableAngle<=-45){
//            rot_image=temp.clone();
//            if(minTableAngle==-90){
            minTableRectWidht=minTableRectWidht^minTableRectHeight;
            minTableRectHeight=minTableRectWidht^minTableRectHeight;
            minTableRectWidht=minTableRectWidht^minTableRectHeight;
            minTableAngle+=90;
//            }

        }
//        else{
        Mat rot_mat = Imgproc.getRotationMatrix2D(minTableCenter, minTableAngle, 1.0);//求旋转矩阵
        Imgproc.warpAffine(temp, rot_image, rot_mat, src.size());//原图像旋转
//        }

        System.out.println("需要旋转的角度:"+minTableAngle);
        System.out.println("旋转后得到的图片 width"+rot_image.cols()+"   height  "+rot_image.rows());

//        if(minTableCenter.x<minTableRectWidht / 2||minTableCenter.y <minTableRectHeight/2){
//            minTableRectWidht=minTableRectWidht^minTableRectHeight;
//            minTableRectHeight=minTableRectWidht^minTableRectHeight;
//            minTableRectWidht=minTableRectWidht^minTableRectHeight;
//        }

        Rect rect=new Rect((int)(minTableCenter.x - (minTableRectWidht / 2)),(int)(minTableCenter.y - (minTableRectHeight/2)),minTableRectWidht,minTableRectHeight);
        System.out.println(rect);
        System.out.println(minTableCenter);
        System.out.println(minTableRectWidht+"   "+minTableRectHeight);
        Imgproc.rectangle(temp,rect,new Scalar(255,255,0),3);
        Imgcodecs.imwrite(savePath+imgName+".jpg",rot_image);//旋转后结果
        Mat minTableRect_roi=new Mat(rot_image,rect);
        Mat minTableRect_roi_result=new Mat();
        minTableRect_roi.copyTo(minTableRect_roi_result);
        Imgcodecs.imwrite(savePath+imgName+"_result.jpg",minTableRect_roi_result);//旋转裁剪后结果
        return savePath+imgName+"_result.jpg";
    }


}
