package eu.duong.lastmodified_poc;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import eu.duong.lastmodified_poc.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_READ_MEDIA_IMAGES = 1337;
    private static final int REQUEST_READ_EXT_STORAGE= 1338;
    private static final int REQUEST_WRITE_REQUEST = 1;
    ActivityMainBinding binding;
    Context mContext;

    MediaStoreImage latest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setVersion();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_READ_MEDIA_IMAGES);
        }
        else
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXT_STORAGE);
        }

        binding.setlastmodified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(latest == null)
                {
                    Toast.makeText(mContext, "No image found", Toast.LENGTH_SHORT).show();
                    return;
                }

                File file = new File(latest.path);

                boolean success = file.setLastModified(new Date().getTime());


                binding.result.setText("Success: " + success);
                binding.result.setTextColor(mContext.getColor( success ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                Date date = new Date(file.lastModified());
                binding.modified.setText("Last modified: " + simpleDateFormat.format(date));


            }
        });

        binding.getwriteaccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(latest == null)
                {
                    Toast.makeText(mContext, "No image found", Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayList<Uri> uris = new ArrayList<>();
                uris.add(latest.uri);

               PendingIntent intent =  MediaStore.createWriteRequest(getContentResolver(), uris);

                try {
                    startIntentSenderForResult(intent.getIntentSender(), REQUEST_WRITE_REQUEST, null, 0,0,0);
                } catch (IntentSender.SendIntentException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        binding.getlastestimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String[] projection = {
                        MediaStore.Images.ImageColumns._ID,
                        MediaStore.Images.ImageColumns.DATA,
                        MediaStore.Images.ImageColumns.DISPLAY_NAME
                };


                Uri queryUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);

                Cursor cursor = getContentResolver().query(
                        queryUri,
                        projection,
                        null,
                        null,
                        MediaStore.Images.ImageColumns.DATE_ADDED + " DESC");

                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID);
                int pathColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);

                int displayColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME);


                if (!cursor.moveToFirst()) {
                    Toast.makeText(mContext, "No images found", Toast.LENGTH_SHORT).show();
                    return;


                }

                String path = cursor.getString(pathColumn);
                binding.path.setText("Path: " + path);
                String filename =  cursor.getString(displayColumn);
                binding.filename.setText("Filename: " + filename);

                File file = new File(path);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                Date date = new Date(file.lastModified());
                binding.modified.setText("Last modified: " + simpleDateFormat.format(date));

                latest = new MediaStoreImage( cursor.getLong(idColumn), ContentUris.withAppendedId(queryUri, cursor.getLong(idColumn)),path, filename);


                binding.getwriteaccess.setEnabled(true);


            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_WRITE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(mContext, "Granted", Toast.LENGTH_SHORT).show();
                binding.setlastmodified.setEnabled(true);
            } else {
                Toast.makeText(mContext, "Not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setVersion() {
        binding.version.setText("SDK Version: " + Build.VERSION.SDK_INT);
    }
}