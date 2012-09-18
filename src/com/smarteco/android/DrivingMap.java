package com.smarteco.android;

import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapController;

public class DrivingMap extends MapActivity implements SensorEventListener{
	MapView mapView;
	Context mContext;
	LocationOverlay mMyOverlay;
	LocationManager mLocationManager;
	LocationProvider mLocationProvider;
	EditText EditText1;
	Button btndraw;
	EditText accMap;
	MapController mapController;
	 Drawable marker = null;
//	SensorActivity accsensor;
	Canvas canvas;
	//FacebookPosting fbPosting;
	 private Facebook mFacebook = new Facebook(C.FACEBOOK_APP_ID);
	
	
	//////////////////////////////////
	SensorManager sm;
	SensorEventListener accL;
	Sensor accSensor; // ���ӵ�

	private long lastTime; 
	
	float beforeaccx;
	float beforeaccy;
	float beforeaccz;
	
	float accx;
	float accy;
	float accz;
	
	float accScala;
	float beforeaccScala=0;
	float nowacc;
	
	Button _facebookPosting;
	Button _drivingStart;
	Button _drivingFinish;

	TextView acc;
	TextView distance;
	TextView nowstate;
	/////////////////////////////////////
	
	View.OnClickListener bHandler = new View.OnClickListener(){
		public void onClick(View v){
			switch(v.getId()){	
			case R.id.button1:
				 Log.v("test","�������1");
				 login();
				 mMyOverlay.locstate(1);
				 mMyOverlay.drawstate(2);
				 nowstate.setText("���������Դϴ�\n���������ϼ���");
				 Log.v("test","���� ����2");
				 _drivingStart.setVisibility(View.INVISIBLE);
				 _drivingFinish.setVisibility(View.VISIBLE);
				break;
			case R.id.button2:
				mMyOverlay.locstate(2);
				 mMyOverlay.drawstate(1);				
				 nowstate.setText("������ ����Ǿ����ϴ�");
				 _drivingFinish.setVisibility(View.INVISIBLE);
				 _facebookPosting.setVisibility(View.VISIBLE);
				break;
			case R.id.button3:
				 Log.v("test","facebook");
				 try {
					 Log.v("test","logintest");
					 Log.v("test","login");
					 screenshot(mapView);
					 Log.v("test","screen compl");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 Log.v("test","facebook-end");
				break;
		}
		}
	};
	////////////////////////////////////////////////////////////////////////////////////
	 public void onActivityResult(int requestCode, int resultCode, Intent data)
	  {
	    super.onActivityResult(requestCode, resultCode, data);
	    if(resultCode == RESULT_OK)
	    {
	    	if(requestCode == 32665){
	    		mFacebook.authorizeCallback(requestCode,resultCode, data);
	    	}
	    }
	    else{
	    	if(requestCode == 32665)
	    	{
	    		mFacebook.authorizeCallback(requestCode, resultCode, data);
	    	}
	    }
	  }
	  
	  public void login()
	  {
		  mFacebook.authorize2(this, new String[] {"publish_stream, user_photos, email"}, new AuthorizeListener());
	  }
	  private void feed(Bitmap screenshot)
	  {
	   try{
		   Log.v(C.LOG_TAG,"access token :"+mFacebook.getAccessToken());
		 //  File f = new File(mFilePath + File.separator + "test.jpg");
		 //  Log.d("test",mFilePath);
		   byte[] imgData=null;
//		   Bitmap images = BitmapFactory.decodeFile(mFilePath+File.separator+"test.jpg");
//		   Bitmap images = BitmapFactory.decodeFile(mFilePath+"test.jpg");
		   imgData = bitmapToByteArray(screenshot);
		   Bundle params = new Bundle();
//		   params.putString("message",mEtContent.getText().toString());
		   params.putString("message","����");
		   params.putString("picture", "test.jpg");
		   params.putString("link","");
		   params.putString("description", "");
		   params.putByteArray("test.jpg",imgData);
		   
		   mFacebook.request("me/photos",params,"POST");
	   	}
	   catch(Exception e){
		   e.printStackTrace();
	   }
	  }
	  public byte[] bitmapToByteArray(Bitmap bitmap){
		  ByteArrayOutputStream stream = new ByteArrayOutputStream();
		  bitmap.compress(CompressFormat.JPEG,100,stream);
		  byte[] byteArray = stream.toByteArray();
		  Log.v("test","bytearray");
		return byteArray;
		  
	  }
	  public void screenshot(MapView view)throws Exception{
		     view.setDrawingCacheEnabled(true);
		     Bitmap bitmap = view.getDrawingCache();
		     Log.v("test","feed call");
		    feed(bitmap);
		     view.setDrawingCacheEnabled(false);
		    }
	  public class AuthorizeListener implements DialogListener
	  {


		public void onComplete(Bundle values) {
			// TODO Auto-generated method stub
			if(C.D)Log.v(C.LOG_TAG,"::: onComplete :::");
		}


		public void onFacebookError(FacebookError e) {
			// TODO Auto-generated method stub
			
		}


		public void onError(DialogError e) {
			// TODO Auto-generated method stub
			
		}


		public void onCancel() {
			// TODO Auto-generated method stub
			
		}
	 
	  }


	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}
	  
	  
	  
	  
	  ///////////////////////////////////////////////////////////////////////////////////////
		
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  //  ȭ�� ��� �����ְ��ϱ�
        setContentView(R.layout.activity_drivingmap);
        mContext = this;
        mapView = (MapView)findViewById(R.id.mapview);
        _drivingStart = (Button) findViewById(R.id.button1);
        _drivingFinish = (Button) findViewById(R.id.button2);
        _facebookPosting = (Button) findViewById(R.id.button3);
       
