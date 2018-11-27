package com.app.iostudio.Upload;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import android.widget.VideoView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.util.IOUtils;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.app.iostudio.R;
import com.app.iostudio.activity.MainActivity;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.HashMap;
import java.util.Map;

public class UploadVideo extends AppCompatActivity implements View.OnClickListener {

    private final String KEY = "AKIAIYKPOLEB6LGXH3QQ";
    private final String SECRET = "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";

    private AmazonS3Client s3Client;
    private BasicAWSCredentials credentials;

    //track Choosing Image Intent
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 1234;
    private static final int PICK_IMAGE = 1;

    private TextView tvFileName;
    private VideoView videoView;
    private EditText edtFileName;
    private EditText txtDescription;
    private TextView imagename;
    private TextView txtpercentage;
    private MediaController ctlr;
    private ProgressBar progressBar;
    private int progressStatus = 0;
    private ProgressDialog progressDialog;
    private Toolbar mToolbar;

    private Uri fileUri=null;
    private Bitmap bitmap;
    RequestQueue requestQueue;
    String imageString="";
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    boolean fileselected=false;
    String slug="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);
         mToolbar = (Toolbar) findViewById(R.id.toolbar_main1);
        mToolbar.setTitle("Upload a new Video");
        mToolbar.setNavigationIcon(R.drawable.back_arrow);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(UploadVideo.this, MainActivity.class);
                startActivity(intent2);
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        videoView = (VideoView) findViewById(R.id.videoView);
        edtFileName =(EditText) findViewById(R.id.edit_file_name);
        txtpercentage = (TextView) findViewById(R.id.txtpercentage);
        txtDescription=(EditText) findViewById(R.id.txtdescription);
        imagename=(TextView) findViewById(R.id.txtimagename);

        requestQueue = Volley.newRequestQueue(this);

//        tvFileName.setText("");

        findViewById(R.id.btngallery).setOnClickListener(this);
        findViewById(R.id.btnpublish).setOnClickListener(this);
        findViewById(R.id.btnimagegallery).setOnClickListener(this);
        findViewById(R.id.btnupload).setOnClickListener(this);

