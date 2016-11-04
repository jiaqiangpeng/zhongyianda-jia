package com.alibaba.livecloud.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.livecloud.R;
/**
 * author:杭州短趣网络传媒技术有限公司
 * date:2016/6/27
 * description:DemoActivity
 */
public class ToastUtils {

    public static Toast toast;

    public ToastUtils() {
    }

    public static void showToast(Context context, String text) {
        View toastRoot = LayoutInflater.from(context).inflate(R.layout.qupai_common_toast_default_layout, null, false);
        TextView message = (TextView) toastRoot.findViewById(R.id.toast_info);
        message.setText(text);
        if (toast != null) {
            toast.cancel();
            toast = null;
        }


        toast = new Toast(context);
        toast.setView(toastRoot);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

    public static void showToast(Context context, int resID) {
        showToast(context, context.getString(resID));
    }

}
