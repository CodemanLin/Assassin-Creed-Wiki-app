package com.example.assassin;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class EntryActivity extends Activity{
	
	TextView title,content,edit;
	LinearLayout layout;
	EditText comment_info;	//对应评论的内容
	Button comment;			//对应确定发表评论按钮
	
	String body;
	String cmt_body;
	
	String textid = "1";
	
	public void onResume(){
		super.onResume();
		onCreate(null);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//这里引入要用的ui页面	
		setContentView(R.layout.entry);
		
		
		Bundle extras = getIntent().getExtras();//获取参数
		if (extras != null) {
			textid = extras.getString("textid");//得到健为textid的值
		}
		
		String url = "http://119.23.206.8:8080/api/text?id="+textid;
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
        //交互获得评论信息
        String cmt_url = "http://119.23.206.8:8080/api/getcmt?textid=" + textid;
		OkHttpClient okCmtHttpClient = new OkHttpClient();
		final Request requestCmt = new Request.Builder()
		        .url(cmt_url)
		        .build();
		final Call callCmt = okCmtHttpClient.newCall(requestCmt);
		Vector<Thread> threadVectorCmt = new Vector<Thread>();
        Thread httpThreadCmt = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = callCmt.execute();
                    cmt_body = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        threadVectorCmt.add(httpThreadCmt);
        httpThreadCmt.start();
        //使主进程在子进程执行后再执行
        for(Thread thread : threadVectorCmt){
            try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        
        //将数据从json转为对象
		Data data = new Gson().fromJson(body,Data.class);
		Data cmtData = new Gson().fromJson(cmt_body,Data.class);
		//判断有否获取数据
		if(data.code != 200){
			String result = new Gson().toJson(data.data);
			Result res = new Gson().fromJson(result,Result.class);
//			errorInfo.setText(res.result.toString());
		}else{
			String result = new Gson().toJson(data.data);
			Result res = new Gson().fromJson(result,Result.class);
			String resData = new Gson().toJson(res.result);
			List<Text> textlist = new Gson().fromJson(resData,new TypeToken<List<Text>>(){}.getType());
			Text text = textlist.get(0);
			
			title = (TextView) this.findViewById(R.id.title_id);
			content = (TextView) this.findViewById(R.id.content_id);
			edit = (TextView) this.findViewById(R.id.edit_btn);
			//插入数据
			title.setText(text.title);
			content.setText(text.content);
			
			//编辑页面跳转
			edit.setOnClickListener(new OnClickListener() {
	        	
	        	public void onClick(View v){
	        		Bundle bundle = new Bundle();
	        		bundle.putString("title", title.getText().toString()); //传标题
	        		bundle.putString("content", content.getText().toString()); //传正文
	        		bundle.putString("textid", textid.toString()); //传textid
	        		//把词条页的参数传到编辑页
	        		Intent intent1 = new Intent(EntryActivity.this, ChangeActivity.class);
	        		intent1.putExtras(bundle);//加载参数
	        		startActivity(intent1);//启动另一activity
	        		
	        		
	        	}
	        });
			
			if(cmtData.code == 200){
	        	String resultCmt = new Gson().toJson(cmtData.data);
				Result resCmt = new Gson().fromJson(resultCmt,Result.class);
				String resDataCmt = new Gson().toJson(resCmt.result);
				List<Comment> cmtList = new Gson().fromJson(resDataCmt,new TypeToken<List<Comment>>(){}.getType());
	        	//获得评论
		        LinearLayout layout = (LinearLayout)this.findViewById(R.id.comment);
		        for(int i=0;i<cmtList.size();i++){
		        	TextView textViewUser = new TextView(this);
		        	textViewUser.setText(cmtList.get(i).username+"：");
		            layout.addView(textViewUser);
		            TextView textViewCmt = new TextView(this);
		            textViewCmt.setText(cmtList.get(i).comment);
		            layout.addView(textViewCmt);
		            TextView textViewE = new TextView(this);
		            textViewE.setText(" ");
		            textViewE.setHeight(5);
		            layout.addView(textViewE);
		        }
	        }
			
			comment_info = (EditText)this.findViewById(R.id.comment_input);
	        comment = (Button)this.findViewById(R.id.comment_btn);
			
			//点击确定发表评论
	        comment.setOnClickListener(new OnClickListener() {
	        	@Override
	        	public void onClick(View v){
	        		String token = "";
	        		SharedPreferences sharedPreferences= getSharedPreferences("data", Activity.MODE_PRIVATE); 
	        		if (sharedPreferences != null) {
	        			token = sharedPreferences.getString("token", "");
	        		}
	        		//获得评论框的值
	        		String comments = comment_info.getText().toString().trim();
	        		
	        		if(comments.equals("")){
	        			Toast.makeText(EntryActivity.this, "不能输入空的评论", Toast.LENGTH_SHORT).show();
	        		}
	        		else{
	        			//评论的接口
	        			String url = "http://119.23.206.8:8080/api/cmt?token="+token+"&textid="+textid+"&cmt=" + comments;
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
	        			
	        			//判断是否成功发表评论
	        			if(data.code != 200){
	        				//不成功则返回错误
	        				Toast.makeText(EntryActivity.this, res.result.toString(), Toast.LENGTH_SHORT).show();
	        			}else{
	                		//评论成功刷新页面
	        				onCreate(null);
	        			}
	        		}
	        	}
	        });
			
		}
		
	}
	
	
}
