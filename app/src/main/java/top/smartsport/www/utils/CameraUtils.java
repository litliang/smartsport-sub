package top.smartsport.www.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import static android.content.ContentValues.TAG;

/**
 * Created by gst-pc on 2017/10/19.
 */

public class CameraUtils {

    public void chooseCamera(Context context, String iconName, int codeIndex){
        Intent in = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 判断存储卡是否可以用，可用进行存储
        if (AppUtil.hasSdcard()) {
            in.putExtra("return-data", false);
            in.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            in.putExtra("noFaceDetection", true);
            in.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(new File(Environment.getExternalStorageDirectory(), iconName)));
        }else{
            Toast.makeText(context, "SD卡不存在，请插入SD卡", Toast.LENGTH_LONG).show();
            return;
        }
        ((Activity)context).startActivityForResult(in, codeIndex);
    }

    public void choosePicture(Context context, int codeIndex){
        Intent in = new Intent(Intent.ACTION_PICK);
        in.setType("image/*");
        ((Activity)context).startActivityForResult(in, codeIndex);
    }

    /**
     * 裁剪图片
     *
     * @param uri
     */
    public void startPhotoZoom(Context context, Uri uri, int codeIndex) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪 crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("noFaceDetection", true);
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 320);
        intent.putExtra("outputY", 320);
        intent.putExtra("return-data", true);
        ((Activity)context).startActivityForResult(intent, codeIndex);
    }

    // 保存到本地
    public String saveIcon(Context context, Bitmap bitmap, String iconName) {
        //头像存本地
        File baseFile = FileHelper.getBaseFile(FileHelper.PATH_PHOTOGRAPH);
        if (baseFile == null) {
            Toast.makeText(context, "SD卡不存在，请插入SD卡",
                    Toast.LENGTH_LONG).show();
            return "";
        }
        FileHelper.saveBitmap(bitmap, iconName, baseFile);
        String imagePath = Environment
                .getExternalStorageDirectory()
                + File.separator
                + FileHelper.PATH_PHOTOGRAPH + iconName;
        Log.d(TAG, "------saveIcon----imagePath-------->" + imagePath);
        return imagePath;
    }

}
