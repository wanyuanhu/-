package com.example.pandong.helloandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends Activity implements View.OnClickListener{
    private Button btn,paizhao,sheying,luyin;
    private PopupWindow myPopupWindow;
    private static final int RESULT_CAPTURE_IMAGE = 1;// 照相的requestCode
    private static final int REQUEST_CODE_TAKE_VIDEO = 2;// 摄像的照相的requestCode
    private static final int RESULT_CAPTURE_RECORDER_SOUND = 3;// 录音的requestCode
    private String strImgPath = "";// 照片文件绝对路径
    private String strVideoPath = "";// 视频文件的绝对路径
    private String strRecorderPath = "";// 录音文件的绝对路径
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button)findViewById(R.id.pdialog);
        paizhao = (Button)findViewById(R.id.pdialog);
        sheying = (Button)findViewById(R.id.sheying);
        luyin = (Button)findViewById(R.id.luyin);
        paizhao.setOnClickListener(this);
        sheying.setOnClickListener(this);
        luyin.setOnClickListener(this);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupWindow();
            }
        });
        // 对电话的来电状态进行监听
        TelephonyManager telManager = (TelephonyManager) this
                .getSystemService(Context.TELEPHONY_SERVICE);
        // 注册一个监听器对电话状态进行监听
        telManager.listen(new MyPhoneStateListener(),
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.paizhao:setPaizhao();break;
            case R.id.sheying:setSheying();break;
            case R.id.luyin:setLuyin();break;
            default:break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MainActivity","requestCode="+requestCode);
        switch (requestCode) {
            case RESULT_CAPTURE_IMAGE://拍照
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, strImgPath, Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_CODE_TAKE_VIDEO://拍摄视频
                if (resultCode == RESULT_OK) {
                    Uri uriVideo = data.getData();
                    Cursor cursor=this.getContentResolver().query(uriVideo, null, null, null, null);
                    if (cursor.moveToNext()) {
                        /** _data：文件的绝对路径 ，_display_name：文件名 */
                        strVideoPath = cursor.getString(cursor.getColumnIndex("_data"));
                        Toast.makeText(this, strVideoPath, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case RESULT_CAPTURE_RECORDER_SOUND://录音
                if (resultCode == RESULT_OK) {
                    Uri uriRecorder = data.getData();
                    Cursor cursor=this.getContentResolver().query(uriRecorder, null, null, null, null);
                    if (cursor.moveToNext()) {
                        /** _data：文件的绝对路径 ，_display_name：文件名 */
                        strRecorderPath = cursor.getString(cursor.getColumnIndex("_data"));
                        Toast.makeText(this, strRecorderPath, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private void setPaizhao(){
        Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(!Environment.isExternalStorageEmulated()){
            Toast.makeText(MainActivity.this,"外部存储未挂载",Toast.LENGTH_SHORT).show();
            return;
        }
        strImgPath = Environment.getExternalStorageDirectory().toString() + "/CONSDCGMPIC/";//存放照片的文件夹
        String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".jpg";//照片命名
        File out = new File(strImgPath);
        if (!out.exists()) {
            out.mkdirs();
        }
        out = new File(strImgPath, fileName);
        strImgPath = strImgPath + fileName;//该照片的绝对路径
        Uri uri = Uri.fromFile(out);
        imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        imageCaptureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(imageCaptureIntent, RESULT_CAPTURE_IMAGE);
    }
    private void setSheying(){
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        startActivityForResult(intent, REQUEST_CODE_TAKE_VIDEO);
    }
    private void setLuyin(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/amr");
        startActivityForResult(intent, RESULT_CAPTURE_RECORDER_SOUND);
    }

    /**
     * 电话监听录音
     */
    private class MyPhoneStateListener extends PhoneStateListener {
        MediaRecorder recorder;
        File audioFile;
        String phoneNumber;

        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE: /* 无任何状态时 */
                    if (recorder != null) {
                        recorder.stop(); //停止刻录
                        recorder.reset(); //重设
                        recorder.release(); //刻录完成一定要释放资源
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK: /* 接起电话时 */

                    try {
                        recorder = new MediaRecorder();
                        recorder.setAudioSource(MediaRecorder.AudioSource.MIC); // 设置音频采集原
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); //内容输出格式
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); //音频编码方式

                        // recorder.setOutputFile("/sdcard/myvoice.amr");
                        audioFile = new File(
                                Environment.getExternalStorageDirectory(),
                                phoneNumber + "_" + System.currentTimeMillis()
                                        + ".3gp");
                        recorder.setOutputFile(audioFile.getAbsolutePath());
                        Log.i("TAG", audioFile.getAbsolutePath());

                        recorder.prepare(); //预期准备
                        recorder.start();

                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                case TelephonyManager.CALL_STATE_RINGING: /* 电话进来时 */
                    phoneNumber = incomingNumber;
                    break;
                default:
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }
    private void showPopupWindow(){
        View v = LayoutInflater.from(this).inflate(R.layout.my_dialog,null,false);
        TextView tv = (TextView)v.findViewById(R.id.textView);
        tv.setText("通过popupWindows 得到的dialog");
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myPopupWindow.dismiss();
            }
        });
        myPopupWindow = new PopupWindow(v,ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myPopupWindow.setFocusable(true);
        myPopupWindow.setOutsideTouchable(true);
        myPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.BLUE));
        myPopupWindow.showAsDropDown(btn);//showPopupWindow(v);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
