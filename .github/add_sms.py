import os
app = os.environ['APP_ID']
d = 'android/app/src/main/java/' + app.replace('.', '/')
os.makedirs(d, exist_ok=True)
src = open('android-sms/BankSmsPlugin.java', encoding='utf8').read().split('\n')
src[0] = 'package %s;' % app
open(d + '/BankSmsPlugin.java', 'w', encoding='utf8').write('\n'.join(src))
open(d + '/MainActivity.java', 'w').write('''package %s;
import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
public class MainActivity extends BridgeActivity {
  @Override public void onCreate(Bundle b) { registerPlugin(BankSmsPlugin.class); super.onCreate(b); }
}
''' % app)
p = 'android/app/src/main/AndroidManifest.xml'
x = open(p).read()
perms = '<uses-permission android:name="android.permission.RECEIVE_SMS"/>\n    <uses-permission android:name="android.permission.READ_SMS"/>\n    '
rcv = ('<receiver android:name="%s.BankSmsPlugin$Rx" android:exported="true" android:permission="android.permission.BROADCAST_SMS">\n'
       '            <intent-filter android:priority="999"><action android:name="android.provider.Telephony.SMS_RECEIVED"/></intent-filter>\n'
       '        </receiver>\n    </application>') % app
x = x.replace('<application', perms + '<application', 1).replace('</application>', rcv, 1)
open(p, 'w').write(x)
print('SMS plugin added for', app)
