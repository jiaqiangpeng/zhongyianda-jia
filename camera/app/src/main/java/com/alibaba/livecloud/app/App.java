package com.alibaba.livecloud.app;

import android.app.Application;

import com.duanqu.qupai.jni.ApplicationGlue;

/**
 * author:杭州短趣网络传媒技术有限公司
 * date:2016/6/27
 * description:DemoActivity
 */
public class App extends Application {

//    @Override
//    protected void attachBaseContext(Context base) {
//        super.attachBaseContext(base);
//        MultiDex.install(this);
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.loadLibrary("gnustl_shared");
        System.loadLibrary("qupai-media-thirdparty");
        System.loadLibrary("alivc-media-jni");

        ApplicationGlue.initialize(this);
    }
}
