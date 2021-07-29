package net.jejer.hipda.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.ui.MainFrameActivity;
import net.jejer.hipda.ui.adapter.KeyValueArrayAdapter;
import net.jejer.hipda.utils.UIUtils;

/**
 * dialog for login
 * Created by GreenSkinMonster on 2015-04-18.
 */
public class LoginDialog extends Dialog {

    private MainFrameActivity mActivity;

    public LoginDialog(MainFrameActivity activity) {
        super(activity);
        mActivity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_login, null);

        final EditText etUsername = (EditText) view.findViewById(R.id.login_username);
        final EditText etPassword = (EditText) view.findViewById(R.id.login_password);
        final Spinner spSecQuestion = (Spinner) view.findViewById(R.id.login_question);
        final EditText etSecAnswer = (EditText) view.findViewById(R.id.login_answer);

        final KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(mActivity, R.layout.spinner_row);
        adapter.setEntryValues(mActivity.getResources().getStringArray(R.array.pref_login_question_list_values));
        adapter.setEntries(mActivity.getResources().getStringArray(R.array.pref_login_question_list_titles));
        spSecQuestion.setAdapter(adapter);

        etUsername.setText(HiSettingsHelper.getInstance().getUsername());
        etPassword.setText(HiSettingsHelper.getInstance().getPassword());
        if (!TextUtils.isEmpty(HiSettingsHelper.getInstance().getSecQuestion())
                && TextUtils.isDigitsOnly(HiSettingsHelper.getInstance().getSecQuestion())) {
            int idx = Integer.parseInt(HiSettingsHelper.getInstance().getSecQuestion());
            if (idx > 0 && idx < adapter.getCount())
                spSecQuestion.setSelection(idx);
        }
        etSecAnswer.setText(HiSettingsHelper.getInstance().getSecAnswer());

        Button btnLogin = (Button) view.findViewById(R.id.login_btn);
        btnLogin.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {

                if (etUsername.getText().toString().length() < 3
                        || etPassword.getText().toString().length() < 3) {
                    UIUtils.toast("请填写有效用户名和密码");
                    return;
                }

                UIUtils.hideSoftKeyboard(mActivity);

                HiSettingsHelper.getInstance().setUsername(etUsername.getText().toString());
                HiSettingsHelper.getInstance().setPassword(etPassword.getText().toString());
                HiSettingsHelper.getInstance().setSecQuestion(adapter.getEntryValue(spSecQuestion.getSelectedItemPosition()));
                HiSettingsHelper.getInstance().setSecAnswer(etSecAnswer.getText().toString());
                HiSettingsHelper.getInstance().setUid("");

                mActivity.doLoginProgress();
                dismiss();
            }
        });

        setContentView(view);
    }

}
