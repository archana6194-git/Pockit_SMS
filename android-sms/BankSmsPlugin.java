package com.example.pockit; // <- change to YOUR app's package name

import android.Manifest;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import com.getcapacitor.*;
import com.getcapacitor.annotation.*;
import org.json.*;

@CapacitorPlugin(name = "BankSms",
  permissions = {@Permission(alias = "sms", strings = {Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS})})
public class BankSmsPlugin extends Plugin {
  static BankSmsPlugin inst;
  @Override public void load() { inst = this; }

  static boolean isBank(String b) {
    String l = b.toLowerCase();
    if (l.contains("otp") || l.contains("one time pass")) return false;
    String[] k = {"debit", "credit", "transfer", "withdrawn", "spent", "purchase", "deducted", "deposited", "received", "refund", "paid to"};
    for (String s : k) if (l.contains(s)) return true;
    return false;
  }

  // Runs even when the app is closed; keeps bank SMS safe until the app opens.
  public static class Rx extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent i) {
      try {
        SmsMessage[] m = Telephony.Sms.Intents.getMessagesFromIntent(i);
        if (m == null || m.length == 0) return;
        StringBuilder sb = new StringBuilder();
        for (SmsMessage s : m) sb.append(s.getMessageBody());
        String body = sb.toString();
        if (!isBank(body)) return;
        SharedPreferences sp = c.getSharedPreferences("banksms", Context.MODE_PRIVATE);
        JSONArray a = new JSONArray(sp.getString("q", "[]"));
        a.put(new JSONObject().put("body", body).put("ts", m[0].getTimestampMillis()));
        sp.edit().putString("q", a.toString()).apply();
        if (inst != null) inst.notifyListeners("sms", new JSObject());
      } catch (Exception e) {}
    }
  }

  @PluginMethod public void getPending(PluginCall call) {
    try {
      SharedPreferences sp = getContext().getSharedPreferences("banksms", Context.MODE_PRIVATE);
      JSObject r = new JSObject();
      r.put("items", new JSONArray(sp.getString("q", "[]")));
      sp.edit().putString("q", "[]").apply();
      call.resolve(r);
    } catch (Exception e) { call.reject(e.getMessage()); }
  }

  @PluginMethod public void readInbox(PluginCall call) {
    try {
      long since = System.currentTimeMillis() - call.getInt("days", 90) * 86400000L;
      JSONArray a = new JSONArray();
      Cursor c = getContext().getContentResolver().query(Uri.parse("content://sms/inbox"),
        new String[]{"body", "date"}, "date>?", new String[]{"" + since}, "date ASC");
      if (c != null) {
        while (c.moveToNext()) if (isBank(c.getString(0))) a.put(new JSONObject().put("body", c.getString(0)).put("ts", c.getLong(1)));
        c.close();
      }
      JSObject r = new JSObject(); r.put("items", a); call.resolve(r);
    } catch (Exception e) { call.reject(e.getMessage()); }
  }
}
