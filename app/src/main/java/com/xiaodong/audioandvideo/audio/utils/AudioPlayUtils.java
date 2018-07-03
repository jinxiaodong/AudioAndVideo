package com.xiaodong.audioandvideo.audio.utils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.xiaodong.basetools.constants.GlobalConstants;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Created by xiaodong.jin on 2018/7/3.
 * description：播放一帧音频工具类
 */
public class AudioPlayUtils {


    /*streamType：当前应用使用哪种音频管理策略，当有多个进程播放音乐时，会按照这个策略展现最终的播放效果*/
    private static final int DEFAULT_STREAM_TYPE = AudioManager.STREAM_MUSIC;

    /*sampleRateInHz:采样率*/
    private static final int DEFAULT_SAMPLE_RATE = 44100;

    /*channelConfig：通道：单：CHANNEL_IN_MONO，双：CHANNEL_IN_STEREO*/
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;

    /*audioFormat:采样数据位宽：常用的是 ENCODING_PCM_16BIT（16bit），ENCODING_PCM_8BIT（8bit）*/
    private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    /*
     * mode:AudioTrack 提供了两种播放模式，一种是 static 方式，一种是 streaming 方式，前者需要一次性将所有的数据都写入播放缓冲区，
     * 简单高效，通常用于播放铃声、系统提醒的音频片段; 后者则是按照一定的时间间隔不间断地写入音频数据，理论上它可用于任何音频播放的场景。
     */
    private static final int DEFAULT_PLAY_MODE = AudioTrack.MODE_STREAM;

    /*bufferSizeInBytes:播放音频的缓冲区大小*/
    private int minBufferSize = AudioTrack.getMinBufferSize(DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT);

    /*是否正在播放*/
    private boolean isPlaying = false;

    /*播放音频的api*/
    private AudioTrack mAudioTrack;
    /*数字信号数组*/
    private byte[] noteArray;
    /*文件根目录*/
    private String basePath = GlobalConstants.CACHE_AUDIO + "/AudioTest/";
    //pcm文件目录
    private String pcmFileName = "";
    /*PCM文件*/
    private File pcmFile;

    private static AudioPlayUtils mInstance;
    private Thread mThread;

    private AudioPlayUtils() {

    }

    public synchronized static AudioPlayUtils getInstance() {
        if (mInstance == null) {
            mInstance = new AudioPlayUtils();
        }
        return mInstance;
    }


    public boolean startPlay(String name) {

        if (isPlaying || minBufferSize == AudioTrack.ERROR_BAD_VALUE) {
            Log.e("TAG", "播放失败");
            return false;
        }

        mAudioTrack = new AudioTrack(DEFAULT_STREAM_TYPE, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT, minBufferSize, DEFAULT_PLAY_MODE);
        if (mAudioTrack.getState() == AudioTrack.STATE_UNINITIALIZED) {
            Log.e("TAG", "AudioTrack 初始化失败！");
            return false;
        }


        if (name.endsWith(".pcm")) {
            pcmFileName = basePath + "/" + name;
        } else {
            pcmFileName = basePath + "/" + name + ".pcm";
        }
        pcmFile = new File(pcmFileName);
        if (!pcmFile.exists()) {
            Log.e("TAG", "文件不存在");
            return false;
        }
        isPlaying = true;
        readData();
        Log.e("TAG", "播放成功！");
        return true;
    }

    private void readData() {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                noteArray = new byte[minBufferSize];
                try {
                    BufferedInputStream is = new BufferedInputStream(new FileInputStream(pcmFile));
                    while (isPlaying) {
                        int read = is.read(noteArray, 0, minBufferSize);
                        if (read > 0) {
                            play(noteArray, 0, minBufferSize);
                        } else {
                            isPlaying = false;
                        }
                    }
                    if (is != null) {
                        is.close();
                        is = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mThread.start();
    }


    public void stopPlay() {

        if (!isPlaying) {
            return;
        }

        isPlaying = false;

        try {
            mThread.interrupt();
            mThread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            mAudioTrack.stop();
        }
        mAudioTrack.release();

        Log.e("TAG", "停止播放成功");
    }

    public boolean play(byte[] audioData, int offsetInBytes, int sizeInBytes) {

        if (!isPlaying) {
            return false;
        }

        if (sizeInBytes < minBufferSize) {
            Log.e("TAG", "缓存数据长度不够");
            return false;
        }
        if (mAudioTrack.write(audioData, offsetInBytes, sizeInBytes) != sizeInBytes) {
            Log.e("TAG", "不能写入全部数据");
        }

        mAudioTrack.play();
        return true;
    }

}
