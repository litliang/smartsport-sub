package app.auto;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.xutils.view.annotation.ContentView;

import java.util.ArrayList;
import java.util.List;

import app.base.MapConf;
import app.base.RRes;
import app.base.SPrefUtil;
import app.base.action.Action;
import app.base.action.ViewInflater;
import app.base.widget.ImageView;
import top.smartsport.www.R;
import top.smartsport.www.base.BaseComptActivity;
import top.smartsport.www.utils.SPUtils;

@ContentView(R.layout.activity_auto_action_ui)
public class DrawerLeftActivity extends BaseComptActivity  {
    private GestureDetector mGestureDetector;
    private DrawerLayout mDrawerLayout;
    private String SPNAME = "frame";

    @Override
    protected void initView() {
        SPrefUtil.iniContext(getApplication());

        RRes.initR(getBaseContext());
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.mipmap.alipay, R.string.setting, R.string.about_title) {

            /**
             * Called when a drawer has settled in a completely closed state.
             */
            public void onDrawerClosed(View view) {
//                getActionBar().setTitle(mTitle);
            }

            /**
             * Called when a drawer has settled in a completely open state.
             */
            public void onDrawerOpened(View drawerView) {
//                getActionBar().setTitle(mDrawerTitle);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.findViewById(R.id.iv_back).setVisibility(View.INVISIBLE);
        ((TextView) mDrawerLayout.findViewById(R.id.ivLeft)).setText("三");
        ((TextView) mDrawerLayout.findViewById(R.id.ivLeft)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        ((TextView) mDrawerLayout.findViewById(R.id.ivLeft)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });


        View nav = findViewById(R.id.nav);

        MapConf.with(getBaseContext()).conf(MapConf.with(getBaseContext()).source(R.layout.auto_string_item)).source(getListRes(), findViewById(R.id.left_drawer)).toView();
        nav.findViewById(R.id.ivLeft).setVisibility(View.GONE);
        nav.findViewById(R.id.iv_back).setVisibility(View.GONE);
        ((TextView) nav.findViewById(R.id.tvTitle)).setText("界面");
        ((ListView) nav.findViewById(R.id.left_drawer)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clickDrawerLayout((String) parent.getItemAtPosition(position));

            }
        });
        String name = (String) SPrefUtil.getValue(SPNAME, "left_drawer", String.class);
        if (!TextUtils.isEmpty(name)) {
            clickDrawerLayout(name);
        }

    }

    private void clickDrawerLayout(String name) {
        mDrawerLayout.closeDrawer(Gravity.LEFT);
        int layoutid = RRes.get("R.layout." + name + "").getAndroidValue();
        SPrefUtil.putValue(getBaseContext(), SPNAME, "left_drawer", name);
        ((ViewGroup) findViewById(R.id.content)).removeAllViews();
        View userview = new ViewInflater(DrawerLeftActivity.this).inflate(layoutid, null);
        ((ViewGroup) findViewById(R.id.content)).addView(userview);
        setView(userview);
    }


    List<View> views = new ArrayList<View>();

    private void setView(View userview) {
        if (userview.getId() != 0) {
            views.add(userview);
            userview.setForeground(getResources().getDrawable(R.drawable.auto_background_rect_white, null));
            setViewClickPanel(userview);
        }
        if (userview instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) userview).getChildCount(); i++) {
                View view = ((ViewGroup) userview).getChildAt(i);

                setView(view);
            }

        }
    }

    private void setViewClickPanel(View userview) {
        if (userview instanceof AdapterView) {

        } else {
            userview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String value = "";
                    if(v instanceof TextView){
                        value = ((TextView)v).getText().toString();
                    }else if(v instanceof app.base.widget.ImageView){
                        value = ((app.base.widget.ImageView)v).getUrl();
                    }
                    String id = RRes.getAttrValue_itsname().get(v.getId());

                    startActivity(new Intent(v.getContext(), ActionActivity.class).putExtra("value",value).putExtra("idname",id).putExtra("type",v.getClass().getSimpleName()).putExtra("W x H",v.getWidth()+" x "+v.getHeight()));
                }
            });
        }
    }

    public List getListRes() {
        RRes.TypeAttrs clz = RRes.getResType().get("layout");
        List list = new ArrayList<String>(clz.attrMaps.keySet());
        return list;

    }


    private class simpleGestureListener extends
            GestureDetector.SimpleOnGestureListener {

        /*****
         * OnGestureListener的函数
         *****/

        final int FLING_MIN_DISTANCE = 100, FLING_MIN_VELOCITY = 200;

        // 触发条件 ：
        // X轴的坐标位移大于FLING_MIN_DISTANCE，且移动速度大于FLING_MIN_VELOCITY个像素/秒

        // 参数解释：
        // e1：第1个ACTION_DOWN MotionEvent
        // e2：最后一个ACTION_MOVE MotionEvent
        // velocityX：X轴上的移动速度，像素/秒
        // velocityY：Y轴上的移动速度，像素/秒
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {


            if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE
                    && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
                // Fling left
//                Log.i("MyGesture", "Fling left");
                mDrawerLayout.closeDrawer(Gravity.LEFT);
//                Toast.makeText(DrawerLeftActivity.this, "Fling Left", Toast.LENGTH_SHORT).show();
            } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE
                    && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
                // Fling right
//                Log.i("MyGesture", "Fling right");
                mDrawerLayout.openDrawer(Gravity.LEFT);
//                Toast.makeText(DrawerLeftActivity.this, "Fling Right", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

    }
}