        findViewById(R.id.button1).setOnClickListener(bHandler);
        findViewById(R.id.button2).setOnClickListener(bHandler);
        findViewById(R.id.button3).setOnClickListener(bHandler);
        
        
        ////////////////////////////////////////////////////
        sm = (SensorManager)getSystemService(SENSOR_SERVICE); 
        accSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // ���ӵ�
        
        distance = (TextView)findViewById(R.id.editText1);
        nowstate = (TextView)findViewById(R.id.editText2);

        ////////////////////////////////////////////////////
        
//        mapController.setZoom(9);
//        mapView.setBuiltInZoomControls(true);     // ����Ʈ�ѷ� ������ ǥ��  
//        mapController.animateTo(mMyOverlay.getMyLocation());
//        mapController.setZoom(mapView.getMaxZoomLevel());
        mMyOverlay = new LocationOverlay(mContext, mapView);
        mapView.getOverlays().add(mMyOverlay);
        mapController = mapView.getController();
//        mapController.setZoom(12);
        mapController.setZoom(mapView.getMaxZoomLevel()-5);
        distance.setText("Total = " + mMyOverlay.TotalDistance);


      
        //getBestProvider ���� gps ��Ʈ�� ����
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//        String provider = LocationManager.GPS_PROVIDER;
//        
//       // mLocationProvider = mLocationManager.GPS_PROVIDER;
//        GPSListener gpsListener = new GPSListener();
//        mLocationManager.requestLocationUpdates(
//					provider,
//					3000,
//					1,
//					gpsListener);
        
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);	//��Ȯ��
        criteria.setPowerRequirement(Criteria.POWER_LOW);	//��������
        criteria.setAltitudeRequired(false);	//�ع�
        criteria.setBearingRequired(true);	//��ħ�� ����
        criteria.setSpeedRequired(false);	//�ӵ�
        criteria.setCostAllowed(true);		//���
        mLocationProvider = mLocationManager.getProvider(mLocationManager.getBestProvider(criteria, true));

    }
    ///////////////////////////////////////////
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) { 

            long currentTime = System.currentTimeMillis(); 
            long gabOfTime = (currentTime - lastTime); 

            if (gabOfTime > 1000) { 
                lastTime = currentTime;   
                accx = event.values[0]; 
                accy = event.values[1]; 
                accz = event.values[2];   
 
                accScala = (float) Math.sqrt(Math.pow(accx, 2) + Math.pow(accy, 2) + Math.pow(accz, 2));
                nowacc = Math.abs(accScala - beforeaccScala);
                
                synchronized (this) {
        			switch (event.sensor.getType()) {
        			case Sensor.TYPE_ACCELEROMETER:
						mMyOverlay.changeacc(nowacc);
        				break;		
         			}	                          
        		}
                beforeaccScala = accScala; 
                } 
        } 
	}
    ///////////////////////////////////////////

	public void onResume()
    {
    	super.onResume();
    	mMyOverlay.enableMyLocation();
    	sm.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL); // ���ӵ�
    }
    public void onPause()
    {
    	super.onPause();
    	mMyOverlay.disableMyLocation();
    	sm.unregisterListener(this);
    }
    public void onDestroy()
    {
    	super.onDestroy();
    	mapView.getOverlays().remove(mMyOverlay);
    }
	protected boolean isRouteDisplayed() 
	{
		return false;	//���� ��Ƽ��Ƽ�� ���� ������ ǥ���ϰ� �ִٸ� �ݵ�� true�� �����ؾ���. �׷��� ������ false ����
	}
	class GPSListener implements LocationListener{
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
		}
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub	
		}
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub	
		}
		
	}
}
