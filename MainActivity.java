package com.example.solution_color;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.library.bitmap_utilities.BitMap_Helpers;
import java.io.File;
import java.io.IOException;
import static android.widget.Toast.LENGTH_LONG;
public class MainActivity extends AppCompatActivity  {

    private static final int CAMERA_REQUEST = 1888;
    public Toolbar myToolbar;
    public ViewGroup relativeLayout;
    private String tPath;
    private Bitmap bitmap;
    private int heightScreen, widthScreen;
    private int sketchPref, colorPref = 50;
    private static final String pathName = "pathString";
    private int alpha = 120;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.getBackground().setAlpha(alpha);

        SharedPreferences prefs = getSharedPreferences(pathName, 0);
        String pathOfLast = prefs.getString("path",null);
        getMetrics();

        if(pathOfLast != null) {
            tPath = pathOfLast;
            bitmap = Camera_Helpers.loadAndScaleImage(tPath, heightScreen, widthScreen);
            if(bitmap != null) {
                Drawable pic = new BitmapDrawable(getResources(), bitmap);
                setDrawLayout(pic);
            }
            else{
                setDrawLayout(getGutter());
            }
        }
        else{
            setDrawLayout(getGutter());
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(bitmap == null){
            Toast.makeText(this, "Please take a picture before Editing/Sharing", LENGTH_LONG).show();
        }
        else {
            if (id == R.id.action_colorize) {
                doColorize();
            }
            if (id == R.id.action_edit) {
                getEditPreferences();
                bitmap = BitMap_Helpers.thresholdBmp(bitmap, sketchPref);
                Drawable pic = new BitmapDrawable(getResources(), bitmap);
                relativeLayout.setBackgroundDrawable(pic);
                Camera_Helpers.saveProcessedImage(bitmap, tPath);
            }
            if (id == R.id.action_share) {
                File file = new File(tPath);
                Uri sharePic = Uri.fromFile(file);
                Intent myIntent = new Intent(Intent.ACTION_SEND);
                myIntent.setType("image/jpg");
                String shareSubject = "";
                String shareText = "";
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
                shareSubject = pref.getString("share_subject", shareSubject);
                shareText = pref.getString("share_text", shareText);
                myIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSubject);
                myIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
                myIntent.putExtra(android.content.Intent.EXTRA_STREAM, sharePic);
                startActivity(myIntent);
            }
            if (id == R.id.action_revert) {
                Camera_Helpers.delSavedImage(tPath);
                Bitmap bm = ((BitmapDrawable) getResources().getDrawable(R.drawable.gutters)).getBitmap();
                Drawable gut = new BitmapDrawable(getResources(), bm);
                relativeLayout.setBackgroundDrawable(gut);
                bitmap = null;
            }
        }
            if (id == R.id.action_settings) {
                Intent myIntent = new Intent(this, SettingsActivity.class);
                startActivity(myIntent);
            }

        return super.onOptionsItemSelected(item);
    }
    private void setDrawLayout(Drawable d){
        relativeLayout = (ViewGroup) findViewById(R.id.relative_layout);
        relativeLayout.setBackgroundDrawable(d);
    }
    private void getMetrics(){
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        heightScreen = metrics.heightPixels;
        widthScreen = metrics.widthPixels;
    }
    private Drawable getGutter(){
        Bitmap bm = ((BitmapDrawable) getResources().getDrawable(R.drawable.gutters)).getBitmap();
        Drawable gut = new BitmapDrawable(getResources(),bm);
        return gut;
    }
    private void doColorize(){
        getEditPreferences();
        Bitmap bitmapSketch = BitMap_Helpers.thresholdBmp(bitmap, sketchPref);
        Bitmap bitmapColorize = BitMap_Helpers.colorBmp(bitmap, colorPref);
        BitMap_Helpers.merge(bitmapColorize, bitmapSketch);
        bitmap = bitmapColorize;
        Drawable pic = new BitmapDrawable(getResources(),bitmap);
        relativeLayout.setBackgroundDrawable(pic);
        Camera_Helpers.saveProcessedImage(bitmapColorize, tPath);
    }
    private void getEditPreferences(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        sketchPref = preferences.getInt("sketchiness", sketchPref);
        colorPref = preferences.getInt("saturation", colorPref);
    }
    public void takePhoto(View view)throws IOException {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File tempFile;
        tempFile = File.createTempFile("camera",".png", getExternalCacheDir());
        tPath = tempFile.getAbsolutePath();
        Uri uri = Uri.fromFile(tempFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            getMetrics();
            bitmap = Camera_Helpers.loadAndScaleImage(tPath, heightScreen, widthScreen);
            Drawable pic = new BitmapDrawable(getResources(),bitmap);
            relativeLayout.setBackgroundDrawable(pic);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences prefs = getSharedPreferences(pathName, 0);
        SharedPreferences.Editor edits = prefs.edit();
        edits.putString("path",tPath);
        edits.apply();
    }
    }



