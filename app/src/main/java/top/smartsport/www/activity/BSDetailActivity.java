package top.smartsport.www.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.util.LogUtil;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import app.base.MapAdapter;
import app.base.MapConf;
import app.base.MapContent;
import intf.FunCallback;
import intf.JsonUtil;
import intf.MapBuilder;
import top.smartsport.www.R;
import top.smartsport.www.adapter.PICAdapter;
import top.smartsport.www.adapter.ScheduleAdapter;
import top.smartsport.www.base.BaseActivity;
import top.smartsport.www.bean.BSDetail;
import top.smartsport.www.bean.NetEntity;
import top.smartsport.www.bean.PicInfo;
import top.smartsport.www.bean.RegInfo;
import top.smartsport.www.bean.Schedule;
import top.smartsport.www.bean.TokenInfo;
import top.smartsport.www.utils.ImageUtil;
import top.smartsport.www.utils.StringUtil;
import top.smartsport.www.widget.MyGridView;
import top.smartsport.www.widget.MyListView;

/**
 * Created by Aaron on 2017/7/25.
 * 比赛详情
 */
@ContentView(R.layout.activity_bsdetail)
public class BSDetailActivity extends BaseActivity {
    public static final String TAG = BSDetailActivity.class.getName();

    private RegInfo regInfo;
    private TokenInfo tokenInfo;

    private String client_id;
    private String state;
    private String url;
    private String access_token;

    @ViewInject(R.id.adapter_bsss_img)
    private ImageView adapter_bsss_img;//图片
    @ViewInject(R.id.adapter_bsss_state)
    private TextView adapter_bsss_state;//状态
    @ViewInject(R.id.adapter_bsss_date)
    private TextView adapter_bsss_date;//时间
    @ViewInject(R.id.adapter_bsss_title)
    private TextView adapter_bsss_title;//标题
    @ViewInject(R.id.adapter_bsss_address)
    private TextView adapter_bsss_address;//地址
    @ViewInject(R.id.adapter_bsss_syu)
    private TextView adapter_bsss_syu;//还剩多少名额
    @ViewInject(R.id.adapter_bsss_pay)
    private TextView adapter_bsss_pay;//报名金额
    @ViewInject(R.id.adapter_bsss_level)
    private TextView adapter_bsss_level;//比赛等级
    @ViewInject(R.id.adapter_bsss_people)
    private TextView adapter_bsss_people;//多少人
    @ViewInject(R.id.adapter_bsss_rl_pay)
    private RelativeLayout adapter_bsss_rl_pay;
    @ViewInject(R.id.fl_loading)
    private FrameLayout fl_loading;
    @ViewInject(R.id.bs_detail_listView)
    private MyListView bs_detail_listView;
    private ScheduleAdapter adapter;

    private String id;
    private String states;

    @ViewInject(R.id.pic_gridView)
    private MyGridView pic_gridView;
    private PICAdapter picAdapter;

    @ViewInject(R.id.bs_detail_baoming)
    private Button bs_detail_baoming;

    @ViewInject(R.id.bs_detail_ll__listView)
    private LinearLayout bs_detail_ll__listView;

    @ViewInject(R.id.bs_detail_content)
    private WebView bs_detail_content;

    @ViewInject(R.id.bs_detail_ll_video)
    private LinearLayout bs_detail_ll_video;

    @ViewInject(R.id.bs_detail_video)
    private ListView bs_detail_video;

    @ViewInject(R.id.action_bar)
    private View actionbar;

    private List<Schedule> mListSchedule;
    private boolean isCurrentScStatus = true;
    private String orderStatus; // 订单状态：null未报名0未支付1已支付

    @Override
    public View getTopBar() {
        return actionbar;
    }

