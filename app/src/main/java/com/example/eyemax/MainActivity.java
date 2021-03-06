package com.example.eyemax;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.multidex.BuildConfig;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/*
 * MainActivity to get a photo from the device through storage or the camera and pass it to
 * IdentifyActivity or get an actor as a search to pass to IdentifyActivity
 */

public class MainActivity extends AppCompatActivity {

    //declare class variables
    String currentPhotoPath;

    private Bitmap currImage;

    //bind views using butterknife
    @BindView(R.id.searchView)
    SearchView searchView;
    @BindView(R.id.button_identify)
    Button buttonIdentify;
    @BindView(R.id.IdProf)
    ImageView IDProf;
    @BindView(R.id.UploadBtn)
    Button Upload_Btn;

    @OnClick(R.id.button_identify)
    public void onClickButtonIdentify (View v) {
        //check if image exists then create intent
        if (currImage == null) {
            Context context = getApplicationContext();
            CharSequence text = "Please upload an image before proceeding";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else {
            identify(v);
        }
    }

    //when image is clicked call function to get image
    @OnClick(R.id.IdProf)
    public void onClickIdProf (View v){
        selectImage();
    }

    //same functionality as above with upload button
    @OnClick(R.id.UploadBtn)
    public void onClickUploadButton (View v){
        selectImage();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //bind UI with butterknife
        ButterKnife.bind(this);

        //setup and inflate the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        //set toolbar text to white
        toolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(),
                R.color.backgroundColor));
        setSupportActionBar(toolbar);

        //initialize UI
        searchView = (SearchView) findViewById(R.id.searchView);

        //handle text actor search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                //only search if an actor has been typed in
                if(query.length() > 0) {
                    System.out.println("We will search");
                    search(query);
                } else {
                    Context context = getApplicationContext();
                    CharSequence text = "Please enter an actor to search before proceeding";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

        });

        buttonIdentify = (Button) findViewById(R.id.button_identify);
        buttonIdentify.setEnabled(false);

        //button to send image to intent
        buttonIdentify.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //check if image exists then create intent
                if(currImage == null) {
                    Context context = getApplicationContext();
                    CharSequence text = "Please upload an image before proceeding";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                } else {
                    identify(v);
                }
            }
        });

        IDProf=(ImageView)findViewById(R.id.IdProf);
        Upload_Btn=(Button)findViewById(R.id.UploadBtn);

        //when image is clicked call function to get image
        IDProf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        //same functionality as above with upload button
        Upload_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    //handle when the user selects a toolbar item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                settings();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    //create intent for IdenitfyActivity passing image
    public void identify(View v) {
        Intent identify = new Intent(this, IdentifyActivity.class);

        //encode image to byte array, store in intent, and pass intent to start IdentifyActivity
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        currImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        identify.putExtra("image", byteArray);
        startActivity(identify);
    }

    //create intent for SettingsActivity and start it
    public void settings() {
        Intent settings = new Intent(this, SettingsActivity.class);

        startActivity(settings);
    }

    //create intent for IdentifyActivity containing actor search string and start it
    public void search(String actorName) {
        Intent intent = new Intent(this, IdentifyActivity.class);

        intent.putExtra("searchedActor", actorName);
        startActivity(intent);
    }

    //declare int for checking type of image request later
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                System.out.println(ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(Objects.requireNonNull(
                        getApplicationContext()),
                        BuildConfig.APPLICATION_ID + ".provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //create menu for choosing option for getting photo
    private void selectImage() {
        final CharSequence[] options = { getString(R.string.take_photo), getString(
                R.string.choose_from_gallery), getString(R.string.cancel) };
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.add_photo));
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals(getString(R.string.take_photo)))
                {
                    dispatchTakePictureIntent();
                }
                else if (options[item].equals(getString(R.string.choose_from_gallery)))
                {
                    Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images
                            .Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                }
                else if (options[item].equals(getString(R.string.cancel))) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    //get photo from activity result, display it in app, and store it
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //ensure getting photo succeeded
        if (resultCode == RESULT_OK) {

            //if photo was retrieved from camera
            if (requestCode == 1) {
                try {
                    //store photo, resize it, and display it
                    Bitmap bitmap;
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                    bitmap = BitmapFactory.decodeFile(currentPhotoPath, bitmapOptions);
                    bitmap=getResizedBitmap(bitmap, 400);
                    currImage = bitmap;
                    IDProf.setImageBitmap(bitmap);

                    //enable identify button now that an image has been selected
                    buttonIdentify.setEnabled(true);
                    BitMapToString(bitmap);
                    String path = android.os.Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + "Phoenix" + File.separator + "default";
                    OutputStream outFile = null;

                    //create the file for the image
                    File file = new File(path, String.valueOf(System.currentTimeMillis()) +
                            ".jpg");
                    try {
                        outFile = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                        outFile.flush();
                        outFile.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            //if the photo was retrieved from storage
            } else if (requestCode == 2) {
                Uri selectedImage  = data.getData();
                try {
                    //get the image from storage, resize it, and display it
                    final InputStream imageStream = getContentResolver().openInputStream(
                            selectedImage);
                    Bitmap thumbnail = BitmapFactory.decodeStream(imageStream);
                    thumbnail=getResizedBitmap(thumbnail, 400);
                    currImage = thumbnail;
                    IDProf.setImageBitmap(thumbnail);
                    buttonIdentify.setEnabled(true);
                    BitMapToString(thumbnail);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //helper function for encoding bitmap
    public ByteBuffer BitMapToString(Bitmap bmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] b = stream.toByteArray();
        ByteBuffer imageBytes = ByteBuffer.wrap(b);
        return imageBytes;
    }

    //get resized bitmap image
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        System.out.println("image");
        System.out.println(image);
        int width = image.getWidth();
        int height = image.getHeight();

        //resize dimensions acccording to ratio
        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        //return resized image
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}