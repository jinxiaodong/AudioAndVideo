package com.xiaodong.audioandvideo.audio.utils;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.Log;

import com.xiaodong.basetools.constants.GlobalConstants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by xiaodong.jin on 2018/6/26.
 * description：音频采集 工具类：注意添加对应权限
 */

public class AudioCaptureUtils {

    private static AudioCaptureUtils mInstance;

    /*Android 提供的基于字节的音频采集API类*/
    private AudioRecord mAudioRecord;


    /*
    *音频采集输入源：DEFAUT(默认)、VOICE_RECOGNITION（用于语音识别，等同于DEFAULT）
    *               MIC（由手机麦克风输入）、VOICE_COMMUNICATION（用于VoIP应用）
    * */
    private static int audioSource = MediaRecorder.AudioSource.MIC;

    /*录音的采样率：目前44100Hz是唯一可以保证兼容所有Android手机的采样率。*/
    private static int audioRate = 44100;

    /*录音的声道：单声道：CHANNEL_IN_MONO、双通道：CHANNEL_IN_STEREO*/
    private static int audioChannel = AudioFormat.CHANNEL_IN_STEREO;

    /*采样数据位宽：常用的是 ENCODING_PCM_16BIT（16bit），ENCODING_PCM_8BIT（8bit）*/
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    /*
     *   缓存大小：配置的是 AudioRecord 内部的音频缓冲区的大小,该缓冲区的值不能低于一帧“音频帧”（Frame）的大小
     *   计算方式：int size = 采样率 x 位宽 x 采样时间 x 通道数
     *   AudioRecord提供的辅助函数：getMinBufferSize(int sampleRateInHz, int channelConfig, int audioFormat);
     * */
    private static int bufferSize = AudioRecord.getMinBufferSize(audioRate, audioChannel, audioFormat);


    /*记录播放状态*/
    private boolean isRecording = false;
    /*数字信号数组*/
    private byte[] noteArray;
    /*PCM文件*/
    private File pcmFile;
    /*WAV文件*/
    private File wavFile;
    /*文件输出流*/
    private OutputStream os;


    /**********关于目录：在具体的项目中，按照项目规范来配置：********/
    /*文件根目录*/
    private String basePath = GlobalConstants.CACHE_AUDIO + "/AudioTest/";
    //wav文件目录
    private String wavFileName = "";
    //pcm文件目录
    private String pcmFileName = "";
    /*读取数据的线程*/
    private Thread mThread;
    private String pcmName = "test1";


    private AudioCaptureUtils() {
        createFile();
//        mAudioRecord = new AudioRecord(audioSource, audioRate, audioChannel, audioFormat, bufferSize);
    }

    public synchronized static AudioCaptureUtils getInstance() {
        if (mInstance == null) {
            mInstance = new AudioCaptureUtils();
        }
        return mInstance;
    }


    public void stopRecord() {
        if (!isRecording) {
            return;
        }

        isRecording = false;
        try {
            mThread.interrupt();
            mThread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            mAudioRecord.stop();
        }
        mAudioRecord.release();
        mAudioRecord = null;
        Log.e("录音", "录音结束");
    }

    /*开始录音*/
    public void startRecord() {
        if (isRecording) {
            return;
        }
        mAudioRecord = new AudioRecord(audioSource, audioRate, audioChannel, audioFormat, bufferSize);

        isRecording = true;
        Log.e("录音", "录音开始");
        mAudioRecord.startRecording();
        readData();
    }

    /*录音读取数据：需要放在子线程中*/
    private void readData() {

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {

                /*缓冲区*/
                noteArray = new byte[bufferSize];
                boolean pcmFile =  getPcmFile(pcmName);
                try {
                    os = new BufferedOutputStream(new FileOutputStream(AudioCaptureUtils.this.pcmFile, pcmFile));
                    Log.e("录音", "=====" + pcmFile);
                    while (isRecording) {
                        int recordSize = mAudioRecord.read(noteArray, 0, bufferSize);
                        if (recordSize > 0) {
                            os.write(noteArray);
                        }
                    }
                    if (os != null) {
                        os.close();
                        os = null;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        mThread.start();

    }


    /**
     * 创建所需的文件夹：如果不存在的话
     */
    private void createFile() {
        File file = new File(basePath);
        if (!file.exists()) {
            //创建整个文件目录
            file.mkdirs();
        }
    }


    /*获取pcm文件，如果文件存在返回true，后续读写时，添加到这个文件中*/

    private boolean getPcmFile(String path) {
        boolean isexist = false;
        if (TextUtils.isEmpty(path)) {
            //如果传入的路径为空，那么就使用系统时间戳
            path = System.currentTimeMillis() + "";
        }
        if (path.endsWith(".pcm")) {
            pcmFileName = basePath + "/" + path;
        } else {
            pcmFileName = basePath + "/" + path + ".pcm";
        }

        pcmFile = new File(pcmFileName);

        if (pcmFile.exists()) {
            isexist = true;
        }
        /*如果文件已经存在，那么不用创建，直接使用原文件，在尾部追加数据*/
        try {
            pcmFile.createNewFile();
        } catch (IOException e) {
        }
        return isexist;
    }

    private void getWavFile(String path) {
        if (TextUtils.isEmpty(path)) {
            //如果传入的路径为空，那么就使用系统时间戳
            path = System.currentTimeMillis() + "";
        }
        if (path.endsWith(".wav")) {
            wavFileName = basePath + "/" + path;
        } else {
            wavFileName = basePath + "/" + path + ".wav";
        }

        wavFile = new File(wavFileName);
        if (wavFile.exists()) {
            wavFile.delete();
        }
        try {
            wavFile.createNewFile();
        } catch (IOException e) {

        }
    }


    /*转换成wav文件*/
    public void convertWaveFile(String wavFilePath) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = audioRate;
        int channels = 1;
        long byteRate = 16 * audioRate * channels / 8;
        byte[] data = new byte[bufferSize];
        try {
            getWavFile(wavFilePath);
            in = new FileInputStream(pcmFileName);
            out = new FileOutputStream(wavFileName);
            totalAudioLen = in.getChannel().size();
            //由于不包括RIFF和WAV
            totalDataLen = totalAudioLen + 36;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
   任何一种文件在头部添加相应的头文件才能够确定的表示这种文件的格式，wave是RIFF文件结构，每一部分为一个chunk，其中有RIFF WAVE chunk，
   FMT Chunk，Fact chunk,Data chunk,其中Fact chunk是可以选择的，
    */
    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate,
                                     int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (1 * 16 / 8);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

}
