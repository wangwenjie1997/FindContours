package last;

import org.opencv.core.*;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;
//最小外围矩形
public class RotateRect {

    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    static String imgPath="F:\\opencvPhoto\\photo\\3.jpg";
    static String savePath="F:\\opencvPhoto\\result\\";

    public static void main(String[] args) {
        long startTime =  System.currentTimeMillis();

        //读入图片
        Mat src= Imgcodecs.imread(imgPath);

        //改变图片大小，使得图片旋转时不会出现信息丢失
        int length=(int)Math.sqrt(src.cols()*src.cols()+src.rows()*src.rows())+1;
        Mat maxSrc=new Mat(length,length,src.type(),new Scalar(0,0,0));
        org.opencv.core.Rect rect=new org.opencv.core.Rect((length-src.cols())/2,(length-src.rows())/2,src.cols(),src.rows());
        Mat roi_img = new Mat(maxSrc,rect);
        src.copyTo(roi_img);
        imwrite(savePath+"maxsrc.jpg",maxSrc);

        Mat temp= maxSrc.clone();//在此Mat中做标记

        //灰度化
        Mat gray=new Mat();
        Imgproc.cvtColor(maxSrc,gray,Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur( gray, gray,new Size(3,3));
        imwrite(savePath+"gray.jpg",gray);

        //二值化
        Mat threshold=new Mat();
        Imgproc.threshold(gray,threshold,127,255,Imgproc.THRESH_BINARY);
        imwrite(savePath+"threshold.jpg",threshold);

        //轮廓查找
        List<MatOfPoint> contours=new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(threshold,contours,hierarchy,Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
        System.out.println("轮廓数量："+contours.size());
        System.out.println("轮廓类型："+hierarchy);
        //画出所有的边界
        Imgproc.drawContours(temp,contours,-1,new Scalar(0,0,255),3);
        Imgcodecs.imwrite(savePath+"allContour.jpg",temp);

        //查找最大和次大边框(一般最大边框即为所拍的纸，次大为纸中的表格)
        double maxArea=0;
        MatOfPoint table=contours.get(0);
        MatOfPoint paper=contours.get(0);
        for(int i=0;i<contours.size();i++){
            MatOfPoint2f matOfPoint2f=new MatOfPoint2f();
            double contourarea=Imgproc.contourArea(contours.get(i));
            if(contourarea>maxArea){
                maxArea=contourarea;
                table=paper;
                paper=contours.get(i);
            }
        }

        //求最大和次大边框的最小外接矩形
        RotatedRect minPaperRect = Imgproc.minAreaRect(new MatOfPoint2f(paper.toArray()));
        RotatedRect minTableRect = Imgproc.minAreaRect(new MatOfPoint2f(table.toArray()));
        //画出财报和财报表格的边界
        Point[] points=new Point[4];
        minPaperRect.points(points);
        for (int i = 0; i < 4; i++)
            Imgproc.line(temp, points[i], points[(i+1)%4],new Scalar(0,255,0),5);
        minTableRect.points(points);
        for (int i = 0; i < 4; i++)
            Imgproc.line(temp, points[i], points[(i+1)%4],new Scalar(255,0,0),5);
        Imgcodecs.imwrite(savePath+"paperAndTableContour.jpg",temp);


        //最大边框的最小外接矩形旋转
        Mat paperResulrt=test(minPaperRect,maxSrc);
        Imgcodecs.imwrite(savePath+"paperResulrt.jpg",paperResulrt);

        //次大边框的最小外接矩形旋转
        Mat tableResulrt=test(minTableRect,maxSrc);
        Imgcodecs.imwrite(savePath+"tableResulrt.jpg",paperResulrt);

        long endTime =  System.currentTimeMillis();
        long usedTime = (endTime-startTime)/1000;
        System.out.println("所用时间："+usedTime);

    }

    public static Mat test(RotatedRect rotatedRect,Mat src){

        Point rotatedRectCenter = rotatedRect.center;//外接矩形中心点坐标
        double rotatedRectAngle = rotatedRect.angle;
        int rotatedRectWidht=(int)rotatedRect.size.width;
        int rotatedRectHeight=(int)rotatedRect.size.height;

        System.out.println("Rect角度:"+rotatedRectAngle);

        //角度大于等于45度图片逆时针旋转90-rotatedRectAngle度并宽高互换（参考链接：https://blog.csdn.net/mailzst1/article/details/83141632）
        if(rotatedRectAngle<=-45){
            rotatedRectWidht=rotatedRectWidht^rotatedRectHeight;
            rotatedRectHeight=rotatedRectWidht^rotatedRectHeight;
            rotatedRectWidht=rotatedRectWidht^rotatedRectHeight;
            rotatedRectAngle+=90;
        }

        System.out.println("需要旋转:"+rotatedRectAngle);

        //图片旋转
        Mat rot_image=new Mat();
        Mat rot_mat = Imgproc.getRotationMatrix2D(rotatedRectCenter, rotatedRectAngle, 1.0);//求旋转矩阵
        Imgproc.warpAffine(src, rot_image, rot_mat, src.size());//原图像旋转

        //旋转后的Rect
        org.opencv.core.Rect rect=new Rect((int)(rotatedRectCenter.x - (rotatedRectWidht / 2)),(int)(rotatedRectCenter.y - (rotatedRectHeight/2)),rotatedRectWidht,rotatedRectHeight);
        System.out.println("旋转后Rect:"+rect);

        //画出旋转后的图片
        Mat temp=rot_image.clone();
        Imgproc.rectangle(temp,rect,new Scalar(255,255,0),3);
        Imgcodecs.imwrite(savePath+System.currentTimeMillis()+".jpg",rot_image);//旋转后结果

        //图片裁剪
        Mat rotatedRect_roi=new Mat(rot_image,rect);
        Mat rotatedRect_roi_result=new Mat();
        rotatedRect_roi.copyTo(rotatedRect_roi_result);

        return rotatedRect_roi_result;
    }


}
