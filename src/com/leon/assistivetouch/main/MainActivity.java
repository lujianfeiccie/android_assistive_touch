package com.leon.assistivetouch.main;

import com.leon.assistivetouch.main.util.L;
import com.leon.assistivetouch.main.util.MemoryCache;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;


/** 
 * 类名      MainActivity.java
 * 说明   主配置界面
 * 创建日期 2012-8-21
 * 作者  LiWenLong
 * Email lendylongli@gmail.com
 * 更新时间  $Date$
 * 最后更新者 $Author$
*/
public class MainActivity extends Activity implements OnClickListener{
	
	private static final String TAG = "MainActivity";
	
	private Button mServiceBtn;
	private IAssistiveTouchService mService;
	private boolean isRunning;
	private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initView();
        mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				connect ();
			}
		}, 1500);
    }
    
    @Override
    public void onDestroy () {
    	super.onDestroy();
    	if (mService != null) {
    		getApplicationContext().unbindService(mServiceConn);
    	}
    	MemoryCache.clear();
    }
    

	private void init () {
    }
    private void initView () {
    	mServiceBtn = (Button) findViewById(R.id.service_start_stop_btn);
    	mServiceBtn.setEnabled(false);
    	mServiceBtn.setOnClickListener(this);
    }
    
    private void changeButtonStatu (boolean isStart) {
    	mServiceBtn.setText(isStart ? R.string.stop_service : R.string.start_service);
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.service_start_stop_btn:
			if (mService != null) {
				try {
					if (isRunning) {
						mService.stop();
					} else {
						mService.start();
					}
					isRunning = mService.isRunning();
					changeButtonStatu(isRunning);
				} catch (RemoteException e) {
					L.e(TAG, "", e);
				}
			} else {
				L.d(TAG, "mService == null");
			}
			break;
		}
	}
	
	/**
	 * 绑定服务
	 * */
	private void connect () {
		L.d(TAG, "connect ...");
		Intent i = new Intent(AssistiveTouchService.ASSISTIVE_TOUCH_START_ACTION);
		getApplicationContext().bindService(i, mServiceConn, Context.BIND_AUTO_CREATE);
	}
	
	private ServiceConnection mServiceConn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			L.d(TAG, "onServiceDisconnected");
			mService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			L.d(TAG, "onServiceConnected");
			mServiceBtn.setEnabled(true);
			mService = IAssistiveTouchService.Stub.asInterface(service);
			try {
				isRunning = mService.isRunning();
				L.d(TAG, "service is running:" + isRunning);
				changeButtonStatu (isRunning);
			} catch (RemoteException e) {
				L.e(TAG, "", e);
			}
		}
	};
}
