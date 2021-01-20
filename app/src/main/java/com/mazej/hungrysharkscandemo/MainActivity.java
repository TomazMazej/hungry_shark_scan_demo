package com.mazej.hungrysharkscandemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ImageView img;
    private static String TAG = "MainActivity";

    static{
        if(OpenCVLoader.initDebug()){
            Log.d(TAG, "Deluje");
        }
        else{
            Log.d(TAG, "Ne deluje");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img = findViewById(R.id.imgView);

        Mat src = Imgcodecs.imread(getApplicationContext().getFilesDir()+"/shark_test.jpg");
        Mat gray = new Mat();
        Mat noiseless = new Mat();
        Mat edges = new Mat();

        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        Imgcodecs.imwrite(getApplicationContext().getFilesDir()+"/gray.jpg", gray);

        Imgproc.blur(gray, noiseless, new Size(3, 3));

        Imgcodecs.imwrite(getApplicationContext().getFilesDir()+"/noiseless.jpg",noiseless);

        Imgproc.Canny(noiseless,edges,200,255);

        Imgcodecs.imwrite(getApplicationContext().getFilesDir()+"/edges.jpg",edges);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat drawing = Mat.zeros(edges.size(), CvType.CV_8UC3);
        for (int i = 0; i < contours.size(); i++) {
            Scalar color = new Scalar(255, 255, 255);
            Imgproc.drawContours(drawing, contours, i, color, 2, Core.LINE_8, hierarchy, 0, new Point());
        }

        Imgcodecs.imwrite(getApplicationContext().getFilesDir()+"/contures.jpg",drawing);

        src.setTo(drawing);
        Imgcodecs.imwrite(getApplicationContext().getFilesDir()+"/final.jpg",drawing);
    }
}
