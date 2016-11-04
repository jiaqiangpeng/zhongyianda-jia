package com.alibaba.livecloud.demo;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;



import com.alibaba.livecloud.R;
import com.alibaba.livecloud.live.AlivcMediaFormat;
import com.alibaba.livecloud.utils.ToastUtils;

import com.avos.avoscloud.AVCloud;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.FunctionCallback;

import java.util.HashMap;

public class DemoActivity extends Activity implements View.OnClickListener,RadioGroup.OnCheckedChangeListener{
    private EditText urlET;
    private Button connectBT;
    private Button bubblingView;
    private RadioGroup resolutionCB;
    private RadioButton resolution240button;
    private RadioButton resolution360button;
    private RadioButton resolution480button;
    private RadioButton resolution540button;
    private RadioButton resolution720button;
    private RadioGroup rotationGroup;
    private RadioButton screenOrientation1;
    private RadioButton screenOrientation2;
    private CheckBox frontCameraMirror;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity);

        AVOSCloud.initialize(this, "mvTCALjzGq3jKEGG5i3thpda-gzGzoHsz", "8PyH67o0uzilz09NGGnwWSGD");

        connectBT = (Button) findViewById(R.id.connectBT);
        connectBT.setOnClickListener(this);
        bubblingView = (Button) findViewById(R.id.bubbling_view);
        bubblingView.setOnClickListener(this);
        urlET = (EditText) findViewById(R.id.rtmpUrl);


        resolutionCB = (RadioGroup) findViewById(R.id.resolution_group);
        resolution240button = (RadioButton) findViewById(R.id.radiobutton0);
        resolution360button = (RadioButton) findViewById(R.id.radiobutton1);
        resolution480button = (RadioButton) findViewById(R.id.radiobutton2);
        resolution540button = (RadioButton) findViewById(R.id.radiobutton3);
        resolution720button = (RadioButton) findViewById(R.id.radiobutton4);
        rotationGroup =(RadioGroup)findViewById(R.id.rotation_group);
        screenOrientation1 = (RadioButton) findViewById(R.id.screenOrientation1);
        screenOrientation2 = (RadioButton) findViewById(R.id.screenOrientation2);
        frontCameraMirror = (CheckBox) findViewById(R.id.front_camera_mirror);
        resolutionCB.setOnCheckedChangeListener(this);
        rotationGroup.setOnCheckedChangeListener(this);




    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()){
            case R.id.connectBT:
                int videoResolution = 0;
                int cameraFrontFacing = 0;
                boolean screenOrientation = false;
                if(resolution240button.isChecked()){
                    videoResolution = AlivcMediaFormat.OUTPUT_RESOLUTION_240P;
                }else if (resolution360button.isChecked()) {
                    videoResolution = AlivcMediaFormat.OUTPUT_RESOLUTION_360P;
                } else if (resolution480button.isChecked()) {
                    videoResolution = AlivcMediaFormat.OUTPUT_RESOLUTION_480P;
                } else if (resolution540button.isChecked()) {
                    videoResolution = AlivcMediaFormat.OUTPUT_RESOLUTION_540P;
                } else if (resolution720button.isChecked()) {
                    videoResolution = AlivcMediaFormat.OUTPUT_RESOLUTION_720P;
                }

                if(frontCameraMirror.isChecked()){
                    cameraFrontFacing = AlivcMediaFormat.CAMERA_FACING_FRONT;
                }else {
                    cameraFrontFacing = AlivcMediaFormat.CAMERA_FACING_BACK;
                }

                if (screenOrientation1.isChecked()){
                    screenOrientation = true;
                }else {
                    screenOrientation = false;
                }

                if(TextUtils.isEmpty(urlET.getText())){
                    ToastUtils.showToast(v.getContext(),"Push url is null");
                    return;
                }



                LiveCameraActivity.startActivity(v.getContext(),urlET.getText().toString(),videoResolution,screenOrientation,cameraFrontFacing);


                break;
            case R.id.bubbling_view:

                HashMap<String, Object> parameters = new HashMap<String, Object>();
                String i=AVInstallation.getCurrentInstallation().getInstallationId();
                parameters.put("installationId", i);
                parameters.put("requestedBy", "camera");


                AVCloud.callFunctionInBackground("streamURLs", parameters, new FunctionCallback() {
                            public void done(Object object, AVException e) {
                                if (e == null) {
                                    HashMap<String, Object> URLs= (HashMap<String, Object>) object;
                                    //"rtmp://video-center.alivecdn.com/schoolbus/5799cf716be3ff0065cbdf98?vhost=live.gaidonghai.com&auth_key=1475698819-0-0-5f0fe51c72edc0988d015963d5d32c92");
                                    urlET.setText(URLs.get("push").toString());
                                } else {
                                    ToastUtils.showToast(v.getContext(),e.toString());
                                }
                            }
                        });




                break;
        }
    }
}
