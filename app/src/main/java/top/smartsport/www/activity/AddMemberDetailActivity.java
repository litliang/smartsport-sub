package top.smartsport.www.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.common.pic.ActivityChooseIcon;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.base.MapConf;
import intf.FunCallback;
import intf.JsonUtil;
import intf.MapBuilder;
import top.smartsport.www.R;
import top.smartsport.www.base.BaseActivity;
import top.smartsport.www.bean.NetEntity;
import top.smartsport.www.bean.RegInfo;
import top.smartsport.www.bean.TokenInfo;
import top.smartsport.www.xutils3.MyCallBack;
import top.smartsport.www.xutils3.X;

/**
 * Created by Administrator on 2017/8/17.
 */
@ContentView(R.layout.activity_add_member_detail)
public class AddMemberDetailActivity extends BaseActivity {
    @ViewInject(R.id.et_team_name)
    private TextView name;
    @ViewInject(R.id.weizhi)
    private TextView weizhi;
    @ViewInject(R.id.haoma)
    private TextView number;
    @ViewInject(R.id.btn_add)
    private Button add;
    @ViewInject(R.id.account_header)
    ImageView mIcon;
    @ViewInject(R.id.account_set_rl)
    RelativeLayout mSetIconRl;

    private String team_id;
    private Bitmap iconBitMap;
    private final int CODE_CHOOSE_ICON = 3;
    private final int CODE_CHOOSE_ICON_CAMERA = 4;
    private final int CODE_CHOOSE_ICON_PICTURE = 5;
    private final int CODE_CHOOSE_ICON_ZOOM = 6;
    private final String KEY_CHOOSE_ICON_TYPE = "choose_type";
    private final String KEY_CHOOSE_TYPE_CAMERA = "choose_type_camera";
    private final String KEY_CHOOSE_TYPE_PICTURE = "choose_type_picture";
    private final String ICON_NAME = "ICON.jpg";
    private String member;
    private int position;
    private List list;

    @Override
    protected void initView() {
        back();
        team_id = getIntent().getStringExtra("team_id");
        member =getIntent().getStringExtra("member");
        position = getIntent().getIntExtra("position",-1);
        if (!TextUtils.isEmpty(member)){
            list =(List) intf.JsonUtil.extractJsonRightValue(JsonUtil.findJsonLink("player",member));
            Map map = (Map) list.get(position);
            MapConf.build().with(AddMemberDetailActivity.this).pair("header_url->account_header").pair("name->et_team_name").pair("position->weizhi").pair("number->haoma").source(map, getWindow().getDecorView()).toView();
            setTitle("编辑球员");
            add.setText("确定");
        }
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editMember(new FunCallback() {
                    @Override
                    public void onSuccess(Object result, List object) {
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onFailure(Object result, List object) {

                    }

                    @Override
                    public void onCallback(Object result, List object) {

                    }
                });
            }
        });

        mSetIconRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddMemberDetailActivity.this, ActivityChooseIcon.class);
                startActivityForResult(intent, CODE_CHOOSE_ICON);
            }
        });

        back();
    }
    private void editMember(FunCallback func) {
            MapBuilder m = MapBuilder.build().add("action", "editMyTeam");
            m.add("team_id", team_id);
            m.add("name",name.getText().toString());
            m.add("type", 0);
            m.add("number", number.getText().toString());
            m.add("position", weizhi.getText().toString());
        if (!TextUtils.isEmpty(member)){
            m.add("member_id",((Map)list.get(position)).get("id"));
        }
        if(imageid!=null){
            m.add("header",imageid);
        }
            callHttp(m.get(), func);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (iconBitMap != null && (!iconBitMap.isRecycled())){
            iconBitMap.recycle();
        }
    }
    String imageid;
    private void postIcon(final String fileName) {
        RegInfo regInfo = RegInfo.newInstance();
        TokenInfo tokenInfo = TokenInfo.newInstance();

        String client_id = regInfo.getApp_key();
        String state = regInfo.getSeed_secret();
        String url = regInfo.getSource_url();
        String access_token = tokenInfo.getAccess_token();
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
                Toast.makeText(AddMemberDetailActivity.this, getResources().getString(R.string.icon_post_fail), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(NetEntity entity) {
                String entity_data = entity.getStatus();
                if (entity_data.equals("true")){
                    Toast.makeText(AddMemberDetailActivity.this, getResources().getString(R.string.icon_post_success), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(AddMemberDetailActivity.this, getResources().getString(R.string.icon_post_fail), Toast.LENGTH_SHORT).show();
                }
                imageid = entity.getImg_id();
            }

            @Override
            public void onError(Throwable throwable, boolean b) {
                super.onError(throwable, b);
            }
        });
    }
}