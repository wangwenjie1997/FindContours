package operate;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.highgui.HighGui.imshow;
import static org.opencv.highgui.HighGui.waitKey;
import static org.opencv.imgproc.Imgproc.findContours;

public class ImageOperate {

    public List<Point> getCornersByContour(Mat imgsource){
        List<MatOfPoint> contours=new ArrayList<MatOfPoint>();
        //轮廓检测
        Imgproc.findContours(imgsource,contours,new Mat(),Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);
        double maxArea=-1;
        int maxAreaIdx=-1;
        MatOfPoint temp_contour=contours.get(0);//假设最大的轮廓在index=0处
        MatOfPoint2f approxCurve=new MatOfPoint2f();
        for (int idx=0;idx<contours.size();idx++){
            temp_contour=contours.get(idx);
            double contourarea=Imgproc.contourArea(temp_contour);
            //当前轮廓面积比最大的区域面积大就检测是否为四边形
            if (contourarea>maxArea){
                //检测contour是否是四边形
                MatOfPoint2f new_mat=new MatOfPoint2f(temp_contour.toArray());
                int contourSize= (int) temp_contour.total();
                System.out.println("当前轮廓点数"+contourSize);
                MatOfPoint2f approxCurve_temp=new MatOfPoint2f();
                //对图像轮廓点进行多边形拟合
                Imgproc.approxPolyDP(new_mat,approxCurve_temp,contourSize*0.05,true);
                System.out.println("多边形拟合"+approxCurve_temp.total());
                if (approxCurve_temp.total()==4){
                    maxArea=contourarea;
                    maxAreaIdx=idx;
                    approxCurve=approxCurve_temp;
                }
            }
        }

        Point[] points=approxCurve.toArray();
        for (int i=0;i<points.length;i++){
            System.out.println("--"+points[i]);
        }


        double[] temp_double=approxCurve.get(0,0);
        System.out.println(temp_double[0]+"  "+temp_double[1]);
        Point point1=new Point(temp_double[0],temp_double[1]);

        temp_double=approxCurve.get(1,0);
        System.out.println(temp_double[0]+"  "+temp_double[1]);
        Point point2=new Point(temp_double[0],temp_double[1]);

        temp_double=approxCurve.get(2,0);
        System.out.println(temp_double[0]+"  "+temp_double[1]);
        Point point3=new Point(temp_double[0],temp_double[1]);

        temp_double=approxCurve.get(3,0);
        System.out.println(temp_double[0]+"  "+temp_double[1]);
        Point point4=new Point(temp_double[0],temp_double[1]);

        List<Point> source=new ArrayList<Point>();
        source.add(point1);
        source.add(point2);
        source.add(point3);
        source.add(point4);
        //对4个点进行排序
        Point centerPoint=new Point(0,0);//质心
        for (Point corner:source){
            centerPoint.x+=corner.x;
            centerPoint.y+=corner.y;
        }
        centerPoint.x=centerPoint.x/source.size();
        centerPoint.y=centerPoint.y/source.size();
        Point lefttop=new Point();
        Point righttop=new Point();
        Point leftbottom=new Point();
        Point rightbottom=new Point();
        for (int i=0;i<source.size();i++){
            if (source.get(i).x<centerPoint.x&&source.get(i).y<centerPoint.y){
                lefttop=source.get(i);
            }else if (source.get(i).x>centerPoint.x&&source.get(i).y<centerPoint.y){
                righttop=source.get(i);
            }else if (source.get(i).x<centerPoint.x&& source.get(i).y>centerPoint.y){
                leftbottom=source.get(i);
            }else if (source.get(i).x>centerPoint.x&&source.get(i).y>centerPoint.y){
                rightbottom=source.get(i);
            }
        }
        source.clear();
        source.add(lefttop);
        source.add(righttop);
        source.add(leftbottom);
        source.add(rightbottom);
        return source;
    }

    public void test(String imgpath,String savePath){
        Mat src = Imgcodecs.imread( imgpath);
        Mat src_gray=new Mat();
        Imgproc.cvtColor( src, src_gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur( src_gray, src_gray,new  Size(3,3));

        Mat threshold_output=new Mat();
        List<MatOfPoint> contours=new ArrayList<MatOfPoint>();
        Mat hierarchy=new Mat();
        Imgproc.threshold( src_gray, threshold_output, 1,255, Imgproc.THRESH_BINARY );
        findContours( threshold_output, contours,hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE,new Point(0, 0));

        Imgproc.drawContours( src, contours, -1, new Scalar(0,0,255), 10, 8, hierarchy, 0,new Point());

//        imshow("test",src);
//        waitKey(0);

        Imgcodecs.imwrite(savePath+"x2.jpg",src);
    }


    static void highlightRemove(Mat src, Mat dst) {

        for (int i = 0; i < src.rows(); i++) {

            for (int j = 0; j < src.cols(); j++) {
                double B = src.get(i, j)[0];
                double G = src.get(i, j)[1];
                double R = src.get(i, j)[2];

                double alpha_r = R / (R + G + B);
                double alpha_g = G / (R + G + B);
                double alpha_b = B / (R + G + B);

                double alpha = Math.max(Math.max(alpha_r, alpha_g), alpha_b);
                double MaxC = Math.max(Math.max(R, G), B);
                double minalpha = Math.min(Math.min(alpha_r, alpha_g), alpha_b);
                double beta_r = 1 - (alpha - alpha_r) / (3 * alpha - 1);
                double beta_g = 1 - (alpha - alpha_g) / (3 * alpha - 1);
                double beta_b = 1 - (alpha - alpha_b) / (3 * alpha - 1);
                double beta = Math.max(Math.max(beta_r, beta_g), beta_b);
                double gama_r = (alpha_r - minalpha) / (1 - 3 * minalpha);
                double gama_g = (alpha_g - minalpha) / (1 - 3 * minalpha);
                double gama_b = (alpha_b - minalpha) / (1 - 3 * minalpha);
                double gama = Math.max(Math.max(gama_r, gama_g), gama_b);

                double temp = (gama * (R + G + B) - MaxC) / (3 * gama - 1);

                double[] data = new double[3];
                data[0] = B - (temp + 0.5);
                data[1] = G - (temp + 0.5);
                data[2] = R - (temp + 0.5);
                dst.put(i, j, data);
            }
        }
    }


}
