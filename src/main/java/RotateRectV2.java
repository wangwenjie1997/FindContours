import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.opencv.highgui.HighGui.imshow;
import static org.opencv.highgui.HighGui.waitKey;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;

//透视还原--失败
public class RotateRectV2 {

    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    static String imgPath="F:\\opencvPhoto\\photo\\2-1.jpg";
    //    static String imgPath="C:\\Users\\Administrator\\Desktop\\photo4\\6-2.jpg";
    static String savePath="F:\\opencvPhoto\\result\\";

    public static void main(String[] args) {
        long startTime =  System.currentTimeMillis();

        Mat src= Imgcodecs.imread(imgPath);


        Mat temp= src.clone();

        //灰度化+二值化
        Mat gray=new Mat();
        Imgproc.cvtColor(src,gray,Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur( gray, gray,new Size(3,3));
        imwrite(savePath+"gray.jpg",gray);
        Mat threshold=new Mat();
        Imgproc.threshold(gray,threshold,127,255,Imgproc.THRESH_BINARY);
        imwrite(savePath+"threshold.jpg",threshold);
        System.out.println("通道数="+threshold.channels());

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
        Mat paperMat=test(minPaperRect,"minPaperRect",threshold);
        //最大边框的最小外接矩形旋转--end



        //次大边框的最小外接矩形旋转--start
        Mat minTable=test(minTableRect,"minTableRect",paperMat);
        //次大边框的最小外接矩形旋转--end

        int minLength=minTable.cols()<minTable.rows()?minTable.cols():minTable.rows();
        System.out.println("通道数"+minTable.channels());
        //寻找顶点需要二值化
        //寻找左上角
        Point leftTop=null;
        for(int i=0;i<minLength;i++){
            if(minTable.get(i,i)[0]!=0)
                continue;
            else{
                leftTop=new Point(i,i);
                break;
            }
        }
        //寻找右上角
        Point rightTop=null;
        for(int i=0;i<minLength;i++){

            if(minTable.get(i,minTable.cols()-1-i)[0]!=0)
                continue;
            else{
                rightTop=new Point(minTable.cols()-1-i,i);
                break;
            }
        }
        //寻找左下角
        Point leftBottom=null;
        for(int i=0;i<minLength;i++){
            if(minTable.get(minTable.rows()-1-i,i)[0]!=0)
                continue;
            else{
                leftBottom=new Point(i,minTable.rows()-1-i);
                break;
            }
        }
        //寻找右下角
        Point rightBottom=null;
        for(int i=0;i<minLength;i++){
            if(minTable.get(minTable.rows()-1-i,minTable.cols()-1-i)[0]!=0)
                continue;
            else{
                rightBottom=new Point(minTable.cols()-1-i,minTable.rows()-1-i);
                break;
            }
        }
        System.out.println("修正前左上"+leftTop);
        System.out.println("修正前右上"+rightTop);
        System.out.println("修正前左下"+leftBottom);
        System.out.println("修正前右下"+rightBottom);
        //顶点修正
        if(leftTop!=null&&rightTop!=null&&leftBottom!=null&&rightBottom!=null){
            System.out.println("找到四个顶点");
            //左上角y坐标修正
            for(int i=(int)leftTop.y;i>=0;i--){
                if(minTable.get(i,(int)leftTop.x)[0]!=0)
                    break;
                else {
                    leftTop=new Point((int)leftTop.x,i);
                }
            }
            //左上角x坐标修正
            for(int i=(int)leftTop.x;i>=0;i--){
                if(minTable.get((int)leftTop.y,i)[0]!=0)
                    break;
                else{
                    leftTop=new Point(i,(int)leftTop.y);
                }
            }

            //右上角y坐标修正
            for(int i=(int)rightTop.y;i>=0;i--){
                if(minTable.get(i,(int)rightTop.x)[0]!=0)
                    break;
                else{
                    rightTop=new Point((int)rightTop.x,i);
                }
            }
            //右上角x坐标修正
            for(int i=(int)rightTop.x;i<minTable.cols();i++){
                if(minTable.get((int)rightTop.y,i)[0]!=0)
                    break;
                else{
                    rightTop=new Point(i,(int)rightTop.y);
                }
            }

            //左下角y坐标修正
            for(int i=(int)leftBottom.y;i<minTable.rows();i++){
                if(minTable.get(i,(int)leftBottom.x)[0]!=0)
                    break;
                else{
                    leftBottom=new Point((int)leftBottom.x,i);
                }
            }
            //左下角x坐标修正
            for(int i=(int)leftBottom.x;i>=0;i--){
                if(minTable.get((int)leftBottom.y,i)[0]!=0)
                    break;
                else{
                    leftBottom=new Point(i,(int)leftBottom.y);
                }
            }

            //右下角y坐标修正
            for(int i=(int)rightBottom.y;i<minTable.rows();i++){
                if(minTable.get(i,(int)rightBottom.x)[0]!=0)
                    break;
                else{
                    rightBottom=new Point((int)rightBottom.x,i);
                }
            }
            //右下角x坐标修正
            for(int i=(int)rightBottom.x;i<minTable.cols();i++){
                if(minTable.get((int)rightBottom.y,i)[0]!=0)
                    break;
                else{
                    rightBottom=new Point(i,(int)rightBottom.y);
                }
            }
        }

        System.out.println("修正后左上"+leftTop);
        System.out.println("修正后右上"+rightTop);
        System.out.println("修正后左下"+leftBottom);
        System.out.println("修正后右下"+rightBottom);


        Imgproc.circle(minTable,leftTop,5,new Scalar(0,0,0),-1);
        Imgproc.circle(minTable,rightTop,5,new Scalar(0,0,0),-1);
        Imgproc.circle(minTable,leftBottom,5,new Scalar(0,0,0),-1);
        Imgproc.circle(minTable,rightBottom,5,new Scalar(0,0,0),-1);


        // 点的顺序[左上 ，右上 ，右下 ，左下]
        List<Point> listSrcs = Arrays.asList(leftTop, rightTop, rightBottom, leftBottom);
        Mat srcPoints = Converters.vector_Point_to_Mat(listSrcs, CvType.CV_32F);

        List<Point> listDsts = Arrays.asList(new Point(0,0), new Point(minTable.cols(),0), new Point(minTable.cols(),minTable.rows()), new Point(0,minTable.rows()));
        Mat dstPoints = Converters.vector_Point_to_Mat(listDsts, CvType.CV_32F);


        Mat perspectiveMmat=Imgproc.getPerspectiveTransform(srcPoints,dstPoints);

        Mat dst = new Mat();

        Imgproc.warpPerspective(minTable, dst, perspectiveMmat, minTable.size(), Imgproc.INTER_LINEAR + Imgproc.WARP_INVERSE_MAP,
                1, new Scalar(0));
        imwrite(savePath+"last.jpg",dst);


        List<MatOfPoint> maxContour=new ArrayList<MatOfPoint>();
        maxContour.add(contours.get(maxContoursNum));
//        Imgproc.drawContours(src,maxContour,-1,new Scalar(0,0,255),3);
        Imgproc.drawContours(src,contours,-1,new Scalar(0,0,255),3);
        imwrite(savePath+"all.jpg",src);

        long endTime =  System.currentTimeMillis();
        long usedTime = (endTime-startTime)/1000;
        System.out.println("所用时间："+usedTime);

    }

    public static Mat test(RotatedRect minTableRect,String imgName,Mat src){

        Mat temp= src.clone();
        Point minTableCenter = minTableRect.center;//外接矩形中心点坐标

        double minTableAngle = minTableRect.angle;

        int minTableRectWidht=(int)minTableRect.size.width;
        int minTableRectHeight=(int)minTableRect.size.height;

        Mat rot_image=new Mat();
        if(minTableAngle<=-45){

            minTableRectWidht=minTableRectWidht^minTableRectHeight;
            minTableRectHeight=minTableRectWidht^minTableRectHeight;
            minTableRectWidht=minTableRectWidht^minTableRectHeight;
            minTableAngle+=90;
        }

        Mat rot_mat = Imgproc.getRotationMatrix2D(minTableCenter, minTableAngle, 1.0);//求旋转矩阵
        Imgproc.warpAffine(temp, rot_image, rot_mat, src.size());//原图像旋转

        System.out.println("需要旋转的角度:"+minTableAngle);
        System.out.println("旋转后得到的图片 width"+rot_image.cols()+"   height  "+rot_image.rows());

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
        return minTableRect_roi_result;
//        return savePath+imgName+"_result.jpg";
    }


}