        findViewById(R.id.btnpublish).setEnabled(false);
        findViewById(R.id.btnimagegallery).setEnabled(false);



        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                System.out.println("YourUploadVideo AWSMobileClient is instantiated and you are connected to AWS!");
            }
        }).execute();

        credentials = new BasicAWSCredentials(KEY, SECRET);
        s3Client = new AmazonS3Client(credentials);

    }

    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    private void uploadFile() {

        if (fileUri != null) {


            final String finalpathname=randomAlphaNumeric(10);
            System.out.println("*************"+finalpathname+"****************");
            final String fileName = edtFileName.getText().toString();

            if (!validateInputFileName(fileName)) {
                return;
            }
            try{
                progressBar.setVisibility(View.VISIBLE);
                final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "/" + fileName);

                createFile(getApplicationContext(), fileUri, file);

                TransferUtility transferUtility =
                        TransferUtility.builder()
                                .context(getApplicationContext())
                                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                                .s3Client(s3Client)
                                .build();

                TransferObserver uploadObserver =
                        transferUtility.upload("preview/" + finalpathname + "." + getFileExtension(fileUri), file);
                findViewById(R.id.btncamera).setEnabled(false);
                findViewById(R.id.btngallery).setEnabled(false);
                findViewById(R.id.videoView).setEnabled(false);
                findViewById(R.id.edit_file_name).setEnabled(false);
                findViewById(R.id.txtdescription).setEnabled(false);
                findViewById(R.id.btnupload).setEnabled(false);
                uploadObserver.setTransferListener(new TransferListener() {

                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        if (TransferState.COMPLETED == state) {

                            String url = "http://api.toureazy.com/api/v1/vod/video/create/";
                            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                                    new Response.Listener<String>()
                                    {
                                        @Override
                                        public void onResponse(String response) {

                                            // response
                                            android.util.Log.d("Response", response);
                                            try {
                                                JSONParser parser = new JSONParser();
                                                JSONObject json = (JSONObject) parser.parse(response);
                                                slug=json.get("slug").toString();
                                                Toast.makeText(getApplicationContext(), "Upload Completed!", Toast.LENGTH_LONG).show();
                                                findViewById(R.id.btnpublish).setEnabled(true);
                                                findViewById(R.id.btnimagegallery).setEnabled(true);

                                            }  catch (ParseException e){
                                                System.out.println("ParseException"+e);
                                                Toast.makeText(getApplicationContext(), "Upload Failed!!", Toast.LENGTH_LONG).show();
                                                findViewById(R.id.btncamera).setEnabled(true);
                                                findViewById(R.id.btngallery).setEnabled(true);
                                                findViewById(R.id.videoView).setEnabled(true);
                                                findViewById(R.id.edit_file_name).setEnabled(true);
                                                findViewById(R.id.txtdescription).setEnabled(true);
                                                findViewById(R.id.btnupload).setEnabled(true);
                                            }
                                        }
                                    },
                                    new Response.ErrorListener()
                                    {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            // error
                                            NetworkResponse response = error.networkResponse;

                                            String errorMsg = "";
                                            if (response != null && response.data != null) {
                                                String errorString = new String(response.data);
                                                android.util.Log.d("log error", errorString);
                                                Toast.makeText(getApplicationContext(), "Upload Failed!!", Toast.LENGTH_LONG).show();
                                                findViewById(R.id.btncamera).setEnabled(true);
                                                findViewById(R.id.btngallery).setEnabled(true);
                                                findViewById(R.id.videoView).setEnabled(true);
                                                findViewById(R.id.edit_file_name).setEnabled(true);
                                                findViewById(R.id.txtdescription).setEnabled(true);
                                                findViewById(R.id.btnupload).setEnabled(true);

                                            }
                                        }
                                    }
                            ) {
                                @Override
                                protected Map<String, String> getParams()
                                {
                                    String dst= String.valueOf(txtDescription.getText());
                                    Map<String, String>  params = new HashMap<String, String>();
                                    params.put("title", fileName);
                                    params.put("content", dst);
                                    params.put("video_path", "preview/"+finalpathname+"."+getFileExtension(fileUri));
                                    return params;
                                }
                            };


                            requestQueue.add(postRequest);
                            file.delete();
                            Toast.makeText(getApplicationContext(), "Upload Completed!", Toast.LENGTH_LONG).show();
                        } else if (TransferState.FAILED == state) {
                            file.delete();
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                        int percentDone = (int) percentDonef;
                        progressBar.setProgress(percentDone);

                        txtpercentage.setText(percentDone + "%");
                    }

                    @Override
                    public void onError(int id, Exception ex) {
                        ex.printStackTrace();
                    }

                });
            }
            catch (Exception e){
                System.out.println("exception ..........................................");
                System.out.println(e);
            }


        }
    }



    @Override
    public void onClick(View view) {
        int i = view.getId();

        if (i == R.id.btngallery) {
            showChoosingFile();
        }
        else if (i == R.id.btnpublish) {

            if(imageString.equals("")){
                Toast.makeText(this, "Please select an image!", Toast.LENGTH_SHORT).show();
            }
            else {
                imageUploading();
            }
        }
        else if (i == R.id.btnimagegallery) {
            showChoosingImageFile();
        }
        else if (i == R.id.btnupload) {
            if(fileselected==false){
                Toast.makeText(this, "Please select a video to upload!", Toast.LENGTH_SHORT).show();
            }
            else if(edtFileName.equals("")){
                Toast.makeText(this, "Enter file name!", Toast.LENGTH_SHORT).show();
            }else{
                uploadFile();
            }

        }
    }
    private void imageUploading(){
        findViewById(R.id.btnimagegallery).setEnabled(false);
        findViewById(R.id.btnpublish).setEnabled(false);

        progressDialog = new ProgressDialog(UploadVideo.this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Uploading, please wait...");
        progressDialog.show();

        String url = "http://api.toureazy.com/api/v1/vod/video/update/";
        StringRequest putRequest = new StringRequest(Request.Method.PUT, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        // response
                        android.util.Log.d("Response", response);
                        try {
                            progressDialog.dismiss();
                            JSONParser parser = new JSONParser();
                            JSONObject json = (JSONObject) parser.parse(response);
                            String image=json.get("is_active").toString();
                            if(image.equals("true")){
                                Toast.makeText(getApplicationContext(), "Upload Completed!", Toast.LENGTH_LONG).show();
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "Upload Failed!!", Toast.LENGTH_LONG).show();
                            }

                            findViewById(R.id.btnpublish).setEnabled(true);
                            findViewById(R.id.btnimagegallery).setEnabled(true);

                        }  catch (ParseException e){
                            System.out.println("ParseException"+e);
                            Toast.makeText(getApplicationContext(), "Upload Failed!!", Toast.LENGTH_LONG).show();
                            findViewById(R.id.btnimagegallery).setEnabled(true);
                            findViewById(R.id.btnpublish).setEnabled(true);

                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        progressDialog.dismiss();
                        NetworkResponse response = error.networkResponse;

                        String errorMsg = "";
                        if (response != null && response.data != null) {
                            String errorString = new String(response.data);
                            android.util.Log.d("log error", errorString);

                            Toast.makeText(getApplicationContext(), "Image Upload Failed!!", Toast.LENGTH_LONG).show();
                            findViewById(R.id.btnimagegallery).setEnabled(true);
                            findViewById(R.id.btnpublish).setEnabled(true);

                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                String dst= String.valueOf(txtDescription.getText());
                Map<String, String>  params = new HashMap<String, String>();
                System.out.println("SSSSSSSSSSSSSluGGGGGGGGGGGG "+slug);
                params.put("slug", slug);
                params.put("is_active", "true");
                params.put("image", "data:image/jpeg;base64,"+imageString);






                return params;
            }
        };


        requestQueue.add(putRequest);


    }

    private void showChoosingFile() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);



    }
    private void showChoosingImageFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);



    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == this.RESULT_CANCELED) {
            fileselected=false;

        }

        if (requestCode == REQUEST_TAKE_GALLERY_VIDEO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            fileselected=true;
            Uri contentURI = data.getData();


            getWindow().setFormat(PixelFormat.TRANSLUCENT);

            ctlr = new MediaController(this);
            ctlr.setMediaPlayer(videoView);
            videoView.setMediaController(ctlr);
            videoView.setVideoURI(contentURI);
            videoView.start();

            videoView.stopPlayback();

        }
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                imagename.setText(getFileName(uri));
                // Log.d(TAG, String.valueOf(bitmap));
                InputStream imageStream = null;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imageStream = this.getContentResolver().openInputStream(uri);

                Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

                int nh = (int) ( bitmap.getHeight() * (512.0 / bitmap.getWidth()) );
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true);
                scaled.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);



            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private boolean validateInputFileName(String fileName) {

        if (TextUtils.isEmpty(fileName)) {
            Toast.makeText(this, "Enter file name!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void createFile(Context context, Uri srcUri, File dstFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(dstFile);
            IOUtils.copy(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            System.out.println("createFile "+e);
            e.printStackTrace();
        }
    }

}
