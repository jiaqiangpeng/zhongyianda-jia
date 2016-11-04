package com.alibaba.livecloud.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.alibaba.livecloud.R;
import com.alibaba.livecloud.live.AlivcMediaFormat;
import com.alibaba.livecloud.live.AlivcMediaRecorder;
import com.alibaba.livecloud.live.AlivcMediaRecorderFactory;
import com.alibaba.livecloud.live.AlivcRecordReporter;
import com.alibaba.livecloud.live.AlivcStatusCode;
import com.alibaba.livecloud.live.OnLiveRecordErrorListener;
import com.alibaba.livecloud.live.OnNetworkStatusListener;
import com.alibaba.livecloud.live.OnRecordStatusListener;
import com.alibaba.livecloud.utils.ToastUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//import com.duanqu.qupai.logger.RecordLoggerManager;

/**
 * author:杭州短趣网络传媒技术有限公司
 * date:2016/6/27
 * description:LiveCameraActivity
 */
public class LiveCameraActivity extends Activity implements SurfaceHolder.Callback{
    private static final String TAG = "AlivcLiveCameraActivity";

    public final static String URL = "url";
    public final static String VIDEO_RESOLUTION = "video_resolution";
    public final static String SCREENORIENTATION = "screen_orientation";
    public final static String FRONT_CAMERA_FACING = "front_camera_face";

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String[] permissionManifest = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    private SurfaceView _CameraSurface;
    private AlivcMediaRecorder mMediaRecorder;
    private AlivcRecordReporter mRecordReporter;


    //qpjia
    private MediaRecorder mediarecorder;// 录制视频的类
    private SurfaceHolder surfaceHolder;

    private Surface mPreviewSurface;
    private Map<String, Object> mConfigure = new HashMap<>();
    private boolean isRecording = false;
    private int mPreviewWidth = 0;
    private int mPreviewHeight = 0;
    private DataStatistics mDataStatistics = new DataStatistics(1000);

    public static void startActivity(Context context, String rtmpUrl, int videoResolution, boolean screenOrientation1, int cameraFacingFront) {
        Intent intent = new Intent(context, LiveCameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(URL, rtmpUrl);
        intent.putExtra(VIDEO_RESOLUTION, videoResolution);
        intent.putExtra(SCREENORIENTATION, screenOrientation1);
        intent.putExtra(FRONT_CAMERA_FACING, cameraFacingFront);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_camera);
        if (Build.VERSION.SDK_INT >= 23) {
            permissionCheck();
        }

        getExtraData();

        initView();

        setRequestedOrientation(screenOrientation ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //采集
        _CameraSurface = (SurfaceView) findViewById(R.id.camera_surface);
        _CameraSurface.getHolder().addCallback(_CameraSurfaceCallback);
        _CameraSurface.setOnTouchListener(mOnTouchListener);

        //对焦，缩放
        mDetector = new GestureDetector(_CameraSurface.getContext(), mGestureDetector);
        mScaleDetector = new ScaleGestureDetector(_CameraSurface.getContext(), mScaleGestureListener);

        mMediaRecorder = AlivcMediaRecorderFactory.createMediaRecorder();
        mMediaRecorder.init(this);
        mDataStatistics.setReportListener(mReportListener);

        /**
         * this method only can be called after mMediaRecorder.init(),
         * otherwise,  will return null;
         */
        mRecordReporter = mMediaRecorder.getRecordReporter();

        mDataStatistics.start();
        mMediaRecorder.setOnRecordStatusListener(mRecordStatusListener);
        mMediaRecorder.setOnNetworkStatusListener(mOnNetworkStatusListener);
        mMediaRecorder.setOnRecordErrorListener(mOnErrorListener);

        mConfigure.put(AlivcMediaFormat.KEY_CAMERA_FACING, cameraFrontFacing);
        mConfigure.put(AlivcMediaFormat.KEY_MAX_ZOOM_LEVEL, 3);
        mConfigure.put(AlivcMediaFormat.KEY_OUTPUT_RESOLUTION, resolution);
        mConfigure.put(AlivcMediaFormat.KEY_MAX_VIDEO_BITRATE, 800000);
        mConfigure.put(AlivcMediaFormat.KEY_DISPLAY_ROTATION, screenOrientation ? AlivcMediaFormat.DISPLAY_ROTATION_90 : AlivcMediaFormat.DISPLAY_ROTATION_0);
        mConfigure.put(AlivcMediaFormat.KEY_EXPOSURE_COMPENSATION, 20);//曝光度
//        btn_switch_beauty.setChecked(false);
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
//        pushUrl = FileUtil.getSDCardPath()+ File.separator+sdf.format(new Date(System.currentTimeMillis()))+".flv";

//        SurfaceHolder holder = _CameraSurface.getHolder();// 取得holder
//        holder.addCallback(this); // holder加入回调接口
//         //setType必须设置，要不出错.
//        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private String pushUrl;
    private int resolution;
    private boolean screenOrientation;
    private int cameraFrontFacing;

    private void getExtraData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            pushUrl = bundle.getString(URL);
            resolution = bundle.getInt(VIDEO_RESOLUTION);
            screenOrientation = bundle.getBoolean(SCREENORIENTATION);
            cameraFrontFacing = bundle.getInt(FRONT_CAMERA_FACING);
        }
    }

