package org.succlz123.shrink.app.test;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import org.succlz123.shrink.app.test.R;

public class SecondActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        String str = getIntent().getStringExtra("params");
        ((TextView) findViewById(R.id.content)).setText("Go from First Activity " + str);
    }
}
