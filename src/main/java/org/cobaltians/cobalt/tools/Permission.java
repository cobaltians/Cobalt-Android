package org.cobaltians.cobalt.tools;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import org.cobaltians.cobalt.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by sebastien on 10/02/16.
 */
public final class Permission {

    /***********************************************************************************************
     *
     * LISTENER
     *
     **********************************************************************************************/

    public interface PermissionListener {

        int DENIED = -1;
        int DISABLED = -2;
        int GRANTED = 0;

        void onRequestPermissionResult(int requestCode, @NonNull String permission, int result);
    }

    /***********************************************************************************************
     *
     * REQUESTED PERMISSION
     *
     **********************************************************************************************/

    private static final class RequestedPermission {

        private final WeakReference<Activity> mActivity;
        private final int mRequestCode;
        private final String mPermission;
        private final PermissionListener mListener;

        RequestedPermission(WeakReference<Activity> activity, int requestCode, String permission, PermissionListener listener) {
            mActivity = activity;
            mRequestCode = requestCode;
            mPermission = permission;
            mListener = listener;
        }

        final Activity getActivity() {
            return mActivity.get();
        }

        final int getRequestCode() {
            return mRequestCode;
        }

        final String getPermission() {
            return mPermission;
        }

        final PermissionListener getListener() {
            return mListener;
        }
    }

    /***********************************************************************************************
     *
     * MEMBERS
     *
     **********************************************************************************************/

    private static Permission sInstance;

    private ArrayList<RequestedPermission> mRequestedPermissions = new ArrayList<>();

    /***********************************************************************************************
     *
     * CONSTRUCTORS
     *
     **********************************************************************************************/

    public static Permission getInstance() {
        if (sInstance == null) {
            sInstance = new Permission();
        }

        return sInstance;
    }

    /***********************************************************************************************
     *
     * METHODS
     *
     **********************************************************************************************/

    public final void requestPermissions(@NonNull final Activity activity,
                                         final int requestCode,
                                         @NonNull final String[] permissions,
                                         final String title,
                                         final String why,
                                         @NonNull final PermissionListener listener) {
        final ArrayList<String> permissionsToRequest = new ArrayList<>(permissions.length);
        for (String permission : permissions) {
            if (checkPermission(activity, permission)) {
                listener.onRequestPermissionResult(requestCode, permission, PermissionListener.GRANTED);
            }
            else {
                permissionsToRequest.add(permission);
            }
        }

        if (permissionsToRequest.size() > 0) {
            boolean shouldShowRationale = false;
            for (String permission : permissionsToRequest) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    shouldShowRationale = true;
                    break;
                }
            }

            if (shouldShowRationale) {
                if (title != null
                    || why != null) {
                    Resources res = activity.getResources();
                    new AlertDialog.Builder(activity)
                            .setTitle(title)
                            .setMessage(why)
                            .setPositiveButton(res.getString(R.string.settings), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    for (String permission : permissionsToRequest) {
                                        mRequestedPermissions.add(new RequestedPermission(new WeakReference<>(activity), requestCode, permission, listener));
                                    }

                                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + activity.getPackageName()));
                                    activity.startActivity(intent);
                                }
                            })
                            .setNegativeButton(res.getString(R.string.ignore), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    for (String permission : permissionsToRequest) {
                                        listener.onRequestPermissionResult(requestCode, permission, PermissionListener.DENIED);
                                    }
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
                else {
                    for (String permission : permissionsToRequest) {
                        listener.onRequestPermissionResult(requestCode, permission, PermissionListener.DISABLED);
                    }
                }
            }
            else {
                for (String permission : permissionsToRequest) {
                    mRequestedPermissions.add(new RequestedPermission(new WeakReference<>(activity), requestCode, permission, listener));
                }
                String[] permissionsArray = new String[permissionsToRequest.size()];
                permissionsToRequest.toArray(permissionsArray);
                ActivityCompat.requestPermissions(activity, permissionsArray, requestCode);
            }
        }
    }

    private boolean checkPermission(Activity activity, String permission) {
        return ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /***********************************************************************************************
     *
     * ACTIVITY LISTENERS
     *
     **********************************************************************************************/

    public final void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        int permissionsLength = permissions.length;
        for (int i = 0 ; i < permissionsLength ; i++) {
            String permission = permissions[i];

            ArrayList<RequestedPermission> permissionsResulted = new ArrayList<>();

            for (RequestedPermission requestedPermission : mRequestedPermissions) {
                if (requestedPermission.getPermission().equals(permission)
                    && requestedPermission.getRequestCode() == requestCode) {
                    switch(grantResults[i]) {
                        case PackageManager.PERMISSION_DENIED:
                            requestedPermission.getListener().onRequestPermissionResult(requestCode,permission, PermissionListener.DENIED);
                            break;
                        case PackageManager.PERMISSION_GRANTED:
                            requestedPermission.getListener().onRequestPermissionResult(requestCode, permission, PermissionListener.GRANTED);
                            break;
                    }

                    permissionsResulted.add(requestedPermission);
                }
            }

            mRequestedPermissions.removeAll(permissionsResulted);
        }
    }

    public final void onPostResume(@NonNull Activity activity) {
        ArrayList<RequestedPermission> permissionsResulted = new ArrayList<>();

        for (RequestedPermission requestedPermission : mRequestedPermissions) {
            Activity requestedActivity = requestedPermission.getActivity();

            if (activity.equals(requestedActivity)) {
                String permission = requestedPermission.getPermission();

                if (checkPermission(activity, permission)) {
                    requestedPermission.getListener().onRequestPermissionResult(requestedPermission.getRequestCode(), permission, PermissionListener.GRANTED);
                }
                else {
                    requestedPermission.getListener().onRequestPermissionResult(requestedPermission.getRequestCode(), permission, PermissionListener.DENIED);
                }

                permissionsResulted.add(requestedPermission);
            }
        }

        mRequestedPermissions.removeAll(permissionsResulted);
    }
}
