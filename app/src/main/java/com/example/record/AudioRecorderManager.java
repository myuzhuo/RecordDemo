package com.example.record;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import com.example.record.config.Constants;
import com.example.record.utils.FormattedAudio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.record.config.Constants.AUDIO_RECORDER_TAG;
import static com.example.record.config.Constants.RECORD_FILE_TYPE_WAV;
import static com.example.record.utils.FileUtils.getPcmFileAbsolutePath;

/**
 * 用于实现录音
 */
public class AudioRecorderManager {
    private static AudioRecorderManager audioRecorderManager;

    public static AudioRecorderManager getInstance() {
        if (audioRecorderManager == null) {
            audioRecorderManager = new AudioRecorderManager();
        }
        return audioRecorderManager;
    }

    //音频输入-麦克风
    public final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    //采用频率
    //44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    //采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
    public final static int AUDIO_SAMPLE_RATE = 16000;
    //声道 单声道
    public final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    //编码
    public final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    public int bufferSizeInBytes = 0;
    //录音对象
    private AudioRecord audioRecord;

    //录音状态
    private Status status = Status.STATUS_NO_READY;

    //保存文件类型
    public FileType fileType= FileType.WAV;

    //文件名
    public String fileName;

    //线程池
    private ExecutorService mExecutorService;
    private AudioRecorderManager() {
        mExecutorService = Executors.newCachedThreadPool();
    }

    private long mStartTime;

    private long mTotalTime;

    private Handler mTimeHandler;
    /**
     * 创建默认的录音对象
     *
     * @param fileName 文件名
     */
    public void createDefaultAudio(String fileName) {
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,
                AUDIO_CHANNEL, AUDIO_ENCODING);
        audioRecord = new AudioRecord(AUDIO_INPUT, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING, bufferSizeInBytes);
        this.fileName = fileName;
        status = Status.STATUS_READY;
    }

    /**
     * 开始录音
     */
    public void startRecord(final RecorderTimeListener listener) {

        if (status == Status.STATUS_NO_READY||audioRecord==null) {
            throw new IllegalStateException("录音尚未初始化,请检查是否禁止了录音权限~");
        }
        if (status == Status.STATUS_START) {
            throw new IllegalStateException("正在录音");
        }
        mStartTime=System.currentTimeMillis();
        audioRecord.startRecording();
        //将录音状态设置成正在录音状态
        status = Status.STATUS_START;
        //使用线程池管理线程
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                writeDataTOFile(fileName);
            }
        });
        mTimeHandler =new Handler();
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                if (status == Status.STATUS_START) {
                    mTimeHandler.postDelayed(this,1000);
                }else{
                    return;
                }
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                mTotalTime=(System.currentTimeMillis()-mStartTime) - TimeZone.getDefault().getRawOffset();
                listener.recorderRefreshTime(simpleDateFormat.format(mTotalTime));
            }
        });
    }


    /**
     * 停止录音
     */
    public void stopRecord() {
        if (status == Status.STATUS_NO_READY || status == Status.STATUS_READY) {
            throw new IllegalStateException("录音尚未开始");
        } else {
            status = Status.STATUS_STOP;
            audioRecord.stop();
            release();
        }
    }

    /**
     * 释放资源
     */
    private void release() {
        Log.d(AUDIO_RECORDER_TAG,"===release===");
        try {
            makePCMFileToFile();
        } catch (IllegalStateException e) {
            throw new IllegalStateException(e.getMessage());
        }

        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }

        status = Status.STATUS_NO_READY;
    }


    /**
     * 将音频信息写入文件
     *
     */
    private void writeDataTOFile(String currentFileName) {
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        byte[] audiodata = new byte[bufferSizeInBytes];
        int readsize;
        try {
            File file = new File(getPcmFileAbsolutePath(currentFileName));
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fos = new FileOutputStream(file);// 建立一个可存取字节的文件
            while (status == Status.STATUS_START) {
                readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
                if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
                    try {
                        fos.write(audiodata);
                    } catch (IOException e) {
                        Log.e(AUDIO_RECORDER_TAG, e.getMessage());
                    }
                }
            }
            fos.close();// 关闭写入流
        } catch (IllegalStateException e) {
            Log.e(AUDIO_RECORDER_TAG, e.getMessage());
            throw new IllegalStateException(e.getMessage());
        } catch (FileNotFoundException e) {
            Log.e(AUDIO_RECORDER_TAG, e.getMessage());
        }catch (IOException e) {
            Log.e(AUDIO_RECORDER_TAG, e.getMessage());
        }
    }

    /**
     * 将单个pcm文件转化为wav文件
     */
    private void makePCMFileToFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] results=FormattedAudio.getInstance().FormatRecordSpecifiedFormatOutput();
                fileName = null;
            }
        }).start();
    }

    /**
     * 获取录音对象的状态
     */
    public Status getStatus() {
        return status;
    }

    /**
     * 录音对象的状态
     */
    public enum Status {
        //未开始
        STATUS_NO_READY,
        //预备
        STATUS_READY,
        //录音
        STATUS_START,
        //停止
        STATUS_STOP
    }

    /**
     * 录音文件类型
     */
    public enum FileType {
        WAV,
        RAW,
        MP3
    }

    public String getFileType(){
        if(fileType== FileType.RAW){
            return Constants.RECORD_FILE_TYPE_RAW;
        }else if(fileType== FileType.MP3){
            return Constants.RECORD_FILE_TYPE_MP3;
        }else {
            return RECORD_FILE_TYPE_WAV;
        }
    }

    public void setFileType(int id){
        switch (id){
            case R.id.recorder_type_wav:
                fileType= FileType.WAV;
                break;
            case R.id.recorder_type_raw:
                fileType= FileType.RAW;
                break;
            case R.id.recorder_type_mp3:
                fileType= FileType.MP3;
                break;
        }
    }

}
