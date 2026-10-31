package uz.ostatka.app;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * Matnni buferga nusxalash va Telegram'ga (yoki boshqa ilovaga) yuborish.
 * JS: Capacitor.Plugins.TextShare.copy({text})
 *     Capacitor.Plugins.TextShare.share({text, telegram: true}) -> {app: "telegram" | "chooser"}
 */
@CapacitorPlugin(name = "TextShare")
public class TextShare extends Plugin {

    private static final String[] TELEGRAM = {"org.telegram.messenger", "org.telegram.messenger.web", "org.thunderdog.challegram"};

    @PluginMethod
    public void copy(PluginCall call) {
        String text = call.getString("text", "");
        ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("Hisobot", text));
        call.resolve();
    }

    @PluginMethod
    public void share(PluginCall call) {
        String text = call.getString("text", "");
        boolean tg = Boolean.TRUE.equals(call.getBoolean("telegram", true));
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, text);
        if (tg) {
            for (String pkg : TELEGRAM) {
                try {
                    Intent i = new Intent(send).setPackage(pkg).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(i);
                    JSObject r = new JSObject();
                    r.put("app", "telegram");
                    call.resolve(r);
                    return;
                } catch (ActivityNotFoundException ignored) {
                }
            }
        }
        // Telegram topilmasa: Android'ning "Ulashish" oynasi
        Intent ch = Intent.createChooser(send, "Hisobotni yuborish").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(ch);
        JSObject r = new JSObject();
        r.put("app", "chooser");
        call.resolve(r);
    }
}
