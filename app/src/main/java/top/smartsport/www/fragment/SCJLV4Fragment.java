package top.smartsport.www.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import app.base.MapConf;
import intf.FunCallback;
import intf.JsonUtil;
import intf.MapBuilder;
import top.smartsport.www.R;
import top.smartsport.www.activity.CoachDetailActivity;
import top.smartsport.www.adapter.SCCoatchAdapter;
import top.smartsport.www.base.BaseActivity;
import top.smartsport.www.base.BaseV4Fragment;
import top.smartsport.www.bean.Coaches;
import top.smartsport.www.bean.NetEntity;
import top.smartsport.www.listview_pulltorefresh.PullToRefreshBase;
import top.smartsport.www.listview_pulltorefresh.PullToRefreshListView;

/**
 * Created by Aaron on 2017/7/24.
 * 教练--收藏教练
 */
@ContentView(R.layout.fragment_scjl)
public class SCJLV4Fragment extends BaseV4Fragment {
    public static final String TAG = "SCJLV4Fragment";
    @ViewInject(R.id.pullrefreshlistview)
    PullToRefreshListView pullrefreshlistview;
    @ViewInject(R.id.mykcempty)
    ViewGroup empty;
    private int page =1;
    private SCCoatchAdapter mAdapter;
    private List mList;
//    private List<Coaches> list;

    public static SCJLV4Fragment newInstance() {
        SCJLV4Fragment fragment = new SCJLV4Fragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;

    }


    @Override
    protected void initView() {
        mAdapter = new SCCoatchAdapter(false);
        pullrefreshlistview.getRefreshableView().setAdapter(mAdapter);
        pullrefreshlistview.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                page =1;
                reload(true);

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                page ++;
                reload(false);
            }
        });
        reload(true);
        pullrefreshlistview.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                startActivityForResult(new Intent(getActivity(), CoachDetailActivity.class).putExtra("id", ((Map)mList.get(i)).get("id").toString()), 101);
            }
        });
    }



    private void reload(final boolean refresh) {
        BaseActivity.callHttp(MapBuilder.build().add("action", "getMyCollection").add("page", page).add("type", 5).get(), new FunCallback() {
            @Override
            public void onCallback(Object result, List object) {

            }

            @Override
            public void onFailure(Object result, List object) {
                if (refresh){
                    pullrefreshlistview.onPullDownRefreshComplete();
                }else {
                    pullrefreshlistview.onPullUpRefreshComplete();
                }
                empty.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSuccess(Object result, List object) {
                if (refresh){
                    pullrefreshlistview.onPullDownRefreshComplete();
                    mList = new ArrayList();
                }else {
                    pullrefreshlistview.onPullUpRefreshComplete();
                }
                String data = ((NetEntity)result).getData().toString();
                List list = new ArrayList();
                try {
                    list = (List<Coaches>) app.base.JsonUtil.extractJsonRightValue(JsonUtil.findJsonLink("coaches", data).toString());
                }catch(Exception e){
                    mList.clear();
                    list.clear();
                }
                for(int i=0; i<list.size(); i++) {
                    if(list.get(i) != null && !list.get(i).equals("null")) {
                        mList.add(list.get(i));
                    }
                }
                if (mList != null && mList.size() > 0){
                    empty.setVisibility(View.GONE);
                }else{
                    empty.setVisibility(View.VISIBLE);
                }
                mAdapter.setData(mList);
                MapConf mc = MapConf.with(getActivity())
                        .pair("header_url->iv_head_icon")
                        .pair("name->tv_coach_name")
                        .pair("team_name->tv_team")
                        .source(R.layout.coach_list);
                MapConf.with(getActivity()).conf(mc).source(mList, pullrefreshlistview.getRefreshableView()).toView();

            }

        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 101:
                reload(true);
                break;
            default:
                break;
        }
    }

}