    @Override
    protected void initView() {
        back();
        fav();
        mListSchedule = new ArrayList<>();
//        ((TextView) actionbar.findViewById(R.id.tvTitle)).setText("比赛");
        id = (String) getObj(BSDetailActivity.TAG);
        String s = (String) getObj("states");


        if (null != s) {
            //1报名中2进行中 3已结束 4已报满5已报名
            Map map = MapBuilder.build().add("1", "报名中").add("3", "已结束").add("2", "进行中").add("4", "已报满").add("5", "已报名").get();
            if (map.values().contains(s)) {
                setBaominStatus(s);
            } else {
                Object o = map.get(s);
                if (o == null) {
                    return;
                }
                setBaominStatus(o);
            }
        }

        regInfo = RegInfo.newInstance();
        tokenInfo = TokenInfo.newInstance();

        client_id = regInfo.getApp_key();
        state = regInfo.getSeed_secret();
        url = regInfo.getSource_url();
        access_token = tokenInfo.getAccess_token();

        picAdapter = new PICAdapter(this);
        pic_gridView.setAdapter(picAdapter);

        findViewById(R.id.lookuppics).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("pics", (Serializable) picInfoList);
                goActivity(BSPictureActivity.class, bundle);
            }
        });
        getDetail();
        findViewById(R.id.rl_sp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getBaseContext(), KCBActivity.class).putExtra("match_id", id));
            }
        });
        findViewById(R.id.rl_sc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), ActivityDataAnalysis.class).putExtra("matchid", id));
            }
        });
