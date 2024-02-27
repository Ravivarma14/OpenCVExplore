package com.example.opencvcopy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import org.opencv.android.*;
import org.opencv.core.*;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.*;
import org.opencv.imgproc.*;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CameraActivity extends org.opencv.android.CameraActivity {

    CameraBridgeVieafwBase cameraBridgeViewBase;
    CascadeClassifier cascadeClassifier;
    Mat rgba,gray;
    MatOfRect rects;
    ImageView shutter;
    ImageView flipCamera;
    ImageView convertToGray;
    ImageView faceDetection;
    float animationControl=360f;
    int saveImage=0;
    int cameraId=0;
    boolean grayControl=false;
    boolean facedetect=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPermission();
        }


        shutter=findViewById(R.id.shutter);
        flipCamera=findViewById(R.id.flipCamera);
        convertToGray=findViewById(R.id.convertToGray);
        faceDetection=findViewById(R.id.faceDetection);
        cameraBridgeViewBase=findViewById(R.id.cameraView);
        cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                rgba=new Mat();
                gray=new Mat();
                rects=new MatOfRect();
            }

            @Override
            public void onCameraViewStopped() {
                rgba.release();
                gray.release();
                rects.release();
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

                rgba=inputFrame.rgba();
                gray=inputFrame.gray();

                //if front camera we have to flip the cam as it previews in rotate mode
                if (cameraId == 1) {
                    Core.flip(rgba, rgba, -1);
                    Core.flip(gray, gray, -1);
                }

                if(facedetect){
                    Mat argMat = grayControl ? gray : rgba;

                    cascadeClassifier.detectMultiScale(gray,rects,1.1,2);

                    for(Rect rect: rects.toList()){
                        Mat submat= argMat.submat(rect);
                        Imgproc.blur(submat,submat,new Size(10,10));
                        Imgproc.rectangle(argMat,rect,new Scalar(0,0,180),3);

                        submat.release();
                    }

                    saveImage = take_picture(saveImage, argMat);

                    return argMat;
                }
                else {

                    Mat argMat = grayControl ? gray : rgba;

                    saveImage = take_picture(saveImage, argMat);

                    return argMat;
                }
            }
        });

        if(OpenCVLoader.initDebug()){
            cameraBridgeViewBase.enableView();

            // code for blur faces


            try {

                InputStream inputStream=getResources().openRawResource(R.raw.lbpcascade_frontalface);
                File file=new File(getDir("cascade",MODE_PRIVATE),"lbpcascade_frontalface.xml");
                FileOutputStream fileOutputStream=new FileOutputStream(file);

                byte[] data=new byte[4096];
                int read_bytes;

                while ((read_bytes=inputStream.read(data))!=-1){
                    fileOutputStream.write(data,0,read_bytes);
                }

                cascadeClassifier=new CascadeClassifier(file.getAbsolutePath());
                if(cascadeClassifier.empty()) cascadeClassifier=null;


                inputStream.close();
                fileOutputStream.close();
                file.delete();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        shutter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(saveImage==1){
                    saveImage=0;
                }
                else {
                    saveImage = 1;
                }
                shutter.animate().rotation(animationControl).setDuration(500).start();
                animationControl=(animationControl+360f) % 720f;
            }
        });

        faceDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                facedetect=!facedetect;

                faceDetection.setImageDrawable( facedetect ? getDrawable(R.drawable.tag_faces_on) : getDrawable(R.drawable.tag_faces_off));

            }
        });


        flipCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swapCamera();
            }
        });

        convertToGray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grayControl=!grayControl;
                convertToGray.setBackground( grayControl ? getDrawable(R.color.blue) : getDrawable(R.color.white));
            }
        });
    }

    private void swapCamera(){
        cameraId=cameraId^1;
        cameraBridgeViewBase.disableView();

        cameraBridgeViewBase.setCameraIndex(cameraId);
        cameraBridgeViewBase.enableView();
    }

    private int take_picture(int saveImage, Mat rgba) {

        if(saveImage==1) {
            Mat saveMat = new Mat();
            Core.flip(rgba.t(), saveMat, 1);
            if(!grayControl) {
                Imgproc.cvtColor(saveMat, saveMat, Imgproc.COLOR_RGBA2BGRA);
            }


            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String currentDateAndTime = simpleDateFormat.format(new Date());
            String fileName = Environment.getExternalStorageDirectory().getPath()+"/DCIM/" +currentDateAndTime + ".jpg";
            Log.d("varma","filename: "+fileName);

            Imgcodecs.imwrite(fileName, saveMat);
            saveImage = 0;
            Intent intent=new Intent(CameraActivity.this,PreviewActivity.class);
            intent.putExtra("Path",fileName);
            //intent.putExtra("Mat",save_mat.getNativeObjAddr());
            startActivity(intent);
        }

        return saveImage;
    }

    private void getPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.CAMERA},101);
            }
            /*if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},100);
            }
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},102);
            }*/
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0]!=PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getPermission();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraBridgeViewBase.enableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraBridgeViewBase.disableView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraBridgeViewBase.disableView();
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(cameraBridgeViewBase);
    }
}