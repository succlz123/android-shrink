package org.succlz123.shrink;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import org.succlz123.shrink.app.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        ((TextView) findViewById(R.id.content)).setText("Go to Second Activity");
        findViewById(R.id.content).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FirstActivity.class);
            MainActivity.this.startActivity(intent);
        });
    }
}
