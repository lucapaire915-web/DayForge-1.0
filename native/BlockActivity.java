package com.dayforge.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Pantalla que tapa la app bloqueada. Sin archivos de diseño: todo se arma por código. */
public class BlockActivity extends Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        build(getIntent());
    }

    @Override
    protected void onNewIntent(Intent i) {
        super.onNewIntent(i);
        setIntent(i);
        build(i);
    }

    private void build(Intent in) {
        String pkg = in.getStringExtra("pkg");
        String name = pkg;
        try {
            PackageManager pm = getPackageManager();
            name = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
        } catch (Exception ignored) { }

        float d = getResources().getDisplayMetrics().density;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.parseColor("#0F1115"));
        int pad = (int) (28 * d);
        root.setPadding(pad, pad, pad, pad);

        TextView ic = new TextView(this);
        ic.setText("🔒");
        ic.setTextSize(56);
        ic.setGravity(Gravity.CENTER);
        root.addView(ic);

        TextView t = new TextView(this);
        t.setText("Modo enfoque activo");
        t.setTextColor(Color.WHITE);
        t.setTextSize(24);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, (int) (12 * d), 0, (int) (8 * d));
        root.addView(t);

        TextView s = new TextView(this);
        s.setText((name != null ? name : "Esta app") + " está bloqueada mientras te enfocás.\n"
                + "Para salir, abrí DayForge y tocá \"Apps bloqueadas\".");
        s.setTextColor(Color.parseColor("#B8BDC9"));
        s.setTextSize(16);
        s.setGravity(Gravity.CENTER);
        s.setPadding(0, 0, 0, (int) (28 * d));
        root.addView(s);

        root.addView(button("Abrir DayForge", true, new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent i = new Intent(BlockActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
            }
        }, d));
        root.addView(button("Volver al inicio", false, new View.OnClickListener() {
            @Override public void onClick(View v) { goHome(); }
        }, d));

        setContentView(root);
    }

    private Button button(String text, boolean primary, View.OnClickListener l, float d) {
        Button bt = new Button(this);
        bt.setText(text);
        bt.setAllCaps(false);
        bt.setTextSize(16);
        bt.setTextColor(Color.WHITE);
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(14 * d);
        g.setColor(Color.parseColor(primary ? "#0A84FF" : "#2A2D35"));
        bt.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (52 * d));
        lp.setMargins(0, 0, 0, (int) (10 * d));
        bt.setLayoutParams(lp);
        bt.setOnClickListener(l);
        return bt;
    }

    private void goHome() {
        Intent h = new Intent(Intent.ACTION_MAIN);
        h.addCategory(Intent.CATEGORY_HOME);
        h.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(h);
        finish();
    }

    @Override
    public void onBackPressed() { goHome(); }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isFinishing()) finish();
    }
}
