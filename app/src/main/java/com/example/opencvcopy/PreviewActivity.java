package com.example.opencvcopy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class PreviewActivity extends AppCompatActivity {

    ImageView preview;
    String path;
    long mat;
    Button toGrey;
    Button toFlip;
    Button toBlur;
    Bitmap imageBitMap;
    Bitmap originalBitmap;
    Mat grey;
    int MAX_KERNEL_LENGTH=31;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        //to get path of image we clicked in CameraActivity
        path=getIntent().getStringExtra("Path");

        preview=findViewById(R.id.preview);
        toGrey=findViewById(R.id.gray);
        toBlur=findViewById(R.id.blur);
        toFlip=findViewById(R.id.flip);

        //to set bitmap image for ImageView
        try{
            File f=new File(path);
            originalBitmap= BitmapFactory.decodeStream(new FileInputStream(f));
            imageBitMap=originalBitmap;
            preview.setImageBitmap(originalBitmap);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        toGrey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertToGrey();
            }
        });

        toBlur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convetToBlur();
            }
        });

        toFlip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertToFlip();
            }
        });
    }

    private void convertToGrey() {

    }

    private void convetToBlur() {

        if(imageBitMap!=null){
            Log.d("varma","Blurring...");
            Mat mat = new Mat();
            Bitmap bmp32 = imageBitMap.copy(Bitmap.Config.ARGB_8888, true);
            Utils.bitmapToMat(bmp32, mat);

            /*for (int i = 1; i < MAX_KERNEL_LENGTH; i = i + 2) {
                Imgproc.blur(mat, mat, new Size(i, i),
                        new Point(-1, -1));

                // Display blurred input image
                //displayDst(DELAY_BLUR);
            }*/

            for (int i = 1; i < MAX_KERNEL_LENGTH; i = i + 2) {
                Imgproc.GaussianBlur(mat, mat, new Size(i, i),
                        0, 0);

                //dst = Imgproc.GaussianBlur(mat,(5,5), BORDER_DEFAULT);
                Log.d("varma","in loop of blur");
            }

            Log.d("varma","Done with blur");
            updateImage(mat);

        }

    }

    private void convertToFlip() {
    }
    
    private void updateImage(Mat Lmat){
        Utils.matToBitmap(Lmat, imageBitMap);
        preview.setImageBitmap(imageBitMap);
        Log.d("varma","Image updated");
    }
}