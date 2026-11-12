package com.dayforge.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;

/** Puente entre la web (window.DayForgeNative) y el bloqueo de apps. */
public class DayForgeNative {
    static final String PREFS = "dayforge_block";
    static final String KEY_ON = "on";

    private final Context ctx;

    public DayForgeNative(Context context) {
        this.ctx = context.getApplicationContext();
    }

    static boolean isOn(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ON, false);
    }

    static boolean serviceEnabled(Context c) {
        String list = Settings.Secure.getString(c.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (list == null) return false;
        ComponentName me = new ComponentName(c, BlockerService.class);
        TextUtils.SimpleStringSplitter sp = new TextUtils.SimpleStringSplitter(':');
        sp.setString(list);
        while (sp.hasNext()) {
            ComponentName cn = ComponentName.unflattenFromString(sp.next());
            if (cn != null && cn.equals(me)) return true;
        }
        return false;
    }

    /** true = quedó como pedías. false = falta dar el permiso (se abre Accesibilidad). */
    @JavascriptInterface
    public boolean setAppBlock(boolean on) {
        if (on && !serviceEnabled(ctx)) {
            openPermissionSettings();
            return false;
        }
        SharedPreferences.Editor e = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        e.putBoolean(KEY_ON, on).apply();
        return true;
    }

    @JavascriptInterface
    public boolean isBlocking() {
        return isOn(ctx) && serviceEnabled(ctx);
    }

    @JavascriptInterface
    public boolean hasPermission() {
        return serviceEnabled(ctx);
    }

    @JavascriptInterface
    public void openPermissionSettings() {
        Intent i = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }
}
