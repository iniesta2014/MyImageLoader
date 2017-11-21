package com.example.qiaowenhao.myimageloader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = findViewById(R.id.list_view);
        ImageAdapter imageAdapter = new ImageAdapter(this, 0, Images.imageUrls);
        listView.setAdapter(imageAdapter);
    }
}
