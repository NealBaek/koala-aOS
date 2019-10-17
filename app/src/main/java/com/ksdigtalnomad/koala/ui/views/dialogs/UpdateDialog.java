package com.ksdigtalnomad.koala.ui.views.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.ksdigtalnomad.koala.R;
import com.ksdigtalnomad.koala.ui.base.BaseDialogFragment;

public class UpdateDialog extends BaseDialogFragment {
    private static final String KEY_TO_ADD = "KEY_TO_ADD";

    private CompleteClickListener completeClickListener;

    private EditText editText;
    private String originalName;

    private static UpdateDialog newInstance() {
        return new UpdateDialog();
    }

    public static UpdateDialog newInstance(String text) {
        UpdateDialog dialog = newInstance();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TO_ADD, text);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, this, R.layout.dialog_update);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        originalName = getArguments().getString(KEY_TO_ADD);

//        mBinding.searchEt.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
//            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//                ToastHelper.writeBottomLongToast("검색!");
//            }
//            return false;
//        });

        editText = view.findViewById(R.id.textTF);
        editText.setText(originalName);

        editText.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                completeClickListener.onClick(); dismiss();
            }
            return false;
        });
        view.findViewById(R.id.btnUpdate).setOnClickListener((v)->{
            completeClickListener.onClick(); dismiss();
        });
        view.findViewById(R.id.btnCancel).setOnClickListener((v)->{
            dismiss();
        });
        view.findViewById(R.id.btnDelete).setOnClickListener((v)->{
            DeleteDialog dialog = DeleteDialog.newInstance(originalName);
            dialog.setDialogListener(()->{
                // 삭제 처리
            });
            dialog.show(getActivity().getFragmentManager(), "Delete Dialog");
        });
    }

    public void setDialogListener(CompleteClickListener completeClickListener) {
        this.completeClickListener = completeClickListener;
    }

    public interface CompleteClickListener {
        void onClick();
    }
}
