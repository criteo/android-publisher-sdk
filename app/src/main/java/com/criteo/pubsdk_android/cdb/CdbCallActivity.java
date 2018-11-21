package com.criteo.pubsdk_android.cdb;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.criteo.pubsdk.model.Publisher;
import com.criteo.pubsdk_android.R;

public class CdbCallActivity extends AppCompatActivity {
    private static final String TAG = CdbCallActivity.class.getSimpleName();
    private Button mButton;
    private TextView mTextViewContent;
    private ViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cdb_call);
        mButton = findViewById(R.id.buttonCdbCall);
        mTextViewContent = findViewById(R.id.textViewContent);
        viewModel = ViewModelProviders.of(this).get(CdbViewModel.class);
        mButton.setOnClickListener((View v) -> {
            onClickCdbCallButton();
        });
    }

    private void onClickCdbCallButton() {
        Log.d(TAG, "onClickCdbCallButton");
        ((CdbViewModel) viewModel).getDataFromCbd(new Publisher(getApplicationContext()))
                .observe(this, data -> {
                    mTextViewContent.setText(mTextViewContent.getText() + "\n" + data.toString());
                });
    }

}
