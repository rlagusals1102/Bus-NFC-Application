package com.example.nfc;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class NotificationActivity extends AppCompatActivity {
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        textView = (TextView) findViewById(R.id.textViewData);
        String data = getIntent().getStringExtra("data");
        textView.setText(data);
    }
}
