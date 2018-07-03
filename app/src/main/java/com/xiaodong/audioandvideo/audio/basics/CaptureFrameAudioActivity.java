package com.xiaodong.audioandvideo.audio.basics;

import android.os.Bundle;
import android.view.View;

import com.xiaodong.audioandvideo.R;
import com.xiaodong.audioandvideo.audio.utils.AudioCaptureUtils;
import com.xiaodong.audioandvideo.audio.utils.AudioPlayUtils;
import com.xiaodong.basetools.base.JBaseActivity;
import com.xiaodong.basetools.view.JButton;

import butterknife.InjectView;

/**
 * Created by xiaodong.jin on 2018/6/26.
 * description：采集一帧音频
 */
public class CaptureFrameAudioActivity extends JBaseActivity implements View.OnClickListener {


    @InjectView(R.id.start)
    JButton mStart;
    @InjectView(R.id.stop)
    JButton mStop;
    @InjectView(R.id.play)
    JButton mPlay;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_capture_frame_audio;
    }


    @Override
    protected void initValue(Bundle onSavedInstance) {
        super.initValue(onSavedInstance);
    }

    @Override
    protected void initWidget(Bundle onSavedInstance) {
        super.initWidget(onSavedInstance);
        setTitle("采集一帧音频");
        showOrHideBackButton(true);
    }

    @Override
    protected void initListener(Bundle onSavedInstance) {
        super.initListener(onSavedInstance);
        mStart.setOnClickListener(this);
        mStop.setOnClickListener(this);
        mPlay.setOnClickListener(this);
    }

    @Override
    protected void initData(Bundle onSavedInstance) {
        super.initData(onSavedInstance);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                AudioCaptureUtils.getInstance().startRecord();
                break;
            case R.id.stop:
                AudioPlayUtils.getInstance().stopPlay();
                AudioCaptureUtils.getInstance().stopRecord();
                AudioCaptureUtils.getInstance().convertWaveFile("");
                break;
            case R.id.play:
                AudioPlayUtils.getInstance().startPlay("test1");
                break;
        }
    }
}
