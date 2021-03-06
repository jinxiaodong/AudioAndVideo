package com.xiaodong.audioandvideo;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.xiaodong.audioandvideo.audio.AudioActivity;
import com.xiaodong.audioandvideo.utils.FormatDataUtils;
import com.xiaodong.audioandvideo.video.VideoActivity;
import com.xiaodong.basetools.base.JBaseActivity;
import com.xiaodong.basetools.base.ListAdapter;
import com.xiaodong.basetools.bean.BeanWraper;
import com.xiaodong.basetools.utils.SystemBarUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;

public class MainActivity extends JBaseActivity {

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
        setTitle("Android音视频技术学习");
        mList.add(FormatDataUtils.getBeanWraper("audio"));
        mList.add(FormatDataUtils.getBeanWraper("video"));

    }

    @Override
    protected void initWidget(Bundle onSavedInstance) {
        super.initWidget(onSavedInstance);
        SystemBarUtil.setChenJinTitle(getCommonHeader().getGuider(), mContext);
        /*禁用页面滑动退出*/
        setEnableBackLayout(false);

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
                    case "audio":
                        intent = new Intent(MainActivity.this, AudioActivity.class);
                        break;
                    case "video":
                        intent = new Intent(MainActivity.this, VideoActivity.class);

                        break;
                }
                if(intent ==null) {
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
