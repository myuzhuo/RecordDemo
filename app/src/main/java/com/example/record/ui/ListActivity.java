package com.example.record.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.example.record.R;
import com.example.record.adapter.FileListAdapter;
import com.example.record.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ListActivity extends Activity {
    ListView listView;
    List<File> list = new ArrayList<>();
    FileListAdapter adapter;

    public static void startIntent(Context context){
        Intent intent=new Intent(context,ListActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        listView = (ListView) findViewById(R.id.listView);
        list=FileUtils.getWavFiles();
        adapter = new FileListAdapter(this, list);
        listView.setAdapter(adapter);

    }
}
