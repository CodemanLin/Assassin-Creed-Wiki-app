package com.example.assassin;

import java.io.IOException;
import java.util.Vector;

import com.google.gson.Gson;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

	//设置控件的类型和变量名
	EditText user,psw;
	CheckBox store;
	Button button,toreg,cancel;
	TextView errorInfo;
	
	//交互返回的数据
	String body;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//这里引入要用的ui页面	
		setContentView(R.layout.login);
		
		//根据控件的类型以及名字引入对应控件，缺少该控件类型时引入包即可解决
		//用户名输入框控件		
		user = (EditText) this.findViewById(R.id.login_userid);
		//密码输入框控件
		psw = (EditText) this.findViewById(R.id.login_password);
		//记住密码控件
		store = (CheckBox) this.findViewById(R.id.login_storePassword);
        //登录按钮控件
        button = (Button) this.findViewById(R.id.login_ok);
        toreg = (Button) this.findViewById(R.id.to_register_id);
        cancel = (Button) this.findViewById(R.id.login_cancel_id);
        //错误显示控件
        errorInfo = (TextView) this.findViewById(R.id.login_errInfo);
        
        //button的点击事件
        button.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		//获得用户名输入框中的值
        		String username = user.getText().toString().trim();
        		//获得密码输入框中的值
        		String password = psw.getText().toString().trim();
        		//判断用户和密码是否为空，为空显示错误提示
        		if(username.equals("")||password.equals("")){
        			errorInfo.setText("缺少用户名或密码");
        		}else{
        			//非空则继续代码将错误提示显示为空，继续代码
        			errorInfo.setText("");
        			//接口地址，就是我群里给的那些链接
        			//?后面的时变量名和值，把对应值接在变量名后就行
        			String url = "http://119.23.206.8:8080/api/log?username="+username+"&password="+password;
        			//这里不用动，前后交互的
        			OkHttpClient okHttpClient = new OkHttpClient();
        			final Request request = new Request.Builder()
        			        .url(url)
        			        .build();
        			final Call call = okHttpClient.newCall(request);
        			Vector<Thread> threadVector = new Vector<Thread>();
                    Thread httpThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Response response = call.execute();
                                body = response.body().string();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    threadVector.add(httpThread);
                    httpThread.start();
                    //使主进程在子进程执行后再执行
                    for(Thread thread : threadVector){
                        try {
							thread.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
                    }
        			//将数据从json转为对象
        			Object obj = new Object();
        			Data data = new Gson().fromJson(body,Data.class);
        			//取出result中的数据
        			String result = new Gson().toJson(data.data);
        			Result res = new Gson().fromJson(result,Result.class);
        			res.result.toString();
        			//判断是否登录成功
        			if(data.code != 200){
        				//不成功则返回错误
//        				errorInfo.setText(res.result.toString());
        				Toast.makeText(LoginActivity.this, res.result.toString(), Toast.LENGTH_SHORT).show();
        			}else{
//                		if(store.isChecked()){}
                		SharedPreferences mySharedPreferences= getSharedPreferences("data", Activity.MODE_PRIVATE); 
            			SharedPreferences.Editor editor = mySharedPreferences.edit(); 
            			editor.putString("username", username); 
            			editor.putString("token", res.result.toString()); 
            			editor.commit();
                		//登录成功转跳到主页
                		finish();
        			}
        			
        		}
        		
			}
        });
        
        toreg.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        		startActivity(intent);//启动另一activity
			}
        });
        
        cancel.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		finish();
			}
        });
        
		
	}
	
}
