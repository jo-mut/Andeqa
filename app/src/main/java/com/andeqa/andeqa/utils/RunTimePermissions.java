package com.andeqa.andeqa.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;

import com.andeqa.andeqa.R;

import java.util.ArrayList;

public class RunTimePermissions extends Activity {


    private Activity activity;
    private ArrayList<PermissionBean> arrayListPermission;
    private String[] arrayPermissions;
    private RunTimePermissionListener runTimePermissionListener;

    public RunTimePermissions(Activity activity)
    {
        this.activity = activity;
    }

    public class PermissionBean
    {

        String permission;
        boolean isAccept;
    }

    public void requestPermission(String[] permissions, RunTimePermissionListener runTimePermissionListener)
    {
        this.runTimePermissionListener = runTimePermissionListener;
        arrayListPermission = new ArrayList<PermissionBean>();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            for (int i = 0; i < permissions.length; i++)
            {
                PermissionBean permissionBean = new PermissionBean();
                if (ContextCompat.checkSelfPermission(activity, permissions[i]) == PackageManager.PERMISSION_GRANTED)
                {
                    permissionBean.isAccept = true;
                }
                else
                {
                    permissionBean.isAccept = false;
                    permissionBean.permission = permissions[i];
                    arrayListPermission.add(permissionBean);
                }


            }

            if (arrayListPermission.size() <= 0)
            {
                runTimePermissionListener.permissionGranted();
                return;
            }
            arrayPermissions = new String[arrayListPermission.size()];
            for (int i = 0; i < arrayListPermission.size(); i++)
            {
                arrayPermissions[i] = arrayListPermission.get(i).permission;
            }
            activity.requestPermissions(arrayPermissions, 10);
        }
        else
        {
            if (runTimePermissionListener != null)
            {
                runTimePermissionListener.permissionGranted();
            }
        }
    }

    public interface RunTimePermissionListener
    {

        void permissionGranted();

        void permissionDenied();
    }

    private void callSettingActivity()
    {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);

    }

    private void checkUpdate()
    {
        boolean isGranted = true;
        int deniedCount = 0;
        for (int i = 0; i < arrayListPermission.size(); i++)
        {
            if (!arrayListPermission.get(i).isAccept)
            {
                isGranted = false;
                deniedCount++;
            }
        }

        if (isGranted)
        {
            if (runTimePermissionListener != null)
            {
                runTimePermissionListener.permissionGranted();
            }
        }
        else
        {
            if (runTimePermissionListener != null)
            {

                runTimePermissionListener.permissionDenied();
            }
        }
    }


    private void updatePermissionResult(String permissions, int grantResults)
    {

        for (int i = 0; i < arrayListPermission.size(); i++)
        {
            if (arrayListPermission.get(i).permission.equals(permissions))
            {
                if (grantResults == 0)
                {
                    arrayListPermission.get(i).isAccept = true;
                }
                else
                {
                    arrayListPermission.get(i).isAccept = false;
                }
                break;
            }
        }

    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        for (int i = 0; i < permissions.length; i++)
        {
            updatePermissionResult(permissions[i], grantResults[i]);
        }
        checkUpdate();
    }


}
