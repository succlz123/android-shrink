package org.succlz123.shrink;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import org.succlz123.shrink.app.R;
import org.succlz123.shrink.app.test.SecondActivity;

public class FirstActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String str = getIntent().getStringExtra("params");
        ((TextView) findViewById(R.id.content)).setText("Go from Main Activity " + str);
        ((TextView) findViewById(R.id.content)).append("\nClick to show app toast and goto second activity");
        findViewById(R.id.content).setOnClickListener(v -> {
            Intent intent = new Intent(this, SecondActivity.class);
            this.startActivity(intent);
        });
        ((TextView) findViewById(R.id.content_2)).setText("\nClick to show app_test toast");
        findViewById(R.id.content_2).setOnClickListener(v -> {
        });
    }
}
