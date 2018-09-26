package com.example.record.utils;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 管理录音文件的类
 */
public class FileUtils {

    private  static String rootPath="RecordDemo";
    //原始文件(不能播放)
    private final static String AUDIO_PCM_BASEPATH = "/"+rootPath+"/pcm/";
    //可播放的高质量音频文件
    private final static String AUDIO_BASEPATH = "/"+rootPath+"/record/";
    //可播放的高质量音频文件
    private final static String AUDIO_BASE64_BASEPATH = "/"+rootPath+"/base64/";

    private static void setRootPath(String rootPath){
        FileUtils.rootPath=rootPath;
    }

    public static String getPcmFileAbsolutePath(String fileName){
        if(TextUtils.isEmpty(fileName)){
            throw new NullPointerException("fileName isEmpty");
        }
        if(!isSdcardExit()){
            throw new IllegalStateException("sd card no found");
        }
        String mAudioRawPath = "";
        if (isSdcardExit()) {
            if (!fileName.endsWith(".pcm")) {
                fileName = fileName + ".pcm";
            }
            String fileBasePath = Environment.getExternalStorageDirectory().getAbsolutePath() + AUDIO_PCM_BASEPATH;
            File file = new File(fileBasePath);
            //创建目录
            if (!file.exists()) {
                file.mkdirs();
            }
            mAudioRawPath = fileBasePath + fileName;
        }

        return mAudioRawPath;
    }

    public static String getFileAbsolutePath(String fileName,String fileType) {
        if(fileName==null){
            throw new NullPointerException("fileName can't be null");
        }
        if(!isSdcardExit()){
            throw new IllegalStateException("sd card no found");
        }

        String mAudioPath = "";
        if (isSdcardExit()) {
            if (!fileName.endsWith(fileType)) {
                fileName = fileName + fileType;
            }
            String fileBasePath = Environment.getExternalStorageDirectory().getAbsolutePath() + AUDIO_BASEPATH;
            File file = new File(fileBasePath);
            //创建目录
            if (!file.exists()) {
                file.mkdirs();
            }
            mAudioPath = fileBasePath + fileName;
        }
        return mAudioPath;
    }


    public static String getTxtFileAbsolutePath(String fileName) {
        if(fileName==null){
            throw new NullPointerException("fileName can't be null");
        }
        if(!isSdcardExit()){
            throw new IllegalStateException("sd card no found");
        }

        String mAudioWavPath = "";
        if (isSdcardExit()) {
            if (!fileName.endsWith(".txt")) {
                fileName = fileName + ".txt";
            }
            String fileBasePath = Environment.getExternalStorageDirectory().getAbsolutePath() + AUDIO_BASE64_BASEPATH;
            File file = new File(fileBasePath);
            //创建目录
            if (!file.exists()) {
                file.mkdirs();
            }
            mAudioWavPath = fileBasePath + fileName;
        }
        return mAudioWavPath;
    }
    /**
     * 判断是否有外部存储设备sdcard
     *
     * @return true | false
     */
    private static boolean isSdcardExit() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取全部wav文件列表
     */
    public static List<File> getWavFiles() {
        List<File> list = new ArrayList<>();
        String fileBasePath = Environment.getExternalStorageDirectory().getAbsolutePath() + AUDIO_BASEPATH;
        File rootFile = new File(fileBasePath);
        if (rootFile.exists()) {
            File[] files = rootFile.listFiles();
            Collections.addAll(list, files);
        }
        return list;
    }

    /**
     * 将文件转成base64 字符串)
     * @param path 文件路径
     */
    public static String encodeBase64File(String path) throws Exception {
        File  file = new File(path);
        FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int)file.length()];
        inputFile.read(buffer);
        inputFile.close();
        return Base64.encodeToString(buffer,Base64.DEFAULT);
    }

}
