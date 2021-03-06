package com.ksdigtalnomad.koala.ui.views.home.tabs.settings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ksdigtalnomad.koala.R;
import com.ksdigtalnomad.koala.data.AlarmDailyController;
import com.ksdigtalnomad.koala.data.models.User;
import com.ksdigtalnomad.koala.data.models.shareMessage.ShareMessage;
import com.ksdigtalnomad.koala.databinding.FragmentTabSettingsBinding;

import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.ksdigtalnomad.koala.ui.base.BaseFragment;
import com.ksdigtalnomad.koala.ui.customView.calendarView.utils.DateHelper;
import com.ksdigtalnomad.koala.ui.views.dialogs.CustomTimePickerDialog;
import com.ksdigtalnomad.koala.helpers.data.FBEventLogHelper;
import com.ksdigtalnomad.koala.helpers.data.LanguageHelper;
import com.ksdigtalnomad.koala.helpers.util.PlayStoreHelper;
import com.ksdigtalnomad.koala.helpers.data.FBRemoteConfigHelper;
import com.ksdigtalnomad.koala.helpers.data.PreferenceHelper;
import com.ksdigtalnomad.koala.helpers.util.ShareHelper;
import com.ksdigtalnomad.koala.helpers.ui.ToastHelper;
import com.ksdigtalnomad.koala.ui.views.user.AccountActivity;
import com.ksdigtalnomad.koala.ui.views.user.LoginActivity;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class TabSettingsFragment extends BaseFragment {

    private FragmentTabSettingsBinding mBinding;

    private Context mContext;

    public static TabSettingsFragment newInstance(){
        TabSettingsFragment fragment = new TabSettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext= getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_tab_settings, container, false);
        mBinding.setFragment(this);

        mBinding.tvUserLoginInfo.setPaintFlags(mBinding.tvUserLoginInfo.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        try {
            PackageInfo info = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            mBinding.version.setText("ver " + info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        boolean isPushEnabled = PreferenceHelper.isAlarmDailyEnabled();
        mBinding.alarmDailySwitch.setChecked(isPushEnabled);
        mBinding.alarmDailySwitch.setOnCheckedChangeListener((CompoundButton compoundButton, boolean isChecked) -> {

            String todayStr = DateHelper.getInstance().getTodayStr("yyyy.MM.dd.");

            ToastHelper.writeBottomLongToast(getResources().getString(R.string.warning_push_agree, todayStr, getResources().getString(isChecked ? R.string.prefix_agree : R.string.prefix_disagree)));

            // 동의 설정
            PreferenceHelper.setAlarmDailyEnabled(isChecked);

            // 시간 설정 레이아웃 Disable
            setPushTimeLayoutEnabled(isChecked);

            // 로깅
            FBEventLogHelper.onAlarmDailyAgree(isChecked, todayStr);
        });
        setPushTimeLayoutEnabled(isPushEnabled);

        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(PreferenceHelper.isLogin()){
            User user = PreferenceHelper.getUser();
            mBinding.tvUserEmail.setText(user.getEmail());
            mBinding.tvUserLoginInfo.setText(getResources().getString(R.string.title_account));
        }else{
            mBinding.tvUserEmail.setText(getResources().getString(R.string.fragment_tab_settings_info_user_guest));
            mBinding.tvUserLoginInfo.setText(getResources().getString(R.string.fragment_tab_settings_info_user_login));
        }
    }

    private void setPushTimeLayoutEnabled(boolean isChecked){
        mBinding.pushTimeLayout.setEnabled(isChecked);
        mBinding.pushTimeLayout.setAlpha(isChecked ? 1f : 0.5f);
    }


    // OnClick
    public void onLoginInfoClick(){

        if(PreferenceHelper.isLogin()){
            // @TODO:
            startActivity(AccountActivity.intent(getContext()));
        }else{
            startActivity(LoginActivity.intent(getContext()));
        }

    }
    public void onKakaoOpenChatRoomClick(){
        String chatRoomUrl = FBRemoteConfigHelper.getInstance().getKakaoOpenChatRoomUrl();
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(chatRoomUrl)));

        FBEventLogHelper.onKakaoOpenChatRoom();
    }
    public void onPushAgreeClick(){
        mBinding.alarmDailySwitch.setChecked(!mBinding.alarmDailySwitch.isChecked());
    }
    public void onPushTimeClick(){
        CustomTimePickerDialog customTimePickerDialog = CustomTimePickerDialog.newInstance(PreferenceHelper.getAlarmDailyHour(), PreferenceHelper.getAlarmDailyMinute());
        customTimePickerDialog.setDialogListener((boolean isTimeSet, int hour, int minute)->{
            if(!isTimeSet) return;

            PreferenceHelper.setAlarmDailyHour(hour);
            PreferenceHelper.setAlarmDailyMinute(minute);

            AlarmDailyController.setAndStartAlarm();

            String settingTime = PreferenceHelper.getAlarmDailySettingTimeStr();
            FBEventLogHelper.onAlarmDailyTime(settingTime);
            mBinding.bodyLayout.postDelayed(() -> ToastHelper.writeBottomLongToast(getResources().getString(R.string.warning_push_time, settingTime)), 100);
        });

        customTimePickerDialog.show(getActivity().getFragmentManager(), "CustomTimePickerDialog");

    }
    public void onCalendarDesignClick(){
        mBinding.viewBadge.onClick();
        startActivityForResult(SettingCalendarDesignActivity.intent(getActivity()), 0);
    }
    public void onOpenPlayStoreComplementClick(){
        PlayStoreHelper.openMyAppInPlayStore(getActivity());
        FBEventLogHelper.onPlayStoreComplement();
    }
    public void onShareClick(){
        ArrayList<ShareMessage> shareMessageList = new Gson().fromJson(FBRemoteConfigHelper.getInstance().getShareMessage(), new TypeToken<ArrayList<ShareMessage>>(){}.getType());

        ShareMessage shareMessage = ShareMessage.builder().build();

        if(shareMessageList != null && shareMessageList.size() > 0) {
            for(ShareMessage item: shareMessageList){
                if(LanguageHelper.isSameLanguage(item.getLangCode())){
                    shareMessage = item;
                    break;
                }
            }
        }

        ShareHelper.startShareIntent(getContext(), shareMessage.getContent());
        FBEventLogHelper.onAppShare();

    }
    public void onVersionClick(){  }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            /** 앱 재시작 */
            Intent i = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(i);
        }
    }
}
