package com.example.record.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.badoo.mobile.util.WeakHandler;
import com.example.record.AudioRecorderManager;
import com.example.record.R;
import com.example.record.RecorderTimeListener;
import com.example.record.config.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity{

    @BindView(R.id.recorder_type_wav)
    TextView mRecorderTypeWav;

    @BindView(R.id.recorder_type_raw)
    TextView mRecorderTypeRaw;

    @BindView(R.id.recorder_type_mp3)
    TextView mRecorderTypeMp3;

    @BindView(R.id.recorder_time)
    TextView mRecorderTime;

    @BindView(R.id.start)
    Button mRecorderStart;

    @BindView(R.id.recorder_list)
    Button mRecorderList;

    AudioRecorderManager audioRecorderManager;


    private WeakHandler mHandler=new WeakHandler(new HandlerCallback());


    class HandlerCallback implements Handler.Callback{

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case Constants.TIME_REFRESH:
                    String time=(String)msg.obj;
                    mRecorderTime.setText(time);
                    break;
            }
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initPermission();
        init();
    }
    private void init(){
        mRecorderTypeWav.setSelected(true);
        audioRecorderManager = AudioRecorderManager.getInstance();
    }

    private void initPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    110);

        }
    }

    @OnClick({R.id.start, R.id.recorder_list, R.id.recorder_type_wav, R.id.recorder_type_raw, R.id.recorder_type_mp3})
    public void onButtonClick(View view){
        switch (view.getId()){
            case R.id.start:
                try {
                    if (audioRecorderManager.getStatus() == AudioRecorderManager.Status.STATUS_NO_READY) {
                        //初始化录音
                        String fileName = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
                        audioRecorderManager.createDefaultAudio(fileName);
                        audioRecorderManager.startRecord(new RecorderTimeListener() {
                            @Override
                            public void recorderRefreshTime(String time) {
                                Message message = new Message();
                                message.what = Constants.TIME_REFRESH;
                                message.obj = time;
                                mHandler.sendMessage(message);
                            }
                        });
                        mRecorderStart.setText(getString(R.string.recorder_stop));

                    } else {
                        //停止录音
                        audioRecorderManager.stopRecord();
                        mRecorderStart.setText(getString(R.string.recorder_start));
                    }

                } catch (IllegalStateException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.recorder_list:
                if (audioRecorderManager.getStatus() == AudioRecorderManager.Status.STATUS_NO_READY) {
                    ListActivity.startIntent(this);
                }else{
                    Toast.makeText(MainActivity.this, "请先停止录音", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.recorder_type_wav:
                setFileType(R.id.recorder_type_wav);
                break;
            case R.id.recorder_type_raw:
                setFileType(R.id.recorder_type_raw);
                break;
            case R.id.recorder_type_mp3:
                setFileType(R.id.recorder_type_mp3);
                break;
        }
    }

    public void setFileType(int id){
        if(audioRecorderManager.getStatus() != AudioRecorderManager.Status.STATUS_NO_READY){
            return ;
        }
        boolean isSelected;
        switch (id){
            case R.id.recorder_type_wav:
                isSelected=mRecorderTypeWav.isSelected();
                if(isSelected){
                    return ;
                }
                mRecorderTypeWav.setSelected(!isSelected);
                mRecorderTypeRaw.setSelected(isSelected);
                mRecorderTypeMp3.setSelected(isSelected);
                audioRecorderManager.setFileType(R.id.recorder_type_wav);
                break;
            case R.id.recorder_type_raw:
                isSelected=mRecorderTypeRaw.isSelected();
                if(isSelected){
                    return ;
                }
                mRecorderTypeWav.setSelected(isSelected);
                mRecorderTypeRaw.setSelected(!isSelected);
                mRecorderTypeMp3.setSelected(isSelected);
                audioRecorderManager.setFileType(R.id.recorder_type_raw);
                break;
            case R.id.recorder_type_mp3:
                isSelected=mRecorderTypeMp3.isSelected();
                if(isSelected){
                    return ;
                }
                mRecorderTypeWav.setSelected(isSelected);
                mRecorderTypeRaw.setSelected(isSelected);
                mRecorderTypeMp3.setSelected(!isSelected);
                audioRecorderManager.setFileType(R.id.recorder_type_mp3);
                break;
        }
    }
}
