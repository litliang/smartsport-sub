package top.smartsport.www.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.io.File;

import activitybase.CallbackActivity;
import top.smartsport.www.R;
import top.smartsport.www.utils.AppUtil;
import top.smartsport.www.utils.CameraUtils;

/**
 * Created by bajieaichirou on 17/3/4.
 * 选择拍照 相册
 */
public class ActivityChooseIcon extends CallbackActivity implements View.OnClickListener{

    private TextView mCameraTxt, mPictureTxt, mCancelTxt;

    private final int CODE_CHOOSE_ICON_CAMERA = 4;
    private final int CODE_CHOOSE_ICON_PICTURE = 5;
    private final int CODE_CHOOSE_ICON_ZOOM = 6;
    private final String ICON_NAME = "ICON.jpg";
    private Context mContext;
    private Bitmap iconBitMap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_choose_icon);
        initUI();
    }

    private void initUI() {
        mContext = ActivityChooseIcon.this;
        mCameraTxt = (TextView) findViewById(R.id.choose_camera);
        mPictureTxt = (TextView) findViewById(R.id.choose_picture);
        mCancelTxt = (TextView) findViewById(R.id.choose_cancel);

        mCameraTxt.setOnClickListener(this);
        mPictureTxt.setOnClickListener(this);
        mCancelTxt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.choose_camera:
                new CameraUtils().chooseCamera(mContext,
                        ICON_NAME, CODE_CHOOSE_ICON_CAMERA);
                break;
            case R.id.choose_picture:
                new CameraUtils(). choosePicture(mContext, CODE_CHOOSE_ICON_PICTURE);
                break;
            case R.id.choose_cancel:
                this.finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String temp;
        if (requestCode == CODE_CHOOSE_ICON_CAMERA){//相机返回
            new CameraUtils().startPhotoZoom(mContext,
                    Uri.fromFile(new File(Environment.getExternalStorageDirectory(), ICON_NAME)), CODE_CHOOSE_ICON_ZOOM);
        }else if ((data != null) &&(requestCode == CODE_CHOOSE_ICON_PICTURE)){//图册返回
            new CameraUtils().startPhotoZoom(mContext, data.getData(), CODE_CHOOSE_ICON_ZOOM);
        }else if ((data != null) && (requestCode == CODE_CHOOSE_ICON_ZOOM)){//裁剪完后
            Bundle extras = data.getExtras();
            if (extras != null) {
                iconBitMap = AppUtil.toRoundBitmap((Bitmap)extras.getParcelable("data"));
                String path = new CameraUtils().saveIcon(mContext, iconBitMap, ICON_NAME);
                setResult(Activity.RESULT_OK, new Intent().putExtra("path", path));
                finish();
            }
        }
    }

}
