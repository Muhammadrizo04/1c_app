package uz.ostatka.app;

import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Yangi APK'ni ilovaning o'zi yuklab oladi va Android o'rnatish oynasini ochadi.
 * Brauzer ishlatilmagani uchun yuklab olish manzili (server IP) foydalanuvchiga ko'rinmaydi.
 * JS: Capacitor.Plugins.ApkUpdater.install({url}), jarayon "progress" hodisasi bilan ({percent}).
 */
@CapacitorPlugin(name = "ApkUpdater")
public class ApkUpdater extends Plugin {

    @PluginMethod
    public void install(PluginCall call) {
        String url = call.getString("url");
        if (url == null || url.isEmpty()) {
            call.reject("url yo'q");
            return;
        }
        new Thread(() -> {
            HttpURLConnection con = null;
            try {
                File apk = new File(getContext().getCacheDir(), "update.apk");
                con = (HttpURLConnection) new URL(url).openConnection();
                con.setConnectTimeout(15000);
                con.setReadTimeout(60000);
                con.setInstanceFollowRedirects(true);
                int code = con.getResponseCode();
                if (code != 200) {
                    call.reject("Yuklab bo'lmadi (" + code + ")");
                    return;
                }
                long total = con.getContentLengthLong();
                long done = 0;
                int lastPct = -1;
                try (InputStream in = con.getInputStream(); OutputStream out = new FileOutputStream(apk)) {
                    byte[] buf = new byte[64 * 1024];
                    int n;
                    while ((n = in.read(buf)) != -1) {
                        out.write(buf, 0, n);
                        done += n;
                        if (total > 0) {
                            int pct = (int) (done * 100 / total);
                            if (pct != lastPct) {
                                lastPct = pct;
                                JSObject p = new JSObject();
                                p.put("percent", pct);
                                notifyListeners("progress", p);
                            }
                        }
                    }
                }
                if (total > 0 && done != total) {
                    call.reject("Fayl to'liq yuklanmadi");
                    return;
                }
                Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", apk);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setDataAndType(uri, "application/vnd.android.package-archive");
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(i);
                call.resolve();
            } catch (Exception e) {
                call.reject("Yuklab bo'lmadi. Internetni tekshiring.");
            } finally {
                if (con != null) con.disconnect();
            }
        }).start();
    }
}
