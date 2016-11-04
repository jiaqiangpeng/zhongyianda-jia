package com.alibaba.livecloud.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.livecloud.R;
import com.alibaba.livecloud.adapter.LiveCommentAdapter;
import com.alibaba.view.BubblingView;

/**
 * Created by liujianghao on 16-7-5.
 */
public class BubblingAnimationActivity extends Activity implements View.OnClickListener{

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, BubblingAnimationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private BubblingView mBubblingView;
    private RecyclerView mRecyclerView;
    private EditText mEtComment;

    private int[] images = {
            R.drawable.heart0,
            R.drawable.heart1,
            R.drawable.heart2,
            R.drawable.heart3,
            R.drawable.heart4,
            R.drawable.heart5,
            R.drawable.heart6,
            R.drawable.heart7,
            R.drawable.heart8
    };
    int i = 0;
    AnimThread mAnimThread;
    LiveCommentAdapter mAdapter;
    LinearLayoutManager mCommentManager;

    Rect mRect = new Rect();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_animation);
        mBubblingView = (BubblingView) findViewById(R.id.bubbling_view);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mEtComment = (EditText) findViewById(R.id.et_comment);
//        mAnimThread = new AnimThread();
//        mAnimThread.start();

        mAdapter = new LiveCommentAdapter();
        mCommentManager = new LinearLayoutManager(this);
        mCommentManager.setStackFromEnd(true);
        mCommentManager.setSmoothScrollbarEnabled(true);
        mEtComment.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEND:
                        String comment = mEtComment.getText().toString();
                        if(!TextUtils.isEmpty(comment)) {
                            mAdapter.addComment(comment);
                            mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
                            mEtComment.setText("");
                        }

                        break;
                }
                return false;
            }
        });
        mRecyclerView.setLayoutManager(mCommentManager);
        mRecyclerView.setAdapter(mAdapter);
        mBubblingView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bubbling_view:
                mBubblingView.addBubblingItem(images[i++%5]);
                break;
        }
    }

    class AnimThread extends Thread{
        @Override
        public void run() {
            super.run();
            while (true) {
                mBubblingView.post(new Runnable() {
                    @Override
                    public void run() {
                        mBubblingView.addBubblingItem(images[i++%5]);
                    }
                });
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        public void cancel() {
            interrupt();
            try {
                join();
            } catch (InterruptedException e) {
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mAnimThread.cancel();
    }
}
