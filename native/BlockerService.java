package com.dayforge.app;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.telecom.TelecomManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Solo mira QUÉ app se abrió (nombre del paquete). No lee pantallas ni textos.
public class BlockerService extends AccessibilityService {

    private static final String[] ALLOWED = {
        "com.whatsapp", "com.whatsapp.w4b",
        "com.google.android.dialer", "com.android.dialer", "com.samsung.android.dialer",
        "com.android.incallui", "com.samsung.android.incallui", "com.android.phone",
        "com.android.server.telecom", "com.google.android.telecom",
        "android", "com.android.systemui", "com.google.android.systemui",
        "com.android.permissioncontroller", "com.google.android.permissioncontroller",
        "com.google.android.packageinstaller", "com.android.packageinstaller",
        "com.android.documentsui", "com.google.android.documentsui",
        "com.android.providers.media.module", "com.google.android.providers.media.module",
        "com.android.inputmethod.latin", "com.google.android.inputmethod.latin",
        "com.samsung.android.honeyboard", "com.touchtype.swiftkey"
    };

    private static final boolean BLOCK_SETTINGS = false;
    private static final String[] SETTINGS = {
        "com.android.settings", "com.samsung.android.settings"
    };

    private final Set<String> allowed = new HashSet<>();
    private long lastLaunch = 0;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        refreshAllowed();
    }

    private void refreshAllowed() {
        allowed.clear();
        allowed.add(getPackageName());
        for (String p : ALLOWED) allowed.add(p);
        if (!BLOCK_SETTINGS) for (String p : SETTINGS) allowed.add(p);
        try {
            Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
            List<ResolveInfo> hs = getPackageManager().queryIntentActivities(home, 0);
            for (ResolveInfo r : hs) allowed.add(r.activityInfo.packageName);
        } catch (Exception ignored) { }
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            for (InputMethodInfo i : imm.getEnabledInputMethodList()) allowed.add(i.getPackageName());
        } catch (Exception ignored) { }
        try {
            TelecomManager tm = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
            String d = tm.getDefaultDialerPackage();
            if (d != null) allowed.add(d);
        } catch (Exception ignored) { }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent ev) {
        if (ev == null || ev.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        if (!DayForgeNative.isOn(this)) return;
        CharSequence cs = ev.getPackageName();
        if (cs == null) return;
        String pkg = cs.toString();
        if (allowed.contains(pkg)) return;
        refreshAllowed();
        if (allowed.contains(pkg)) return;

        long now = System.currentTimeMillis();
        if (now - lastLaunch < 700) return;
        lastLaunch = now;

        Intent i = new Intent(this, BlockActivity.class);
        i.putExtra("pkg", pkg);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    @Override
    public void onInterrupt() { }
}
