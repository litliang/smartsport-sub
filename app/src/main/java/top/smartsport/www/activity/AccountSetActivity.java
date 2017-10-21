package top.smartsport.www.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.base.JsonUtil;
import app.base.MapConf;
import intf.FunCallback;
import intf.MapBuilder;
import top.smartsport.www.R;
import top.smartsport.www.base.BaseActivity;
import top.smartsport.www.bean.NetEntity;
import top.smartsport.www.bean.RegInfo;
import top.smartsport.www.bean.TokenInfo;
import top.smartsport.www.utils.ActivityStack;
import top.smartsport.www.utils.SPUtils;
import top.smartsport.www.xutils3.MyCallBack;
import top.smartsport.www.xutils3.X;

/**
 * Created by Aaron on 2017/7/18.
 * 账户设置
 */
@ContentView(R.layout.activity_accountset)
public class AccountSetActivity extends BaseActivity {

    @ViewInject(R.id.account_header)
    ImageView mIcon;

    @ViewInject(R.id.account_set_rl)
    RelativeLayout mSetIconRl;

    private RegInfo regInfo;
    private TokenInfo tokenInfo;

    private String client_id;
    private String state;
    private String url;
    private String access_token;
    private String data = "";
    private Bitmap iconBitMap;
    private Map data_map;

    private final int CODE_CHOOSE_ICON = 3;

    @Override
    protected void initView() {
        regInfo = RegInfo.newInstance();
        tokenInfo = TokenInfo.newInstance();

        client_id = regInfo.getApp_key();
        state = regInfo.getSeed_secret();
        url = regInfo.getSource_url();
        access_token = tokenInfo.getAccess_token();
        data = (String) SPUtils.get(getBaseContext()
                , "getUserInfo", "");
        BaseActivity.callHttp(MapBuilder.build().add("action", "getUserInfo").get(), new FunCallback() {
            @Override
            public void onSuccess(Object result, List object) {
            }

            @Override
            public void onFailure(Object result, List object) {
            }

            @Override
            public void onCallback(Object result, List object) {
                if (result instanceof NetEntity) {
                    data = ((NetEntity)result).getData().toString();

                    SPUtils.put(getBaseContext(), "getUserInfo", data);
                    SPUtils.put(getBaseContext(), "is_vip", JsonUtil.findJsonLink("is_vip",data));
                    MapConf.with(getBaseContext()).pair("username->username").pair("truename->truename").pair("age->account_age").pair("sex->account_sex","0:保密;1:男;2:女").pair("height:%s cm->account_height").pair("weight:%s kg->account_weight")
                            .pair("leg->account_habit","1:左脚;2:右脚;3:左右脚")
                            .pair("header_url->account_header")
                            .pair("soccer_age:%s 年->account_ql")
                            .pair("address->account_jz")
                            .source(data, getWindow().getDecorView()).toView();
                }
            }
        });

        mSetIconRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityChooseIcon.getInstance().invokeResultActivity(AccountSetActivity.this,3);
            }
        });

        findViewById(R.id.chpwd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(),ForgetPSWActivity.class).putExtra("title","修改密码"));
            }
        });
    }


    @Event(value = {R.id.account_btn_login_out})
    private void getEvent(View v) {
        switch (v.getId()) {
            case R.id.account_btn_login_out:
                loginOut();
                break;
        }
    }

    /**
     * 用户退出
     */
    private void loginOut() {
        SPUtils.put(getBaseContext(), "USER", "");
        goActivity(LoginActivity.class);
        finish();
        ActivityStack.getInstance().findActivityByClass(MainActivity.class).finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String temp;
        if (requestCode == CODE_CHOOSE_ICON){
            if(data != null) {
                String path = data.getStringExtra("path");
                Glide.with(this).load(path)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .placeholder(R.mipmap.default_img)
                        .error(R.mipmap.default_img).into(mIcon);
                postIcon(path);
            }
        }
    }

    private void postIcon(final String fileName) {
        File file = new File(fileName);
        Map<String,Object> map = new HashMap<>();
        map.put("client_id",client_id);
        map.put("state",state);
        map.put("access_token",access_token);
        map.put("action","uploadImg");
        map.put("image", file);
        X.Post(url, map, new MyCallBack<String>() {
            @Override
            protected void onFailure(String message) {
                Toast.makeText(AccountSetActivity.this, getResources().getString(R.string.icon_post_fail), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(NetEntity entity) {
                String entity_data = entity.getStatus();
                if (entity_data.equals("true")){
                    Toast.makeText(AccountSetActivity.this, getResources().getString(R.string.icon_post_success), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(AccountSetActivity.this, getResources().getString(R.string.icon_post_fail), Toast.LENGTH_SHORT).show();
                }
                String icon_imgId = entity.getImg_id();
                Log.e("smile", " onSuccess   entity.getImg_id() =   " + icon_imgId);
                MapConf.with(getBaseContext()).pair("header", icon_imgId);
                data_map = MapConf.with(getBaseContext()).pair("username->username").pair("username->truename").pair("age->account_age").pair("sex->account_sex","0:女;1:男").pair("height->account_height").pair("weight->account_weight")
                        .pair("leg->account_habit","1:左脚;2:右脚;3:左右脚;0:未知")
                        .pair("header_url->account_header")
                        .pair("soccer_age:%s 年->account_ql")
                        .pair("address->account_jz")
                        .pair("header", icon_imgId)
                        .toMap(AccountSetActivity.this);
                postData(data_map, icon_imgId);
            }

            @Override
            public void onError(Throwable throwable, boolean b) {
                super.onError(throwable, b);
            }
        });
    }

    /**
     * 用户信息完善确定
     * */
    private void postData(Map map, String img_id){

        callHttp(MapBuilder.withMap(map).add("action", "saveBaseUserInfo").add("header", img_id).add("type", "modify").get(), new FunCallback() {
            @Override
            public void onSuccess(Object result, List object) {
                showToast("用户信息已更新");
            }

            @Override
            public void onFailure(Object result, List object) {
            }

            @Override
            public void onCallback(Object result, List object) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (iconBitMap != null && (!iconBitMap.isRecycled())){
            iconBitMap.recycle();
        }
    }
}
