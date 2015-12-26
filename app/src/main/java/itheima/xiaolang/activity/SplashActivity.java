package itheima.xiaolang.activity;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import itheima.xiaolang.R;
import itheima.xiaolang.utils.PackageInfoUtils;
import itheima.xiaolang.utils.StreamTools;

public class SplashActivity extends AppCompatActivity {

    private static final int SHOW_UPDATED_DIALOG = 1;
    private TextView tv_splash_version;
    private String version;
    private static final int ERROR=2;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SHOW_UPDATED_DIALOG://提示用户更新对话框
                    String desc = (String) msg.obj;
                    showDialog(desc);
                break;
                case ERROR:
                Toast.makeText(SplashActivity.this,"错误码:"+msg.obj,Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void showDialog(String desc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(desc);
        builder.setPositiveButton("立刻升级",null);
        builder.setNegativeButton("下次再说",null);
        builder.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        version = PackageInfoUtils.getPackageVersion(this);
        tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);
        tv_splash_version.setText("版本号:"+ version);
        new Thread(new CheckVersion()).start();
    }
    class CheckVersion implements Runnable{
        @Override
        public void run() {
            String path = getResources().getString(R.string.urlVersion);
            try {
                URL url = new URL(path);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                int code = connection.getResponseCode();
                if(code ==200){
                    InputStream inputStream = connection.getInputStream();
                    String result = StreamTools.readStream(inputStream);
                    JSONObject jsob = new JSONObject(result);
                    String realversion = jsob.getString("version");
                    String desc = jsob.getString("description");
                    if(version.equals(realversion)){
                        Log.i("SplashActivity","版本最新,无需更新");
                    }else{
                        Log.i("SplashActivity","版本号不一致,提示用户升级");
                        Message msg = Message.obtain();
                        msg.obj = desc;
                        msg.what = SHOW_UPDATED_DIALOG;
                        handler.sendMessage(msg);
                    }

                }else{
                    //服务器出问题
                    Message msg = Message.obtain();
                    msg.what = ERROR;
                    msg.obj = 410;
                    handler.sendMessage(msg);
                }
            } catch (MalformedURLException e) {
                //服务器URL编写错误
                Message msg = Message.obtain();
                msg.what = ERROR;
                msg.obj = 405;
                handler.sendMessage(msg);
                e.printStackTrace();
            } catch (IOException e) {
                //网络错误
                Message msg = Message.obtain();
                msg.what = ERROR;
                msg.obj = 408;
                handler.sendMessage(msg);
                e.printStackTrace();
            } catch (JSONException e) {
                //网络json文件配置有问题
                Message msg = Message.obtain();
                msg.what = ERROR;
                msg.obj = 409;
                handler.sendMessage(msg);
                e.printStackTrace();
            }catch (Resources.NotFoundException e){
                //服务器路径有问题
                Message msg = Message.obtain();
                msg.what = ERROR;
                msg.obj = 404;
                handler.sendMessage(msg);
                e.printStackTrace();
            }

        }
    }
}
