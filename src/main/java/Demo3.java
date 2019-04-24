import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.opencv.highgui.HighGui.imshow;
import static org.opencv.highgui.HighGui.waitKey;

public class Demo3 {

    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    static String imgPath="F:\\opencvPhoto\\photo\\3.jpg";
    static String savePath="F:\\opencvPhoto\\result\\";

    public static void main(String[] args) {
        //灰度化+二值化
        Mat src= Imgcodecs.imread(imgPath);
        Mat gray=new Mat();
        Imgproc.cvtColor(src,gray,Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur( gray, gray,new Size(3,3));
        Imgcodecs.imwrite(savePath+"gray.jpg",gray);
        Mat threshold=new Mat();
        Imgproc.threshold(gray,threshold,127,255,Imgproc.THRESH_BINARY);
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
        Imgproc.drawContours(src,contours,-1,new Scalar(0,0,255),3);

        double maxArea=0;
        MatOfPoint table=contours.get(0);
        MatOfPoint paper=contours.get(0);
        int tableNum=0;
        int paperNum=0;

        for(int i=0;i<contours.size();i++){
            Scalar scalar=new Scalar(new Random().nextInt(255-0)+0,new Random().nextInt(255-0)+0,new Random().nextInt(255-0)+0);

            double contourarea=Imgproc.contourArea(contours.get(i));
            if(contourarea>maxArea){
                maxArea=contourarea;
                table=paper;
                paper=contours.get(i);

                tableNum=paperNum;
                paperNum=i;
            }
        }

        Imgproc.drawContours(src,contours,paperNum,new Scalar(0,255,0),3);
        Imgproc.drawContours(src,contours,tableNum,new Scalar(255,0,0),3);

//        Rect paperRect=Imgproc.boundingRect(paper);
//        Rect tablerRect=Imgproc.boundingRect(paper);

//        Imgproc.rectangle(src,paperRect,new Scalar(255,0,0),3);
//        Imgproc.rectangle(src,tablerRect,new Scalar(0,0,255),3);


        Imgcodecs.imwrite(savePath+"xx.jpg",src);

    }


}
