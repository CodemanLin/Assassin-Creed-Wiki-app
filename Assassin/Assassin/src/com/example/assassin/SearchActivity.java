package com.example.assassin;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class SearchActivity extends Activity{

	String body;
	String content = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_list);
		
		Bundle extras = getIntent().getExtras();//获取参数
		if (extras != null) {
			content = extras.getString("content");
		}
		
		String url = "http://119.23.206.8:8080/api/search?content="+content;
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
        Data data = new Gson().fromJson(body,Data.class);
		if(data.code != 200){
			String result = new Gson().toJson(data.data);
			Result res = new Gson().fromJson(result,Result.class);
			Toast.makeText(this, res.result.toString(), Toast.LENGTH_SHORT).show();
		}else{
			String result = new Gson().toJson(data.data);
			Result res = new Gson().fromJson(result,Result.class);
			String resData = new Gson().toJson(res.result);
			List<Text> textlist = new Gson().fromJson(resData,new TypeToken<List<Text>>(){}.getType());
			LinearLayout layout = (LinearLayout)this.findViewById(R.id.Search);
			for(int i=0;i<textlist.size();i++){
				LinearLayout textLayout = new LinearLayout(this);
				textLayout.setOrientation(1);//vertical
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				layoutParams.setMargins(10, 0, 10, 0);//4个参数按顺序分别是左上右下
				layoutParams.gravity = Gravity.CENTER;
				textLayout.setLayoutParams(layoutParams);
				textLayout.setId(i);
				layout.addView(textLayout);

	        	TextView textViewTitle = new TextView(this);
	        	textViewTitle.setText(textlist.get(i).title);
	        	textViewTitle.setTextSize(20);
	        	//设置上边距
	        	LinearLayout.LayoutParams layoutParamsTextView = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	        	layoutParamsTextView.setMargins(0, 5, 0, 0);//4个参数按顺序分别是左上右下
	        	textViewTitle.setLayoutParams(layoutParamsTextView);
	        	textLayout.addView(textViewTitle);
	        	
	            TextView textViewContent = new TextView(this);
	            textViewContent.setText(textlist.get(i).content);
	            textViewContent.setTextSize(15);
	            textLayout.addView(textViewContent);
	            
	            TextView textViewE = new TextView(this);
	            textViewE.setHeight(5);
	            layout.addView(textViewE);
	            
	            TextView textViewLine = new TextView(this);
	            textViewLine.setBackgroundColor(Color.parseColor("#d3d3d3"));
	            textViewLine.setHeight(1);
	            layout.addView(textViewLine);
	        }
			//多控件点击
			for(int i=0;i<textlist.size();i++){
				final LinearLayout ly= (LinearLayout)layout.getChildAt(i*3).findViewById(i);
				final int textid = textlist.get(i).id;
				ly.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View v){
						Bundle bundle = new Bundle();
		        		bundle.putString("textid", Integer.toString(textid)); //传textid
		        		//把词条页的参数传到编辑页
		        		Intent intent = new Intent(SearchActivity.this, EntryActivity.class);
		        		intent.putExtras(bundle);//加载参数
		        		startActivity(intent);//启动另一activity
					}
				});
			}
		}
	}
	
}