//        findViewById(R.id.bs_detail_baoming).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Map map = MapConf.with(getBaseContext())
////                        .pair("total->sign_up_total_price_tv").pair("player->sign_up_member_tv").pair("contact->sign_up_contact_tv").pair("contact_mobile->sign_up_phone_iv")
//                        .toMap(BSDetailActivity.this);
//                callHttp(MapBuilder.withMap(map).add("action", "matchApplyPay").add("match_id", id).add("team_id", id).add("members", "").add("coach_name", "").add("coach_mobile", "").get(), new FunCallback() {
//                    @Override
//                    public void onSuccess(Object result, List object) {
//                        startActivity(new Intent(getBaseContext(), ActivityOrderConfirm.class));
//
//                    }
//
//                    @Override
//                    public void onFailure(Object result, List object) {
//
//                    }
//
//                    @Override
//                    public void onCallback(Object result, List object) {
//
//                    }
//                });
//                startActivity(new Intent(getBaseContext(), BSSignUpActivity.class));
//            }
//        });
        bs_detail_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO 对决列表的item点击

            }
        });
    }

    private void setBaominStatusInBaoming(Object o) {
        states = o.toString();
        if (states.equals("报名中")) {
            bs_detail_baoming.setVisibility(View.VISIBLE);//报名显示
            bs_detail_ll__listView.setVisibility(View.GONE); //正在比赛列表隐藏
            bs_detail_ll_video.setVisibility(View.GONE);//赛事视频隐藏
            adapter_bsss_state.setBackgroundResource(R.drawable.shape_bg_button);
            bs_detail_baoming.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivityForResult(new Intent(BSDetailActivity.this, SSBMActivity.class).putExtra(SSBMActivity.TAG, id), 101);
                }
            });
        }
    }


    private void setBaominStatus(Object o) {
        states = o.toString();
        adapter_bsss_rl_pay.setVisibility(View.VISIBLE);
        if (states.equals("进行中")) {
            bs_detail_baoming.setVisibility(View.INVISIBLE);//报名隐藏
            bs_detail_ll__listView.setVisibility(View.VISIBLE); //正在比赛列表显示
            bs_detail_ll_video.setVisibility(View.VISIBLE);//赛事视频隐藏
            adapter_bsss_state.setBackgroundResource(R.drawable.shape_bg_button_blue);
        } else if (states.equals("已结束")) {
            bs_detail_baoming.setVisibility(View.INVISIBLE);//报名隐藏
            bs_detail_ll__listView.setVisibility(View.GONE); //正在比赛列表隐藏
            bs_detail_ll_video.setVisibility(View.VISIBLE);//赛事视频隐藏
            adapter_bsss_rl_pay.setVisibility(View.GONE);
            adapter_bsss_state.setBackgroundResource(R.drawable.shape_bg_button_gray);
        } else if (states.equals("已报满")) {
            bs_detail_baoming.setVisibility(View.INVISIBLE);//报名隐藏
            bs_detail_ll__listView.setVisibility(View.GONE); //正在比赛列表隐藏
            bs_detail_ll_video.setVisibility(View.VISIBLE);//赛事视频隐藏
            adapter_bsss_state.setBackgroundResource(R.drawable.shape_bg_button_gray);
        } else if (states.equals("已报名")) {
            bs_detail_baoming.setVisibility(View.INVISIBLE);//报名隐藏
            bs_detail_ll__listView.setVisibility(View.GONE); //正在比赛列表隐藏
            bs_detail_ll_video.setVisibility(View.VISIBLE);//赛事视频隐藏
            adapter_bsss_state.setBackgroundResource(R.drawable.shape_bg_button_gray);
        }
    }

    @Override
    public void favImpl(View view, boolean unfav) {
        fav.run(view, unfav + "", 3, id);
    }

    @Event(value = {R.id.rl_kc, R.id.rl_sc, R.id.rl_sp})
    private void getEvent(View v) {
        switch (v.getId()) {
            case R.id.rl_kc://参赛球队
                Bundle bundle = new Bundle();
                bundle.putString("matchid", id);
                goActivity(CSQDActivity.class, bundle);
                break;
            case R.id.rl_sc://数据分析
                goActivity(ActivityDataAnalysis.class);
                break;
            case R.id.rl_sp://赛程表
                break;
        }
    }

    /**
     * 获取比赛详情
     */
    private List<PicInfo> picInfoList = new ArrayList<>();

    private void getDetail() {
        final MapBuilder json = MapBuilder.build();
            json.add("client_id", client_id);
            json.add("state", state);
            json.add("access_token", access_token);
            json.add("action", "getMatchDetail");
            json.add("id", id);

            //‘view_img’ : 1,  //选填 为1时显示全部赛事图片，不填默认显示4张
            //‘view_video’ : 1,  //选填 为1时显示全部赛事视频，不填默认显示6部
            json.add("view_img", "1");
            json.add("view_video", "1");

        BaseActivity.callHttp(json.get(), getView(R.id.content), new FunCallback() {
            @Override
            public void onSuccess(Object result, List object) {
                NetEntity entity = ((NetEntity)result);
                String data = ((NetEntity)result).getData().toString();
                LogUtil.d("------onSuccess---data------->" + data);
                fl_loading.setVisibility(View.GONE);
                BSDetail bsDetail = entity.toObj(BSDetail.class);
                String collect_status = app.base.JsonUtil.findJsonLink("collect_status", entity.getData().toString()).toString();
                String status = app.base.JsonUtil.findJsonLink("status", entity.getData().toString()).toString();


                Map map = MapBuilder.build().add("1", "报名中").add("3", "已结束").add("2", "进行中").add("4", "已报满").add("5", "已报名").get();
                states = map.get(status).toString();
                adapter_bsss_state.setText(states);
                setBaominStatusInBaoming(states);
                MapConf.build().with(BSDetailActivity.this)
                        .pair("collect_status->ivRight_text", "0:mipmap.fav_undo;1:mipmap.fav_done")
                        .pair("order_status->bs_detail_baoming", "", "visible()").source(entity.getData().toString(), BSDetailActivity.this).toView();
                setFaved(!collect_status.equals("0"));
                setBaominStatus(states);
                ImageLoader.getInstance().displayImage(bsDetail.getCover(), adapter_bsss_img, ImageUtil.getOptions(), ImageUtil.getImageLoadingListener(true));

                setSharetitle(bsDetail.getName());
                setSharetxt(bsDetail.getAddress());
                setShareurl(bsDetail.getCover());
                adapter_bsss_title.setText(bsDetail.getName());
                adapter_bsss_date.setText(bsDetail.getStart_time() + " 至 " + bsDetail.getEnd_time());
                adapter_bsss_address.setText(bsDetail.getAddress());
                adapter_bsss_level.setText(bsDetail.getLevel());
                adapter_bsss_people.setText(bsDetail.getType());
                adapter_bsss_syu.setText("还剩" + bsDetail.getSurplus() + "个名额");
                adapter_bsss_pay.setText("¥" + bsDetail.getSell_price().replace(".00", ""));
                if (bsDetail.getDescription().trim().equals("")) {
                    findViewById(R.id.saishijianjie).setVisibility(View.GONE);
                }
                if (states.toString().equals("报名中")) {
                    bs_detail_baoming.setVisibility(View.VISIBLE);
                } else {
                    bs_detail_baoming.setVisibility(View.INVISIBLE);
                }
                bs_detail_content.loadData(bsDetail.getDescription(), "text/html;charset=UTF-8", null);
                // 订单状态：null未报名 0未支付 1已支付
                orderStatus = app.base.JsonUtil.findJsonLink("order_status", entity.getData().toString()).toString();
                if(!StringUtil.isEmpty(orderStatus) && !orderStatus.equals("null")) {
                    bs_detail_baoming.setVisibility(View.GONE);
                }
                bs_detail_baoming.setText("我要报名(￥" + bsDetail.getSell_price().replace(".00", "") + ")");

                picInfoList = bsDetail.toList(PicInfo.class);
                ((TextView) findViewById(R.id.text_apply)).setText(bsDetail.getApply_num() + "/" + bsDetail.getQuota());
//                if (bsDetail.getApply_num().equals("0")) {
//                    findViewById(R.id.rl_kc).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            showToast("当前没有参赛球队");
//                        }
//                    });
//                }
                if(picInfoList!=null){
                    picAdapter.addAll(picInfoList.subList(0, 3));
                }
                if (picAdapter.getCount() == 0) {
                    findViewById(R.id.pictitle).setVisibility(View.GONE);
                }
                MapAdapter.AdaptInfo adaptinfo = new MapAdapter.AdaptInfo();
                adaptinfo.addListviewItemLayoutId(R.layout.adapter_bsdetail_shipin);
                adaptinfo.addViewIds(new Integer[]{R.id.news_name});
                adaptinfo.addObjectFields(new String[]{"name"});
                MapAdapter mapAdapter = new MapAdapter(BSDetailActivity.this, adaptinfo);
                mapAdapter.setItemDataSrc(new MapContent(JsonUtil.extractJsonRightValue(bsDetail.getMatch_video().toString())));
                bs_detail_video.setAdapter(mapAdapter);
                bs_detail_video.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Bundle b = new Bundle();
                        b.putString("fileurl", ((Map) adapterView.getItemAtPosition(i)).get("fileurl").toString());
                        b.putString("name", ((Map) adapterView.getItemAtPosition(i)).get("name").toString());

                        startActivity(new Intent(getBaseContext(), BSVideoActivity.class).putExtra("videoid", ((Map) adapterView.getItemAtPosition(i)).get("id").toString()));
                    }
                });
                if (bs_detail_video.getCount() == 0) {
                    bs_detail_ll_video.setVisibility(View.GONE);
                }

                try {
                    JSONObject jsonObject = new JSONObject(data);
                    JSONArray schedule = jsonObject.optJSONArray("schedule");
                    if(schedule != null && schedule.length() > 0) {
                        Gson gson = new Gson();
                        for (int i = 0; i < schedule.length(); i++) {
                            JSONObject scheduleItem = schedule.optJSONObject(i);
                            Schedule scheduleBase = gson.fromJson(scheduleItem.toString(), Schedule.class);
                            mListSchedule.add(scheduleBase);
                        }
                    }
                    bs_detail_ll__listView.setVisibility(View.VISIBLE);
                    if(mListSchedule != null && mListSchedule.size() > 0) {
                        // 显示对决列表
                        adapter = new ScheduleAdapter(getBaseContext(), mListSchedule);
                        bs_detail_listView.setAdapter(adapter);
                    } else {
                        // TODO 显示假数据：
//                        mListSchedule = new Schedule().getData();
//                        adapter = new ScheduleAdapter(getBaseContext(), mListSchedule);
//                        bs_detail_listView.setAdapter(adapter);
                    }
                    isCurrentScStatus = getFaved();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getDetail();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        boolean scStatus = getFaved();
        if(isCurrentScStatus != scStatus)
            setResult(RESULT_OK);
    }

}
