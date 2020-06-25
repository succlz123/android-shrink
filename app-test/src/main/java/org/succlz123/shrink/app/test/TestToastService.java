package org.succlz123.shrink.app.test;

import android.app.Activity;
import android.widget.Toast;

public class TestToastService {

    public void show(Activity activity) {
        Toast.makeText(activity, "123_test", Toast.LENGTH_SHORT).show();
    }
}