    private void permissionCheck() {
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (String permission : permissionManifest) {
            if (PermissionChecker.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionCheck = PackageManager.PERMISSION_DENIED;
            }
        }
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissionManifest, PERMISSION_REQUEST_CODE);
        }
    }

    private ToggleButton btn_live_push;
    private ToggleButton toggle_camera;
//    private ToggleButton btn_switch_beauty;
    private ToggleButton toggle_flash_light;

    private TextView tv_video_capture_fps;
    private TextView tv_audio_encoder_fps;
    private TextView tv_video_encoder_fps;
    private TextView tv_output_bitrate;
    private TextView tv_av_output_diff;
    private TextView tv_audio_out_fps;
    private TextView tv_video_output_fps;
    private TextView tv_video_delay_duration;
    private TextView tv_audio_delay_duration;
    private TextView tv_video_cache_frame_cnt;
    private TextView tv_audio_cache_frame_cnt;
    private TextView tv_video_cache_byte_size;
    private TextView tv_audio_cache_byte_size;
    private TextView tv_video_frame_discard_cnt;
    private TextView tv_audio_frame_discard_cnt;
    private TextView tv_cur_video_bueaty_duration;
    private TextView tv_cur_video_encoder_duration;

    private void initView() {
        btn_live_push = (ToggleButton) findViewById(R.id.toggle_live_push);
        btn_live_push.setOnCheckedChangeListener(_PushOnCheckedChange);

        toggle_camera = (ToggleButton) findViewById(R.id.toggle_camera);
        toggle_camera.setOnCheckedChangeListener(_CameraOnCheckedChange);

//        btn_switch_beauty = (ToggleButton) findViewById(R.id.btn_switch_beauty);
//        btn_switch_beauty.setOnCheckedChangeListener(_SwitchBeautyOnCheckedChange);

        toggle_flash_light = (ToggleButton) findViewById(R.id.toggle_flash_light);
        toggle_flash_light.setOnCheckedChangeListener(_SwitchFlashLightOnCheckedChange);

        tv_video_capture_fps = (TextView) findViewById(R.id.tv_video_capture_fps);
        tv_audio_encoder_fps = (TextView) findViewById(R.id.tv_audio_encoder_fps);
        tv_video_encoder_fps = (TextView) findViewById(R.id.tv_video_encoder_fps);
        tv_output_bitrate = (TextView) findViewById(R.id.tv_output_bitrate);
        tv_av_output_diff = (TextView) findViewById(R.id.tv_av_output_diff);
        tv_audio_out_fps = (TextView) findViewById(R.id.tv_audio_out_fps);
        tv_video_output_fps = (TextView) findViewById(R.id.tv_video_output_fps);
        tv_video_delay_duration = (TextView) findViewById(R.id.tv_video_delay_duration);
        tv_audio_delay_duration = (TextView) findViewById(R.id.tv_audio_delay_duration);
        tv_video_cache_frame_cnt = (TextView) findViewById(R.id.tv_video_cache_frame_cnt);
        tv_audio_cache_frame_cnt = (TextView) findViewById(R.id.tv_audio_cache_frame_cnt);
        tv_video_cache_byte_size = (TextView) findViewById(R.id.tv_video_cache_byte_size);
        tv_audio_cache_byte_size = (TextView) findViewById(R.id.tv_audio_cache_byte_size);
        tv_video_frame_discard_cnt = (TextView) findViewById(R.id.tv_video_frame_discard_cnt);
        tv_audio_frame_discard_cnt = (TextView) findViewById(R.id.tv_audio_frame_discard_cnt);
        tv_cur_video_bueaty_duration = (TextView) findViewById(R.id.tv_cur_video_bueaty_duration);
        tv_cur_video_encoder_duration = (TextView) findViewById(R.id.tv_cur_video_encoder_duration);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPreviewSurface != null) {
            mMediaRecorder.prepare(mConfigure, mPreviewSurface);
            Log.d("AlivcMediaRecorder", " onResume==== isRecording =" + isRecording + "=====");
            if (isRecording) {
                mMediaRecorder.startRecord(pushUrl);
            }
        }
    }

    @Override
    protected void onPause() {
        if (isRecording) {
            mMediaRecorder.stopRecord();
        }
        /**
         * 如果要调用stopRecord和reset()方法，则stopRecord（）必须在reset之前调用，否则将会抛出IllegalStateException
         */
        mMediaRecorder.reset();
        super.onPause();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("DEBUG", "onBackPressed");
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        RecordLoggerManager.closeLoggerFile();
        mDataStatistics.stop();
        mMediaRecorder.release();
    }

    private final CompoundButton.OnCheckedChangeListener _SwitchFlashLightOnCheckedChange =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mMediaRecorder.addFlag(AlivcMediaFormat.FALG_FALSH_MODE_ON);
                    } else {
                        mMediaRecorder.removeFlag(AlivcMediaFormat.FALG_FALSH_MODE_ON);
                    }
                }
            };

    private final CompoundButton.OnCheckedChangeListener _SwitchBeautyOnCheckedChange =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mMediaRecorder.addFlag(AlivcMediaFormat.FLAG_BEAUTY_ON);
                    } else {
                        mMediaRecorder.removeFlag(AlivcMediaFormat.FLAG_BEAUTY_ON);
                    }
                }
            };

    private final CompoundButton.OnCheckedChangeListener _CameraOnCheckedChange =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int currFacing = mMediaRecorder.switchCamera();
                    if (currFacing == AlivcMediaFormat.CAMERA_FACING_FRONT) {
                        mMediaRecorder.addFlag(AlivcMediaFormat.FLAG_BEAUTY_ON);
                    }
                    mConfigure.put(AlivcMediaFormat.KEY_CAMERA_FACING, currFacing);
                }
            };

    private final CompoundButton.OnCheckedChangeListener _PushOnCheckedChange =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        try {
                            //qpjia 录制本地视频
//                            LocalVideo();

                            mMediaRecorder.startRecord(pushUrl);
                            isRecording = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        mMediaRecorder.stopRecord();

                        //qpjia
//                        if (mediarecorder != null) {
//                            // 停止录制
//                            mediarecorder.stop();
//                            // 释放资源
//                            mediarecorder.release();
//                            mediarecorder = null;
//                        }
                    }
                }
            };

    private GestureDetector mDetector;
    private ScaleGestureDetector mScaleDetector;
    private GestureDetector.OnGestureListener mGestureDetector = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            if (mPreviewWidth > 0 && mPreviewHeight > 0) {
                float x = motionEvent.getX() / mPreviewWidth;
                float y = motionEvent.getY() / mPreviewHeight;
                mMediaRecorder.focusing(x, y);

            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }
    };

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mDetector.onTouchEvent(motionEvent);
            mScaleDetector.onTouchEvent(motionEvent);
            return true;
        }
    };

    private ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mMediaRecorder.setZoom(scaleGestureDetector.getScaleFactor(), null);
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        }
    };

    private final SurfaceHolder.Callback _CameraSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            holder.setKeepScreenOn(true);
            mPreviewSurface = holder.getSurface();

            mMediaRecorder.prepare(mConfigure, mPreviewSurface);
            if ((int) mConfigure.get(AlivcMediaFormat.KEY_CAMERA_FACING) == AlivcMediaFormat.CAMERA_FACING_FRONT) {
                mMediaRecorder.addFlag(AlivcMediaFormat.FLAG_BEAUTY_ON);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mMediaRecorder.setPreviewSize(width, height);
            mPreviewWidth = width;
            mPreviewHeight = height;
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mPreviewSurface = null;
            mMediaRecorder.stopRecord();
            mMediaRecorder.reset();
        }
    };


    private OnRecordStatusListener mRecordStatusListener = new OnRecordStatusListener() {
        @Override
        public void onDeviceAttach() {

        }

        @Override
        public void onDeviceAttachFailed(int facing) {

        }

        @Override
        public void onSessionAttach() {
            if (isRecording && !TextUtils.isEmpty(pushUrl)) {
                mMediaRecorder.startRecord(pushUrl);
            }
            mMediaRecorder.focusing(0.5f, 0.5f);
        }

        @Override
        public void onSessionDetach() {

        }

        @Override
        public void onDeviceDetach() {

        }

        @Override
        public void onIllegalOutputResolution() {
            Log.d(TAG, "selected illegal output resolution");
            ToastUtils.showToast(LiveCameraActivity.this, R.string.illegal_output_resolution);
        }
    };


    private OnNetworkStatusListener mOnNetworkStatusListener = new OnNetworkStatusListener() {
        @Override
        public void onNetworkBusy() {
            Toast toast = Toast.makeText(LiveCameraActivity.this,
                    "当前网络状态极差，已无法正常流畅直播，确认要继续直播吗？", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
            TextView text = (TextView) toast.getView().findViewById(android.R.id.message);
            text.setGravity(Gravity.CENTER);
            toast.show();
            Log.d("LiveRecord", "=== onNetworkBusy ===");
        }

        @Override
        public void onNetworkFree() {
            Log.d(TAG, "=== onNetworkFree ===");
        }

        @Override
        public void onConnectionStatusChange(int status) {
            switch (status) {
                case AlivcStatusCode.STATUS_CONNECTION_START:
                    ToastUtils.showToast(LiveCameraActivity.this, "Start live stream connection!");
                    Log.d("LiveRecord", "=== AlivcStatusCode.STATUS_CONNECTION_START ===");
                    break;
                case AlivcStatusCode.STATUS_CONNECTION_ESTABLISHED:
                    Log.d("LiveRecord", "=== AlivcStatusCode.STATUS_CONNECTION_ESTABLISHED ===");
                    ToastUtils.showToast(LiveCameraActivity.this, "Live stream connection is established!");
                    break;
                case AlivcStatusCode.STATUS_CONNECTION_CLOSED:
                    Log.d("LiveRecord", "=== AlivcStatusCode.STATUS_CONNECTION_CLOSED ===");
                    ToastUtils.showToast(LiveCameraActivity.this, "Live stream connection is closed!");
                    mMediaRecorder.stopRecord();
                    break;
            }
        }

        @Override
        public boolean onNetworkReconnect() {
            return true;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        int toastTip = 0;
                        if (Manifest.permission.CAMERA.equals(permissions[i])) {
                            toastTip = R.string.no_camera_permission;
                        } else if (Manifest.permission.RECORD_AUDIO.equals(permissions[i])) {
                            toastTip = R.string.no_record_audio_permission;
                        }
                        if (toastTip != 0) {
                            Toast.makeText(this, toastTip, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
    }

    private OnLiveRecordErrorListener mOnErrorListener = new OnLiveRecordErrorListener() {
        @Override
        public void onError(int errorCode) {
            switch (errorCode) {
                case AlivcStatusCode.ERROR_AUTH_FAILED:
                    Log.i("LiveRecord", "==== live auth failed, need new auth key ====");
                    ToastUtils.showToast(LiveCameraActivity.this, "live auth failed, need new auth key");
                    break;
                case AlivcStatusCode.ERROR_SERVER_CLOSED_CONNECTION:
                case AlivcStatusCode.ERORR_OUT_OF_MEMORY:
                case AlivcStatusCode.ERROR_CONNECTION_TIMEOUT:
                case AlivcStatusCode.ERROR_BROKEN_PIPE:
                case AlivcStatusCode.ERROR_ILLEGAL_ARGUMENT:
                case AlivcStatusCode.ERROR_IO:
                case AlivcStatusCode.ERROR_NETWORK_UNREACHABLE:
                case AlivcStatusCode.ERROR_OPERATION_NOT_PERMITTED:
                default:
                    Log.i("LiveRecord", "=== Live stream connection error-->" + errorCode + " ===");
                    ToastUtils.showToast(LiveCameraActivity.this, "Live stream connection error-->" + errorCode);
                    break;
            }
        }
    };

    DataStatistics.ReportListener mReportListener = new DataStatistics.ReportListener() {
        @Override
        public void onInfoReport() {
            runOnUiThread(mLoggerReportRunnable);
        }
    };

    private Runnable mLoggerReportRunnable = new Runnable() {
        @Override
        public void run() {
            if (mRecordReporter != null) {
                tv_video_capture_fps.setText(mRecordReporter.getInt(AlivcRecordReporter.VIDEO_CAPTURE_FPS) + "fps");
                tv_audio_encoder_fps.setText(mRecordReporter.getInt(AlivcRecordReporter.AUDIO_ENCODER_FPS) + "fps");
                tv_video_encoder_fps.setText(mRecordReporter.getInt(AlivcRecordReporter.VIDEO_ENCODER_FPS) + "fps");

                /**
                 * OUTPUT_BITRATE的单位是byte / s，所以转换成bps需要要乘8
                 */
                tv_output_bitrate.setText(mRecordReporter.getLong(AlivcRecordReporter.OUTPUT_BITRATE) * 8 + "bps");

                tv_av_output_diff.setText(mRecordReporter.getLong(AlivcRecordReporter.AV_OUTPUT_DIFF) + "microseconds");
                tv_audio_out_fps.setText(mRecordReporter.getInt(AlivcRecordReporter.AUDIO_OUTPUT_FPS) + "fps");
                tv_video_output_fps.setText(mRecordReporter.getInt(AlivcRecordReporter.VIDEO_OUTPUT_FPS) + "fps");
//                tv_stream_publish_time = (TextView) findViewById(R.id.tv_video_capture_fps);
//                tv_stream_server_ip = (TextView) findViewById(R.id.tv_video_capture_fps);
                tv_video_delay_duration.setText(mRecordReporter.getLong(AlivcRecordReporter.VIDEO_DELAY_DURATION) + "microseconds");
                tv_audio_delay_duration.setText(mRecordReporter.getLong(AlivcRecordReporter.AUDIO_DELAY_DURATION) + "microseconds");
                tv_video_cache_frame_cnt.setText(mRecordReporter.getInt(AlivcRecordReporter.VIDEO_CACHE_FRAME_CNT) + "");
                tv_audio_cache_frame_cnt.setText(mRecordReporter.getInt(AlivcRecordReporter.AUDIO_CACHE_FRAME_CNT) + "");
                tv_video_cache_byte_size.setText(mRecordReporter.getLong(AlivcRecordReporter.VIDEO_CACHE_BYTE_SIZE) + "byte");
                tv_audio_cache_byte_size.setText(mRecordReporter.getLong(AlivcRecordReporter.AUDIO_CACHE_BYTE_SIZE) + "byte");
                tv_video_frame_discard_cnt.setText(mRecordReporter.getInt(AlivcRecordReporter.VIDEO_FRAME_DISCARD_CNT) + "");
                tv_audio_frame_discard_cnt.setText(mRecordReporter.getInt(AlivcRecordReporter.AUDIO_FRAME_DISCARD_CNT) + "");
                tv_cur_video_bueaty_duration.setText(mRecordReporter.getLong(AlivcRecordReporter.CUR_VIDEO_BEAUTY_DURATION) + "ms");
                tv_cur_video_encoder_duration.setText(mRecordReporter.getLong(AlivcRecordReporter.CUR_VIDEO_ENCODER_DURATION) + "ms");
            }
        }
    };


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceHolder = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        surfaceHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mediarecorder = null;
    }

    private void LocalVideo() {
        //qpjia
        mediarecorder = new MediaRecorder();
        mediarecorder.reset();
        // 设置录制视频源为Camera(相机)
        mediarecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        mediarecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        // 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
        mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        // 设置录制的视频编码h263 h264
        mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        // 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
        mediarecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
         //设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错
        mediarecorder.setVideoFrameRate(20);
        mediarecorder.setPreviewDisplay(surfaceHolder.getSurface());

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);
        // 设置视频文件输出的路径
        mediarecorder.setOutputFile("/sdcard/" + str + ".3gp");
        try {
            // 准备录制
            mediarecorder.prepare();
            // 开始录制
            mediarecorder.start();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
