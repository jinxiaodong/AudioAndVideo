package com.xiaodong.audioandvideo.audio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.xiaodong.audioandvideo.R;
import com.xiaodong.audioandvideo.audio.basics.CaptureFrameAudioActivity;
import com.xiaodong.audioandvideo.utils.FormatDataUtils;
import com.xiaodong.audioandvideo.video.VideoActivity;
import com.xiaodong.basetools.base.JBaseActivity;
import com.xiaodong.basetools.base.ListAdapter;
import com.xiaodong.basetools.bean.BeanWraper;
import com.xiaodong.basetools.utils.SystemBarUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;

public class AudioActivity extends JBaseActivity {


    @InjectView(R.id.recycleview)
    RecyclerView mRecycleview;
    private ListAdapter mListAdapter;
    private List<BeanWraper> mList = new ArrayList<>();

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_main;
    }


    @Override
    protected void initValue(Bundle onSavedInstance) {
        super.initValue(onSavedInstance);
        setTitle("音频技术");
        mList.add(FormatDataUtils.getBeanWraper("采集一帧音频"));
        mList.add(FormatDataUtils.getBeanWraper("播放一帧音频"));

    }

    @Override
    protected void initWidget(Bundle onSavedInstance) {
        super.initWidget(onSavedInstance);
        SystemBarUtil.setChenJinTitle(getCommonHeader().getGuider(), mContext);
        mListAdapter = new ListAdapter(this, mList);
    }

    @Override
    protected void initListener(Bundle onSavedInstance) {
        super.initListener(onSavedInstance);
        mListAdapter.setOnButtonClick(new ListAdapter.onButtonClick() {
            @Override
            public void onBtnClick(View view, int position) {
                BeanWraper beanWraper = mList.get(position);
                Intent intent = null;
                switch (beanWraper.name) {
                    case "采集一帧音频":
                        intent = new Intent(AudioActivity.this, CaptureFrameAudioActivity.class);
                        break;
                    case "播放一帧音频":
                        intent = new Intent(AudioActivity.this, VideoActivity.class);

                        break;
                }
                if (intent == null) {
                    return;
                }
                startActivity(intent);
            }
        });

    }


    @Override
    protected void initData(Bundle onSavedInstance) {
        super.initData(onSavedInstance);


        mRecycleview.setLayoutManager(new LinearLayoutManager(mContext));
        mRecycleview.setAdapter(mListAdapter);
    }

}
