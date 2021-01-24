package com.mazej.hungrysharkscandemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageView showcase;
    private String currentImagePath = null;
    private static final int IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().hide();
        OpenCVLoader.initDebug();
        setContentView(R.layout.activity_main);

        //TODO: make it null
        currentImagePath = getApplicationContext().getFilesDir()+"/test02.jpg";
        showcase = findViewById(R.id.showcase);
        findContours();
    }

    //zajame sliko
    public void captureImage(View view){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(cameraIntent.resolveActivity(getPackageManager()) != null){
            File imageFile = null;

            try{
                imageFile = getImageFile();
            } catch(IOException e){
                e.printStackTrace();
            }

            if(imageFile != null){
                Uri imageUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", imageFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, IMAGE_REQUEST);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(IMAGE_REQUEST == requestCode && resultCode == RESULT_OK){
            findContours();
        }
    }

    //v spremenljivko currentImagePath shrani pot do zajete slike
    private File getImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss").format(new Date());
        String imageName = "jpg_"+timeStamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = File.createTempFile(imageName, ".jpg", storageDir);
        currentImagePath = imageFile.getAbsolutePath();
        return imageFile;
    }

    //najde konture in na koncu prikaze sliko
    public void findContours(){
        //Mat src = Imgcodecs.imread(getApplicationContext().getFilesDir()+"/shark.jpg");
        Mat src = Imgcodecs.imread(currentImagePath);
        Mat conv = new Mat();
        Mat gray = new Mat();
        Mat noiseless = new Mat();
        Mat edges = new Mat();
        Mat kernel = new Mat(3,3,CvType.CV_32F){
            {
                put(0,0,0.11);
                put(0,1,0.11);
                put(0,2,0.11);

                put(1,0,0.11);
                put(1,1,0.11);
                put(1,2,0.11);

                put(2,0,0.11);
                put(2,1,0.11);
                put(2,2,0.11);
            }
        };

        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(gray, noiseless, new Size(3, 3));

        double x = Core.mean(noiseless).val[0];
        System.out.println(x);
        Imgproc.Canny(noiseless,edges,x,255);

        Imgcodecs.imwrite(getApplicationContext().getFilesDir()+"/edges.jpg",edges);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat drawing = Mat.zeros(edges.size(), CvType.CV_8UC3);
        Imgproc.cvtColor(drawing,drawing, Imgproc.COLOR_BGR2BGRA);
        drawing = drawing.setTo(new Scalar(0,0,0,0));
        MatOfPoint2f appCurve = new MatOfPoint2f();
        Rect max = new Rect();
        for (int i = 0; i < contours.size(); i++) {
            Scalar color = new Scalar(255,255,255,255);
            Imgproc.drawContours(drawing, contours, i, color, -1);
            MatOfPoint2f cont2f = new MatOfPoint2f(contours.get(i).toArray());
            double appDistance = Imgproc.arcLength(cont2f, true) * 0.02;
            Imgproc.approxPolyDP(cont2f, appCurve, appDistance,true);

            MatOfPoint points = new MatOfPoint(appCurve.toArray());

            Rect rect = Imgproc.boundingRect(points);

            if(rect.area() > max.area()){
                max = rect;
            }
        }

        max.x -=10;
        max.y -=10;
        max.width+=10;
        max.height+=10;
        Imgproc.rectangle(drawing, new Point(max.x,max.y),
                new Point(max.x+max.width,max.y+max.height),
                new Scalar(255,0,0),3);
        //src.setTo(drawing);


        Mat mask = new Mat(drawing, max);
        Mat roi = new Mat(src,max);

        Imgproc.cvtColor(roi,roi, Imgproc.COLOR_BGR2BGRA);

        Mat end = new Mat();
        Core.bitwise_and(roi,mask,end);

        Size orientation = end.size();
        orientation.width /= 2;

        Rect scope = new Rect();
        scope.width = (int) orientation.width;
        scope.height = (int) orientation.height;

        Mat left = new Mat(end, scope);

        scope.x += scope.width;
        Mat right = new Mat(end, scope);

        Mat test = new Mat();
        if( Core.mean(right).val[0] > Core.mean(left).val[0]){
            test = right;
        }else{
            test = left;
        }
        //Core.bitwise_and(roi,mask,end);
        //Imgproc.threshold(roi,end,100,255,2);

        //Imgproc.filter2D(conv,end,-1,kernel);
        //Mat filled = new Mat();
        //Imgproc.floodFill(end, filled, new Point(0,0),new Scalar(255));
        Imgcodecs.imwrite(getApplicationContext().getFilesDir()+"/final.png",test);

        //File imgFile = new File("/data/user/0/com.mazej.hungrysharkscandemo/files/final.jpg");
        Bitmap myBitmap = BitmapFactory.decodeFile(getApplicationContext().getFilesDir()+"/final.png");
        showcase.setImageBitmap(myBitmap);
    }
}
