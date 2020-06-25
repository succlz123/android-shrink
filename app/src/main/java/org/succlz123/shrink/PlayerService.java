package org.succlz123.shrink;

import android.app.Activity;
import android.widget.Toast;

public class PlayerService {

    void play(Activity activity) {
        Toast.makeText(activity, "player is play", Toast.LENGTH_SHORT).show();
    }
}
