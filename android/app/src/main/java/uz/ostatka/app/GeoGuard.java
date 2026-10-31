package uz.ostatka.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import java.util.LinkedHashSet;
import java.util.List;

import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

/**
 * Telefon joylashuvi (GPS / Wi-Fi). Soxta joylashuv (mock) ilovalarini aniqlaydi.
 * JS: Capacitor.Plugins.GeoGuard.get() -> {lat, lon, acc, mock}
 *     xato kodlari: DENIED (ruxsat yo'q), COARSE (faqat taxminiy ruxsat), OFF (GPS o'chiq), TIMEOUT
 *     Capacitor.Plugins.GeoGuard.openSettings({type: "gps" | "app"})
 */
@CapacitorPlugin(
    name = "GeoGuard",
    permissions = {
        @Permission(alias = "location", strings = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}),
        @Permission(alias = "phone", strings = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_PHONE_NUMBERS})
    }
)
public class GeoGuard extends Plugin {

    /* Bino ichida GPS aniqligi kamdan-kam 30 m ga yetadi, shuning uchun kutish qisqa:
       - aniqligi GOOD_ACC dan yaxshi joylashuv kelsa, darhol qaytariladi;
       - birinchi joylashuv kelgach, yaxshirog'i uchun ko'pi bilan AFTER_FIRST_MS kutiladi;
       - umuman joylashuv bo'lmasa, TIMEOUT_MS dan keyin xato. */
    private static final long TIMEOUT_MS = 10000;
    private static final long AFTER_FIRST_MS = 2500;
    private static final float GOOD_ACC = 100f;
    private static final long FRESH_MS = 120000;

    /* Qurilma ma'lumoti (administrator «Kirishlar tarixi» uchun):
       {name: "Samsung SM-A515F", android: "13", id: ANDROID_ID — ilova + telefon uchun doimiy} */
    @PluginMethod
    public void info(PluginCall call) {
        JSObject r = new JSObject();
        String man = Build.MANUFACTURER == null ? "" : Build.MANUFACTURER;
        String mod = Build.MODEL == null ? "" : Build.MODEL;
        if (!man.isEmpty()) man = man.substring(0, 1).toUpperCase() + man.substring(1);
        r.put("name", mod.toLowerCase().startsWith(man.toLowerCase()) ? mod : (man + " " + mod).trim());
        r.put("android", Build.VERSION.RELEASE);
        String id = "";
        try {
            id = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception ignored) {
        }
        r.put("id", id == null ? "" : id);
        call.resolve(r);
    }

    /* SIM-kartadagi telefon raqami(lar)i: {numbers: "+99890...,+99893..."} (vergul bilan).
       Ko'p SIM-kartalarda raqam yozilmagan bo'ladi — unda bo'sh satr. Ruxsat berilmasa {numbers:"", denied:true}. */
    @PluginMethod
    public void sim(PluginCall call) {
        if (getPermissionState("phone") != PermissionState.GRANTED) {
            requestPermissionForAlias("phone", call, "simPerm");
            return;
        }
        readSim(call);
    }

    @PermissionCallback
    private void simPerm(PluginCall call) {
        if (getPermissionState("phone") != PermissionState.GRANTED) {
            JSObject r = new JSObject();
            r.put("numbers", "");
            r.put("denied", true);
            call.resolve(r);
            return;
        }
        readSim(call);
    }

    private static void addNum(LinkedHashSet<String> set, String n) {
        if (n != null && n.replaceAll("[^0-9]", "").length() >= 7) set.add(n.trim());
    }

    private void readSim(PluginCall call) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        try {
            SubscriptionManager sm = (SubscriptionManager) getContext().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            List<SubscriptionInfo> l = sm != null ? sm.getActiveSubscriptionInfoList() : null;
            if (l != null) {
                for (SubscriptionInfo si : l) {
                    String n;
                    if (Build.VERSION.SDK_INT >= 33) n = sm.getPhoneNumber(si.getSubscriptionId());
                    else n = si.getNumber();
                    addNum(set, n);
                }
            }
        } catch (Exception ignored) {
        }
        try {
            TelephonyManager tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) addNum(set, tm.getLine1Number());
        } catch (Exception ignored) {
        }
        JSObject r = new JSObject();
        r.put("numbers", android.text.TextUtils.join(",", set));
        call.resolve(r);
    }

    @PluginMethod
    public void get(PluginCall call) {
        if (getPermissionState("location") != PermissionState.GRANTED) {
            requestPermissionForAlias("location", call, "permCallback");
            return;
        }
        locate(call);
    }

    @PermissionCallback
    private void permCallback(PluginCall call) {
        if (getPermissionState("location") != PermissionState.GRANTED) {
            call.reject("Joylashuvga ruxsat berilmadi", "DENIED");
            return;
        }
        locate(call);
    }

    @SuppressWarnings("MissingPermission")
    private void locate(PluginCall call) {
        Context ctx = getContext();
        if (ctx.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            call.reject("Aniq joylashuvga ruxsat kerak", "COARSE");
            return;
        }
        LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean net = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gps && !net) {
            call.reject("GPS o'chiq", "OFF");
            return;
        }

        final Location[] best = {null};
        for (String p : new String[]{LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER}) {
            try {
                Location l = lm.getLastKnownLocation(p);
                if (l != null && System.currentTimeMillis() - l.getTime() < FRESH_MS && better(l, best[0])) best[0] = l;
            } catch (Exception ignored) {
            }
        }
        if (best[0] != null && best[0].getAccuracy() <= GOOD_ACC) {
            call.resolve(toJs(best[0]));
            return;
        }

        Handler h = new Handler(Looper.getMainLooper());
        final boolean[] done = {false};
        final boolean[] gotFix = {false};
        final LocationListener[] ref = {null};
        Runnable finish = () -> {
            if (done[0]) return;
            done[0] = true;
            try {
                lm.removeUpdates(ref[0]);
            } catch (Exception ignored) {
            }
            if (best[0] != null) call.resolve(toJs(best[0]));
            else call.reject("Joylashuv aniqlanmadi", "TIMEOUT");
        };
        ref[0] = new LocationListener() {
            @Override
            public void onLocationChanged(Location l) {
                if (better(l, best[0])) best[0] = l;
                if (best[0].getAccuracy() <= GOOD_ACC) finish.run();
                else if (!gotFix[0]) {
                    gotFix[0] = true;
                    h.postDelayed(finish, AFTER_FIRST_MS);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        h.post(() -> {
            try {
                if (gps) lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, ref[0], Looper.getMainLooper());
                if (net) lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, ref[0], Looper.getMainLooper());
            } catch (Exception e) {
                finish.run();
            }
        });
        h.postDelayed(finish, TIMEOUT_MS);
    }

    private static boolean better(Location l, Location cur) {
        return cur == null || l.getAccuracy() < cur.getAccuracy();
    }

    @SuppressWarnings("deprecation")
    private static JSObject toJs(Location l) {
        boolean mock = Build.VERSION.SDK_INT >= 31 ? l.isMock() : l.isFromMockProvider();
        JSObject o = new JSObject();
        o.put("lat", l.getLatitude());
        o.put("lon", l.getLongitude());
        o.put("acc", Math.round(l.getAccuracy()));
        o.put("mock", mock);
        return o;
    }

    @PluginMethod
    public void openSettings(PluginCall call) {
        Intent i;
        if ("gps".equals(call.getString("type"))) {
            i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        } else {
            i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getContext().getPackageName()));
        }
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(i);
        call.resolve();
    }
}
