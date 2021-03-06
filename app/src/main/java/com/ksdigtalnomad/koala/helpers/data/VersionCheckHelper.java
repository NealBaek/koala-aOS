package com.ksdigtalnomad.koala.helpers.data;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.ksdigtalnomad.koala.data.models.AppVersion;

public class VersionCheckHelper {

    public static void getUpdateState(AppVersion appVersion, Activity activity, CompleteListener listener) {
        PackageInfo info = null;
        try {
            info = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (info != null) {
            String[] buildVersions = info.versionName.split("\\.");
            int bMajorNum = Integer.parseInt(buildVersions[0]);
            int bMinorNum = Integer.parseInt(buildVersions[1]);
            int bPatchNum = Integer.parseInt(buildVersions[2]);

            int tMajorNum = appVersion.getMajor();
            int tMinorNum = appVersion.getMinor();
            int tPatchNum = appVersion.getPatch();

            Log.d("ABC", "빌드 버전: " + bMajorNum + "." + bMinorNum + "." + bPatchNum);

            if (bMajorNum < tMajorNum){ showMustUpdate(activity); return; }
            else if (bMajorNum > tMajorNum){ listener.onComplete(); return; }
            else if (bMinorNum < tMinorNum){ showNeedUpdate(activity, listener); return; }
            else if (bMinorNum > tMinorNum){ listener.onComplete(); return; }
            else if (bPatchNum < tPatchNum){ showNeedUpdate(activity, listener); return; }
        }

        listener.onComplete();
    }

    private static void showMustUpdate(Activity activity) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setMessage("새 버전이 있습니다.\n업데이트를 해야만 앱을 실행할 수 있습니다.")
                .setCancelable(false)
                .setPositiveButton("업데이트", (dialog, which) -> startMarket(activity))
                .setNegativeButton("종료", (dialog, which) -> activity.finish())
                .create();
        alertDialog.show();
    }

    private static void showNeedUpdate(Activity activity, CompleteListener listener) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setMessage("새 버전이 있습니다.\n지금 업데이트 하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("업데이트", (dialog, which) -> startMarket(activity))
                .setNegativeButton("취소", (dialog, which) -> listener.onComplete())
                .create();
        alertDialog.show();
    }

    private static void startMarket(Activity activity) {
        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + activity.getPackageName())));
        activity.finish();
    }


    public interface CompleteListener{
//        void onPositive();
        void onComplete();
    }
}
