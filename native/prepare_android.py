# Copia el bloqueador al proyecto Android que genera Capacitor.
import shutil, os, sys
A = "android/app/src/main/"
J = A + "java/com/dayforge/app/"
for f in ("DayForgeNative", "BlockerService", "BlockActivity"):
    shutil.copy("native/" + f + ".java", J + f + ".java")
open(J + "MainActivity.java", "w", encoding="utf-8").write("""package com.dayforge.app;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        getBridge().getWebView().addJavascriptInterface(new DayForgeNative(this), "DayForgeNative");
    }
}
""")
os.makedirs(A + "res/xml", exist_ok=True)
open(A + "res/xml/accessibility_service_config.xml", "w", encoding="utf-8").write("""<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeWindowStateChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:accessibilityFlags="flagDefault"
    android:canRetrieveWindowContent="false"
    android:notificationTimeout="100"
    android:description="@string/dayforge_block_desc" />
""")
open(A + "res/values/dayforge_strings.xml", "w", encoding="utf-8").write("""<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="dayforge_block_desc">Detecta qué app se abre para bloquearla mientras el modo enfoque de DayForge está activo. No lee tu pantalla ni tus datos.</string>
</resources>
""")
p = A + "AndroidManifest.xml"
m = open(p, encoding="utf-8").read()
if "BlockerService" in m:
    sys.exit(0)
q = """
    <queries>
        <intent><action android:name="android.intent.action.MAIN" /><category android:name="android.intent.category.HOME" /></intent>
        <intent><action android:name="android.intent.action.MAIN" /><category android:name="android.intent.category.LAUNCHER" /></intent>
    </queries>

    <application"""
x = """
        <activity
            android:name=".BlockActivity"
            android:exported="false"
            android:launchMode="singleTask"
            android:taskAffinity=""
            android:excludeFromRecents="true"
            android:enableOnBackInvokedCallback="false"
            android:theme="@android:style/Theme.Material.NoActionBar" />

        <service
            android:name=".BlockerService"
            android:exported="true"
            android:label="DayForge - Modo enfoque"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/accessibility_service_config" />
        </service>
    </application>"""
m = m.replace("<application", q, 1).replace("</application>", x, 1)
open(p, "w", encoding="utf-8").write(m)
