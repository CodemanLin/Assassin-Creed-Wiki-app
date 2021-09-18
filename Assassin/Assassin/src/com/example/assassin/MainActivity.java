package com.example.assassin;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	Menu game, character, view, place;

	// 界面内容
	TextView edit; // 对应蓝色编辑按钮
	TextView title; // 对应标题
	TextView content; // 对应正文内容
	EditText comment_info; // 对应评论的内容
	Button comment; // 对应确定发表评论按钮
	LinearLayout layout;

	String body;
	String sort_body;
	String cmt_body;

	String textid = "1";

	public void onResume() {
		super.onResume();
		onCreate(null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		String url = "http://119.23.206.8:8080/api/text?id=" + textid;
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
			
			
			edit = (TextView)this.findViewById(R.id.main_edit_btn);
	        title = (TextView)this.findViewById(R.id.main_title_id);
	        content = (TextView)this.findViewById(R.id.main_content_id);
	        comment_info = (EditText)this.findViewById(R.id.main_comment_input);
	        comment = (Button)this.findViewById(R.id.main_comment_btn);
			
	        //插入数据
			title.setText(text.title);
			content.setText(text.content);
			
	        //点击首页标题旁的“编辑”按钮，则跳转到编辑页，同时在编辑页显示首页的信息
	        edit.setOnClickListener(new OnClickListener() {
	        	@Override
	        	public void onClick(View v){
	        		Bundle bundle = new Bundle();
	        		bundle.putString("title", title.getText().toString()); //传标题
	        		bundle.putString("content", content.getText().toString()); //传正文
	        		bundle.putString("textid", textid.toString()); //传textid
	        		//把首页的参数传到编辑页
	        		Intent intent1 = new Intent(MainActivity.this, ChangeActivity.class);
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
	        			Toast.makeText(MainActivity.this, "不能输入空的评论", Toast.LENGTH_SHORT).show();
	        		}
	        		else{
	        			//评论的接口
	        			String url = "http://119.23.206.8:8080/api/cmt?token="+token+"&textid=1&cmt=" + comments;
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
	        				Toast.makeText(MainActivity.this, res.result.toString(), Toast.LENGTH_SHORT).show();
	        			}else{
	        				onCreate(null);
	        			}
	        		}
	        	}
	        });
	        
		}
		
		
	}

	/**
	 * 创建选项菜单事件
	 * 
	 * @param menu
	 * @return
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// 可以采用代码
		// 创建选项菜单
		getMenuInflater().inflate(R.menu.main_menu, menu);

		Menu game = menu.addSubMenu("游戏作品").setIcon(
				android.R.drawable.ic_menu_gallery);
		Menu character = menu.addSubMenu("角色").setIcon(
				android.R.drawable.ic_menu_my_calendar);
		Menu view = menu.addSubMenu("世界观").setIcon(
				android.R.drawable.ic_menu_myplaces);
		Menu place = menu.addSubMenu("地点").setIcon(
				android.R.drawable.ic_menu_mapmode);

		String[] sort = { "游戏作品", "角色", "世界观", "地点" };
		Menu[] menus = { game, character, view, place };

		for (int i = 0; i < sort.length; i++) {
			List<Text> textlist = getSort(sort[i]);
			for (int j = 0; j < textlist.size(); j++) {
				Text text = textlist.get(j);
				menus[i].add(i, text.id, i, text.title);
			}
		}

		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * 关于菜单点击事件
	 * 
	 * @param item
	 */
	public void onAboutMenu(MenuItem item) {
		// 通知系统刷新Menu
		// invalidateOptionsMenu();
		// 跳转到登录界面
		Intent intent1 = new Intent(MainActivity.this, LoginActivity.class);
		startActivity(intent1);// 启动另一activity
	}

	public List<Text> getSort(String sort) {
		String url = "http://119.23.206.8:8080/api/sort?sort=" + sort;
		OkHttpClient okHttpClient = new OkHttpClient();
		final Request request = new Request.Builder().url(url).build();
		final Call call = okHttpClient.newCall(request);
		Vector<Thread> threadVector = new Vector<Thread>();
		Thread httpThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Response response = call.execute();
					sort_body = response.body().string();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		threadVector.add(httpThread);
		httpThread.start();
		// 使主进程在子进程执行后再执行
		for (Thread thread : threadVector) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		Data data = new Gson().fromJson(sort_body, Data.class);
		if (data.code != 200) {
			return new ArrayList<Text>();
		} else {
			String result = new Gson().toJson(data.data);
			Result res = new Gson().fromJson(result, Result.class);
			String resData = new Gson().toJson(res.result);
			List<Text> textlist = new Gson().fromJson(resData,
					new TypeToken<List<Text>>() {
					}.getType());
			return textlist;
		}
	}

	/**
	 * 选项菜单被选中事件
	 * 
	 * @param item
	 * @return
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_search) {
			showNormalDia();
			return true;
		}
		if (item.getItemId() > 0) {
			Bundle bundle = new Bundle();
			bundle.putString("textid", Integer.toString(item.getItemId())); // 传textid
			// 把词条页的参数传到编辑页
			Intent intent1 = new Intent(MainActivity.this, EntryActivity.class);
			intent1.putExtras(bundle);// 加载参数
			startActivity(intent1);// 启动另一activity
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 选项菜单打开后事件，解决溢出菜单的图标显示问题
	 * 
	 * @param item
	 * @return
	 */
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
			if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
				try {
					Method m = menu.getClass().getDeclaredMethod(
							"setOptionalIconsVisible", Boolean.TYPE);
					m.setAccessible(true);
					m.invoke(menu, true);
				} catch (Exception e) {
				}
			}
		}
		return super.onMenuOpened(featureId, menu);
	}

	/* 搜索框 */
	private void showNormalDia() {
		AlertDialog.Builder normalDia = new AlertDialog.Builder(
				MainActivity.this);
		normalDia.setIcon(R.drawable.ic_search);
		final EditText text = new EditText(MainActivity.this);
		normalDia.setView(text);
		normalDia.setTitle("搜索");
		normalDia.setMessage("搜索内容");
		normalDia.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String content = text.getText().toString();
						Bundle bundle = new Bundle();
						bundle.putString("content", content); // 传textid
						// 把词条页的参数传到编辑页
						Intent searchintent = new Intent(MainActivity.this,
								SearchActivity.class);
						searchintent.putExtras(bundle);// 加载参数
						startActivity(searchintent);// 启动另一activity
					}
				});
		normalDia.setNegativeButton("取消",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		normalDia.create().show();
	}

}
