package com.example.prova_pol_martin;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_IMAGE_CAPTURE  = 1;
    private static final int MY_PERMISSIONS_REQUESTS = 70;
    private static final String TAG = "test";

    Button btnFoto, btnAndroid, btnSo, btnSend;
    ScrollView scroll;
    LinearLayout orangeLayout, greenLayout;
    LinearLayout.LayoutParams params;
    Context ctx;
    Boolean bGranted = false;
    EditText text;
    SoundPool pool;
    int loaded;
    private int soundID1;
    private int soundID2;
    private int sounds;
    boolean mpReady;
    MediaPlayer myMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnFoto = (Button) findViewById(R.id.fotoBtn);
        btnAndroid = (Button) findViewById(R.id.androidBtn);
        btnSend = (Button) findViewById(R.id.textBtn);
        btnSo = (Button) findViewById(R.id.soBtn);
        text = (EditText) findViewById(R.id.editText);

        btnFoto.setOnClickListener(this);
        btnAndroid.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnSo.setOnClickListener(this);

        scroll = (ScrollView) findViewById(R.id.scroll);
        orangeLayout = (LinearLayout) findViewById(R.id.orangeLayout);
        greenLayout = (LinearLayout) findViewById(R.id.greenLayout);

        ctx = getApplicationContext();

        //Poner linearLayout, encima del teclado.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setSounds(4);

        pool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        pool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded++;
            }
        });

        soundID1 = pool.load(this,R.raw.beep01, 1);
        soundID2 = pool.load(this,R.raw.beep02, 1);


    }

    public void setSounds(int sounds) {
        this.sounds = sounds;
    }

    public void onClick(View v) {
        int id = v.getId();

        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        float actualVolume = (float) audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = actualVolume / maxVolume;
        Log.d("test", "onclick:" + volume + "," + actualVolume + ", " + maxVolume);
        //    Toast.makeText(this, "volume:" + volume, Toast.LENGTH_SHORT).show();

        if (id == R.id.androidBtn) {
            pool.play(soundID2, volume, volume, 0, 0, 1);

            if (mpReady) {
                Log.d(TAG, "Music stopped due to click");
                System.out.println("Click stopped async music");

                myMediaPlayer.stop();
            }


        } else if (id == R.id.textBtn) {

            TextView message = new TextView(this);
            message.setText(text.getText());
            message.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.bocadillo2, null));
            message.setLayoutParams(params);
            orangeLayout.addView(message);
            text.setText("");

        } else if (id == R.id.fotoBtn) {

            checkPermissions();
            if (bGranted) takePhoto();

        } else if (id == R.id.soBtn) {
                pool.play(soundID1, volume, volume, 0, 0, 1);
        }

        scrollDown();
    }

    public void scrollDown() {
        scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    /* Foto */

    public void takePhoto(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Log.d(TAG, "onActivityResult: ");
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            ImageView imageView = new ImageView(ctx);
            imageView.setImageBitmap(imageBitmap);

            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;

            imageView.setLayoutParams(params);
            greenLayout.addView(imageView);

            //Log.d("test", "onActivityResult: " + imageBitmap.getDensity() + "," +
            //        imageBitmap.getHeight() + "," + imageBitmap.getWidth());
        }
        scrollDown();
    }


    /* Permisos */

    public void checkPermissions() {

        //demana permisos i dona explicació si laprimera vegada nega algun d ells


        int permCheck1 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        int permCheck2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (!(permCheck1 == PackageManager.PERMISSION_GRANTED) | (!(permCheck2 == PackageManager.PERMISSION_GRANTED)))
        {
            requestSDReadWritePermissions();

        } else bGranted = true;
    }

    private void requestSDReadWritePermissions(){

        if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) |
                (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)))) {

            // asincrona:no bloquejar el thread esperant la seva resposta
            // Bona pràctica, try again to request the permission.
            // explicar a l usuari per què calen aquests permisos
            new AlertDialog.Builder(this)
                    .setTitle("Es necessita permís d'accés a disc de lectura i escriptura")
                    .setMessage("Per a accedir a càmera, necessitem els dos permisos")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this
                                    ,  new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            Manifest.permission.READ_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUESTS);
                        }
                    })
                    .setNegativeButton("cancel.lar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();


        } else {
            // request the permission.
            // CALLBACK_NUMBER is a integer constants
            //   Toast.makeText(this, "demana permis, no rationale ", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUESTS);
            // The callback method gets the result of the request.
            // Log.d(TAG, "startRecording: no rationale");
        }
    }

}

