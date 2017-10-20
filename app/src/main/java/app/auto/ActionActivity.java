package app.auto;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.xutils.view.annotation.ContentView;

import top.smartsport.www.R;
import top.smartsport.www.base.BaseActivity;
import top.smartsport.www.base.BaseComptActivity;

public class ActionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_ction);
        String value = getIntent().getStringExtra("value");
        value= value==null?"":value;
        String idname = getIntent().getStringExtra("idname");
        idname= idname==null?"":idname;
        String type = getIntent().getStringExtra("type");

        String wxh = getIntent().getStringExtra("W x H");

        ((TextView)findViewById(R.id.tvTitle)).setText(value+" "+idname.replace("id/","")+"\n ["+type+"] ["+wxh+"]");
    }
}

