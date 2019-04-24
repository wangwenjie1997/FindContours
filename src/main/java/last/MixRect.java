package last;

import org.opencv.core.*;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;

public class MixRect {

    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    static String imgPath="F:\\opencvPhoto\\photo\\test4.jpg";
    static String savePath="F:\\opencvPhoto\\result\\";

    public static void main(String[] args) {
        long startTime =  System.currentTimeMillis();

        //读入图片
        Mat src= Imgcodecs.imread(imgPath);
        //表单边框裁剪
        Mat paperMat=new Mat();
        paperMat=getPaperMat(src);
        long getPaperEndTime =  System.currentTimeMillis();
        long getPaperUsedTime = (getPaperEndTime-startTime)/1000;
        System.out.println("得到表单所用时间："+getPaperUsedTime);
        Imgcodecs.imwrite(savePath+"paperMat.jpg",paperMat);



        //表格边框裁剪
        Mat tableMat=new Mat();
//        tableMat=getTableMat(paperMat);
        tableMat=getTableMatTwo(paperMat);
        Imgcodecs.imwrite(savePath+"tableMat.jpg",tableMat);

        long endTime =  System.currentTimeMillis();
        long usedTime = (endTime-startTime)/1000;
        System.out.println("所用时间："+usedTime);
    }

    //得到报表
    public static Mat getPaperMat(Mat src){

        Mat safeSrc=getSafeMat(src);
        imwrite(savePath+"safePaperSrc.jpg",safeSrc);
        Mat gray=toGray(safeSrc);
        Mat threshold=toThreshold(gray);

        Mat temp=safeSrc.clone();

        List<MatOfPoint> contours=findContours(threshold,safeSrc);
        Imgproc.drawContours(temp,contours,-1,new Scalar(0,0,255),3);
        Imgcodecs.imwrite(savePath+"AllPaperContour.jpg",temp);

        double maxArea=0;
        MatOfPoint paper=contours.get(0);
        for(int i=0;i<contours.size();i++){
            MatOfPoint2f matOfPoint2f=new MatOfPoint2f();
            double contourarea=Imgproc.contourArea(contours.get(i));
            if(contourarea>maxArea){
                maxArea=contourarea;
                paper=contours.get(i);
            }
        }

        RotatedRect minPaperRect = Imgproc.minAreaRect(new MatOfPoint2f(paper.toArray()));

        Point[] points=new Point[4];
        minPaperRect.points(points);
        for (int i = 0; i < 4; i++){
            Imgproc.line(temp, points[i], points[(i+1)%4],new Scalar(0,255,0),5);
        }

        Imgcodecs.imwrite(savePath+"PaperContour.jpg",temp);

        Mat paperMat=getRectImg(minPaperRect,safeSrc);
        return paperMat;
    }

    //得到表单（RotateRect）
    public static Mat getTableMat(Mat src){

        Mat safeSrc=getSafeMat(src);
        imwrite(savePath+"safeTableSrc.jpg",safeSrc);
        Mat gray=toGray(safeSrc);
        Mat threshold=toThreshold(gray);

        Mat temp=safeSrc.clone();

        List<MatOfPoint> contours=findContours(threshold,safeSrc);
        Imgproc.drawContours(temp,contours,-1,new Scalar(0,0,255),3);
        Imgcodecs.imwrite(savePath+"AllTableContour.jpg",temp);

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

        RotatedRect minTableRect = Imgproc.minAreaRect(new MatOfPoint2f(table.toArray()));
        Point[] points=new Point[4];
        minTableRect.points(points);
        for (int i = 0; i < 4; i++)
            Imgproc.line(temp, points[i], points[(i+1)%4],new Scalar(255,0,0),5);
        Imgcodecs.imwrite(savePath+"TableContour.jpg",temp);

        Mat paperMat=getRectImg(minTableRect,safeSrc);
        return paperMat;
    }

    //得到表单（Rect）
    public static Mat getTableMatTwo(Mat src){

        Mat gray=toGray(src);
        Mat threshold=toThreshold(gray);

        Mat temp=src.clone();

        List<MatOfPoint> contours=findContours(threshold,src);
        Imgproc.drawContours(temp,contours,-1,new Scalar(0,0,255),3);
        Imgcodecs.imwrite(savePath+"AllTableContour.jpg",temp);

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

        Rect tableRect=Imgproc.boundingRect(table);

        Imgproc.rectangle(temp,tableRect,new Scalar(255,0,0),3);
        Imgcodecs.imwrite(savePath+"TableContour.jpg",temp);

        Mat tablerRect_roi=new Mat(src,tableRect);
        Mat tablerRect_roi_result=new Mat();
        tablerRect_roi.copyTo(tablerRect_roi_result);

        return tablerRect_roi_result;
    }

    //防止图片旋转后导致信息丢失
    public static Mat getSafeMat(Mat src){
        int length=(int)Math.sqrt(src.cols()*src.cols()+src.rows()*src.rows())+1;
        Mat maxSrc=new Mat(length,length,src.type(),new Scalar(0,0,0));
        org.opencv.core.Rect rect=new org.opencv.core.Rect((length-src.cols())/2,(length-src.rows())/2,src.cols(),src.rows());
        Mat roi_img = new Mat(maxSrc,rect);
        src.copyTo(roi_img);
        return maxSrc;
    }

    //灰度化
    public static Mat toGray(Mat src){
        Mat gray=new Mat();
        Imgproc.cvtColor(src,gray,Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur( gray, gray,new Size(3,3));
        imwrite(savePath+"gray.jpg",gray);
        return gray;
    }

    //二值化
    public static Mat toThreshold(Mat src){
        Mat threshold=new Mat();
        Imgproc.threshold(src,threshold,127,255,Imgproc.THRESH_BINARY);
        imwrite(savePath+"threshold.jpg",threshold);
        return threshold;
    }

    //寻找轮廓
    public static List<MatOfPoint> findContours(Mat src,Mat safeSrc){
        Mat temp=safeSrc.clone();

        List<MatOfPoint> contours=new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(src,contours,hierarchy,Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
        System.out.println("轮廓数量："+contours.size());
        System.out.println("轮廓类型："+hierarchy);
        return contours;
    }

    //旋转+裁剪图片
    public static Mat getRectImg(RotatedRect rotatedRect,Mat src){

        Point rotatedRectCenter = rotatedRect.center;//外接矩形中心点坐标
        double rotatedRectAngle = rotatedRect.angle;
        int rotatedRectWidht=(int)rotatedRect.size.width;
        int rotatedRectHeight=(int)rotatedRect.size.height;

        System.out.println("Rect角度:"+rotatedRectAngle);

        //角度大于等于45度图片逆时针旋转90-rotatedRectAngle度并宽高互换（参考链接：https://blog.csdn.net/mailzst1/article/details/83141632）
        //角度负为顺时针，角度正为逆时针
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
