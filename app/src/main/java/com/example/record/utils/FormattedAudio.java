package com.example.record.utils;

import android.util.Log;

import com.example.record.AudioRecorderManager;
import com.example.record.config.Constants;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.example.record.AudioRecorderManager.AUDIO_CHANNEL;
import static com.example.record.AudioRecorderManager.AUDIO_SAMPLE_RATE;

public class FormattedAudio {
    private static  FormattedAudio mFormattedAudio;

    public static FormattedAudio getInstance(){
        if(mFormattedAudio==null){
            mFormattedAudio=new FormattedAudio();
        }
        return mFormattedAudio;
    }

   public synchronized String[] FormatRecordSpecifiedFormatOutput(){
        String[] resutls=new String[2];
        try {
            File pcmFile = new File(FileUtils.getPcmFileAbsolutePath(AudioRecorderManager.getInstance().fileName));
            if (!pcmFile.exists()) {
                return null;
            }
            int TOTAL_SIZE = (int) pcmFile.length();
            String wavPath;
            if(AudioRecorderManager.getInstance().fileType== AudioRecorderManager.FileType.MP3){
                wavPath=FileUtils.getFileAbsolutePath(AudioRecorderManager.getInstance().fileName,Constants.RECORD_FILE_TYPE_WAV);
            }else{
                wavPath=FileUtils.getFileAbsolutePath(AudioRecorderManager.getInstance().fileName,AudioRecorderManager.getInstance().getFileType());
            }

            File outFile = new File(wavPath);
            if (outFile.exists()){
                outFile.delete();
            }
            byte buffer[] = new byte[1024 * 4]; // Length of All Files, Total Size
            OutputStream ouStream = new BufferedOutputStream(new FileOutputStream(wavPath));
            InputStream inStream = new BufferedInputStream(new FileInputStream(pcmFile));
            if(AudioRecorderManager.getInstance().fileType!= AudioRecorderManager.FileType.RAW){
                WriteWaveFileHeader(ouStream,TOTAL_SIZE,TOTAL_SIZE + (44 - 8),AudioRecorderManager.AUDIO_SAMPLE_RATE,AudioRecorderManager.AUDIO_CHANNEL,(AudioRecorderManager.AUDIO_CHANNEL * AudioRecorderManager.AUDIO_SAMPLE_RATE / 8));
            }
            int size = inStream.read(buffer);
            while (size != -1) {
                ouStream.write(buffer);
                size = inStream.read(buffer);
            }
            inStream.close();
            ouStream.close();
            String outPath = "";
            switch (AudioRecorderManager.getInstance().fileType){
                case RAW:
                case WAV:
                    resutls[0]=wavPath;
                case MP3:
                    outPath=FileUtils.getFileAbsolutePath(AudioRecorderManager.getInstance().fileName,Constants.RECORD_FILE_TYPE_MP3);
                    LameUtil.init(AUDIO_SAMPLE_RATE,AUDIO_CHANNEL,0,AUDIO_SAMPLE_RATE,AUDIO_SAMPLE_RATE,9);
                    LameUtil.convertMp3(wavPath,outPath);
                    resutls[0]=outPath;
                    break;
                    default:
                        String Base64Path=FileUtils.getTxtFileAbsolutePath(AudioRecorderManager.getInstance().fileName);
                        FileWriter out = new FileWriter(Base64Path);
                        out.write(FileUtils.encodeBase64File(outPath));
                        out.flush();
                        out.close();
                        resutls[1]=Base64Path;
            }
        } catch (FileNotFoundException e) {
            Log.e("FormattedAudio", e.getMessage());
            return null;
        } catch (IOException ioe) {
            Log.e("FormattedAudio", ioe.getMessage());
            return null;
        } catch (Exception e) {
            Log.e("FormattedAudio", e.getMessage());
            return null;
        }
        return resutls;
    }

    /**
     *提供wav头信息。
     * @param pcm_size 原文件大小
     * @param file_size 文件大小
     * @param longSampleRate 采样频率
     * @param channels  声道
     * @param byteRate 比特率
     * @throws IOException --
     */
    private void WriteWaveFileHeader(OutputStream outPutStream,int pcm_size, int file_size, int longSampleRate,int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header 资源交换文件标志
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (file_size & 0xff);//04~07 4字节size=文件大小-8字节 (从下一个字节开始到文件末尾的总字节数)
        header[5] = (byte) ((file_size >> 8) & 0xff);
        header[6] = (byte) ((file_size >> 16) & 0xff);
        header[7] = (byte) ((file_size >> 24) & 0xff);
        header[8] = 'W';//wav文件标志
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk4字节“fmt” 波形格式标志,最后一位空格
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4字节过滤字节(一般为00000010H)
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // 2字节格式种类(值为1时,表示数据为线性pcm编码)
        header[21] = 0;
        header[22] = (byte) channels;//2字节通道数,单声道为1,双声道为2
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);//4字节采样率
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);//4字节比特率(Byte率=采样频率*音频通道数*每次采样得到的样本位数/8)
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // 2字节数据块长度(每个样本的字节数=通道数*每次采样得到的样本位数/8)
        header[33] = 0;
        header[34] = 16; // 2字节每个采样点的位数
        header[35] = 0;
        header[36] = 'd';//4字节 “data”数据标志符
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (pcm_size & 0xff);//4字节 pcm音频数据大小
        header[41] = (byte) ((pcm_size >> 8) & 0xff);
        header[42] = (byte) ((pcm_size >> 16) & 0xff);
        header[43] = (byte) ((pcm_size >> 24) & 0xff);
        outPutStream.write(header, 0, 44);
    }
}
