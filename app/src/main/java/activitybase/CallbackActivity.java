package activitybase;

import android.app.Activity;
import android.content.Intent;

import top.smartsport.www.activity.AccountSetActivity;

//as a ordinary runner, related nothing with Activity's life cycle
public class CallbackActivity extends Activity {

    public static CallbackActivity instance = new CallbackActivity();

    public static CallbackActivity getInstance() {
        return instance;
    }
    //default requestcode 0
    public void invokeResultActivity(Activity driving) {
        invokeResultActivity(driving,0);
    }

    public void invokeResultActivity(Activity driving, int requesrcode) {
        Intent intent = new Intent(driving, getClass());
        driving.startActivityForResult(intent, requesrcode);
    }
}
