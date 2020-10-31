package com.mrwhitedeveloper.basic.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.mrwhitedeveloper.basic.model.ImageFilePath;

public class GalleryIntentActivity extends AppCompatActivity {
      Bitmap bitmap;
      Button btnGallery;
      ImageView imageView;
       String imgPath;
          private static int RESULT_LOAD_IMG = 1;
    private static int REQUEST_IMAGE_CAPTURE = 1;
    private static String TIME_STAMP="null";
        ProgressDialog progressBar;
        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_intent);
        
          imageView=findViewById(R.id.imageView);
        btnGallery=findViewById(R.id.btnGallery);
        btnGallery.setOnClickListener(view->{
            Access access=new Access();
            if(access.isReadPermissionGranted(AdmitStudentActivity.this)){
                loadImageFromGallery(view);
            }

        });
    }
      public void loadImageFromGallery(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }
    
      @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode ==1 && resultCode==RESULT_OK){
            Uri selectedImageUri = data.getData();
            try {
                imgPath= ImageFilePath.getPath(GalleryIntentActivity.this, selectedImageUri);
                Log.d(LOG_NAME,"img result="+imgPath);
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri));
                imageView.setImageBitmap(bitmap);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                bytes = stream.toByteArray();
            }catch (IOException ioException){
                Log.d(LOG_NAME, "gallery "+ioException.toString());
            }
        }
      
    }
    private void uploadData(String url,String firstName){
       progressBar = new ProgressDialog(this);
        progressBar.setCancelable(true);
        progressBar.setMessage("Saving data please wait...");
        progressBar.show();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();

        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "filename.jpg",
                        RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                .addFormDataPart("first_name",firstName)
         ).build();

        // Initialize a new Request
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Do something when request failed
                e.printStackTrace();
                Log.d(LOG_NAME," Request Failed."+e.toString());
                progressBar.dismiss();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(!response.isSuccessful()){
                    throw new IOException("Error : " + response);
                }else {
                    Log.d(LOG_NAME," Request Successful.");
                }
                progressBar.dismiss();

                // Read data in the worker thread
                final String s_data = response.body().string();

                try {

                    JSONObject jsonObj = new JSONObject(s_data);
                    final int code=jsonObj.getInt("code");

                    GalleryIntentActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(code==1){
                                Toast.makeText(GalleryIntentActivity.this,"Uploaded" , Toast.LENGTH_LONG).show();
                                clearAllTextFields();
                            }
                            else if(code==2){
                                Toast.makeText(GalleryIntentActivity.this, "Not Uploaded", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                } catch (JSONException e) {
                    Log.e(LOG_NAME, "Error parsing data " + e.toString());
                    // Toast.makeText(GalleryIntentActivity.this, "JsonArray fail", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
}
