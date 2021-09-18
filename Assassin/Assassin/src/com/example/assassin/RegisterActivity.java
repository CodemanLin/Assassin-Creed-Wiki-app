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

public class RegisterActivity extends Activity {

	//设置控件的类型和变量名
	EditText user,psw,check_psw;
	Button button;
	Button cancel;
	TextView errorInfo;
	
	//交互返回的数据
	String body;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//这里引入要用的ui页面
		setContentView(R.layout.register);
		
		user = (EditText) this.findViewById(R.id.login_userid);
		psw = (EditText) this.findViewById(R.id.login_password);
		check_psw = (EditText) this.findViewById(R.id.check_psw);
		button = (Button) this.findViewById(R.id.login_reg);
		cancel = (Button) this.findViewById(R.id.reg_cancel_id);
		errorInfo = (TextView) this.findViewById(R.id.login_errInfo);
		
		button.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		//获得用户名输入框中的值
        		String username = user.getText().toString().trim();
        		//获得密码输入框中的值
        		String password = psw.getText().toString().trim();
        		String check_password = check_psw.getText().toString().trim();
        		//判断用户和密码是否为空，为空显示错误提示
        		if(username.equals("")||password.equals("")){
        			errorInfo.setText("缺少用户名或密码");
        		}else if(!password.equals(check_password)){
        			errorInfo.setText("两次输入的密码不同");
        		}else{
        			//非空则继续代码将错误提示显示为空，继续代码
        			errorInfo.setText("");
        			//接口地址，就是我群里给的那些链接
        			//?后面的时变量名和值，把对应值接在变量名后就行
        			String url = "http://119.23.206.8:8080/api/reg?username="+username+"&password="+password;
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
        				Toast.makeText(RegisterActivity.this, res.result.toString(), Toast.LENGTH_SHORT).show();
        			}else{
                		//注册成功暂停一秒转跳到登录页
//        				errorInfo.setText(res.result.toString());
        				Toast.makeText(RegisterActivity.this, res.result.toString(), Toast.LENGTH_SHORT).show();
        				try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
//                		Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
//                		startActivity(intent);//启动另一activity
        				finish();
        			}
        			
        		}
        		
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
