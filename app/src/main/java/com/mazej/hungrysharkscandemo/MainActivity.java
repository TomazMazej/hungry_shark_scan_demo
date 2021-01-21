package com.mazej.hungrysharkscandemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
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
        currentImagePath = getApplicationContext().getFilesDir()+"/shark_test.jpg";
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
        Mat gray = new Mat();
        Mat noiseless = new Mat();
        Mat edges = new Mat();

        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        Imgcodecs.imwrite(getApplicationContext().getFilesDir()+"/gray.jpg", gray);

        noiseless = gray;
        //Imgproc.blur(gray, noiseless, new Size(1, 1));

        Imgcodecs.imwrite(getApplicationContext().getFilesDir()+"/noiseless.jpg",noiseless);

        Imgproc.Canny(noiseless,edges,200,255);

        Imgcodecs.imwrite(getApplicationContext().getFilesDir()+"/edges.jpg",edges);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat drawing = Mat.ones(edges.size(), CvType.CV_8UC3);
        Imgproc.cvtColor(drawing,drawing, Imgproc.COLOR_BGR2BGRA);
        drawing = drawing.setTo(new Scalar(0,0,0,0));
        for (int i = 0; i < contours.size(); i++) {
            Scalar color = new Scalar(0, 0, 0, 255);
            Imgproc.drawContours(drawing, contours, i, color, 2, Core.LINE_8, hierarchy, 0, new Point());
        }
        //src.setTo(drawing);
        Imgcodecs.imwrite(getApplicationContext().getFilesDir()+"/final.png",drawing);

        //File imgFile = new File("/data/user/0/com.mazej.hungrysharkscandemo/files/final.jpg");
        Bitmap myBitmap = BitmapFactory.decodeFile(getApplicationContext().getFilesDir()+"/final.png");
        showcase.setImageBitmap(myBitmap);
    }
}
