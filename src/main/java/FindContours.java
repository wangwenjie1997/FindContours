import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;

public class FindContours {

    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
    static String imgPath="F:\\opencvPhoto\\photo\\xdc-3.jpg";
    static String savePath="F:\\opencvPhoto\\result\\";

    public static void main(String[] args) {
        Mat src= Imgcodecs.imread(imgPath);
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
        Imgproc.findContours(threshold,contours,hierarchy,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);
        System.out.println("轮廓数量："+contours.size());
        System.out.println("轮廓类型："+hierarchy);

        Imgproc.drawContours(src,contours,-1,new Scalar(0,0,255),1);
        imwrite(savePath+"xxxxx.jpg",src);

        int max=0;
        int two=0;

        for(int i=0;i<hierarchy.cols();i++){
            if(hierarchy.get(0,i)[3]==-1){
                max++;
            }
            if(hierarchy.get(0,i)[3]==0){
                two++;
            }
//            if(hierarchy.get(0,i)[3]==2){
                List<MatOfPoint> contour=new ArrayList<MatOfPoint>();
                contour.add(contours.get(i));
                Imgproc.drawContours(src,contour,-1,new Scalar(new Random().nextInt(255 -0 + 1) +0,new Random().nextInt(255 -0 + 1) +0,new Random().nextInt(255 -0 + 1) +0),3);
                Rect rect=Imgproc.boundingRect(contours.get(i));
                Mat Roi=new Mat(src,rect);
                imwrite(savePath+i+".jpg",Roi);
//            }
        }

        System.out.println("最大"+max+"    次大"+two);

        imwrite(savePath+"src.jpg",src);

        for(int k=0;k<hierarchy.cols();k++) {
            System.out.print("轮廓下标："+k +" { ");
            double[] ds = hierarchy.get(0, k);
            for (int l=0;l<ds.length;l++) {
                switch (l) {
                    case 0:
                        System.out.print(" 后一个轮廓下标："+ ds[l]);
                        break;
                    case 1:
                        System.out.print("  前一个轮廓下标："+ds[l]);
                        break;
                    case 2:
                        System.out.print("  子轮廓下标："+ds[l]);
                        break;
                    case 3:
                        System.out.print("  父轮廓下标："+ds[l]);
                        break;

                    default:
                        break;
                }
            }
            System.out.print(" }\n");
        }

    }

}
