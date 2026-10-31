package uz.ostatka.app;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(ApkUpdater.class);
        registerPlugin(GeoGuard.class);
        registerPlugin(TextShare.class);
        super.onCreate(savedInstanceState);
    }
}
