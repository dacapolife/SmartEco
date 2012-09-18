package com.smarteco.android;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;


public class EfficiencyActivity extends Activity {
    /** Called when the activity is first created. */
	// ���� ����
	Button btnCalc; //Button dbdrop;
	EditText editDistance, editOil;
	ListView list;
	DatePicker datepick;
	// ������
	ArrayList<ListItem> data = null;
    ListAdapter adapter = null;
    // �ӽ� ��..
    int totaldistance;
    int _index;
    //DB����
    SQLiteDatabase m_db;
    String strSQL;
    Cursor m_cursor;
    boolean dbFlag = false;
    
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effiency);        

        list = (ListView)findViewById(R.id.listView1);
        data = new ArrayList<ListItem>();
      
        findViewById(R.id.bt_cal).setOnClickListener(bHandler);
   
        createTable();// db table ����
        getData();//db data �ҷ�����
        adapter = new ListAdapter(this, R.layout.item, data);	// ����Ϳ�  �ҷ��� data �� listview�� �����غ�..
        list.setAdapter(adapter);	// list view �����ֱ�.. adapter�� �ִ� data�� listview �� ��� 
        Intent intent = getIntent();
        dbFlag = intent.getBooleanExtra("delDB",true);
        if(dbFlag){
        	confirmAlert();
        	
        }
        // listview�� item�� ���� ��ġ�� �߻� event ó��.
        list.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
            	//���̾�α׸� ����ش�.
            	alert(index);
                return true;
            }
        });
       }
    ///   methods ~~~~ /////////////
       
	void saveData(){
		//context �� ���� EfficiencyActivity �� �Ҵ�..
		Context mContext = com.smarteco.android.EfficiencyActivity.this;
		// ���̾�α� â�� ���� ���� ��ü
		AlertDialog.Builder builder;
		AlertDialog dialog;
		// 
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout=inflater.inflate(R.layout.customdialog,(ViewGroup)findViewById(R.id.customdialog_layout));
		
		builder = new AlertDialog.Builder(mContext);
		
		editDistance = (EditText)layout.findViewById(R.id.ed_distance);
		editOil = (EditText)layout.findViewById(R.id.ed_oil);
		datepick = (DatePicker)layout.findViewById(R.id.datePicker);
		
		builder.setView(layout);
		
		builder.setTitle("���� ������ �Է��ϼ���");
		
		// ���̾�α� â ����..
		/*
		 * �Է� ��ư�� ����� 
		 * editText�� ���� ������ ����ó��...
		 * ���� ������ DB�� �� ����
		 */
		builder.setPositiveButton("�Է�",new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				String str1 = editDistance.getText().toString();
				String str2 = editOil.getText().toString();
				int distance=0;
				if((str1.length() == 0 || str2.length() == 0)){
					err_alert();
				}
				else{
					// date ������ ���ڿ��� ���� . �� �� ��
					// getMonth() ��  0~11 ��, + 1 ����� �Ѵ�.
					String strDate =datepick.getYear()+"-"+(datepick.getMonth()+1)+"-"+datepick.getDayOfMonth();
					
					// DB �� ����..
					createTable();
					m_cursor = m_db.rawQuery("SELECT * FROM efficiency",null);
					 
					m_cursor.moveToLast();
					if(m_cursor.getPosition()==-1){
						distance = 0;
					}
					else{
						distance = Integer.parseInt(editDistance.getText().toString())-m_cursor.getInt(4);	
					}

					insert(strDate,distance,Integer.parseInt(editOil.getText().toString()),Integer.parseInt(editDistance.getText().toString()));
					getLastData();
					list.setAdapter(adapter);
				}
			}
		});
		// ���̾�α� ��ҹ�ư ����
		builder.setNegativeButton("���",new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog,int which){
				dialog.dismiss();
			}
		});
		// ���̾�α� �����.. show
		dialog=builder.create();
		dialog.show();
	}
	
	View.OnClickListener bHandler = new View.OnClickListener(){
		public void onClick(View v){
			switch(v.getId()){
			// �Է� ��ư
			case R.id.bt_cal:
				saveData();
				break;
				// DB�ʱ�ȭ �׽�Ʈ
//			case R.id.dbdrop:
//				createTable();
//				dropTable();
//				break;
			}
		}
	};
    
    private void alert(final int index){
    	 
    	new AlertDialog.Builder(this)
    	.setItems(C.items,new DialogInterface.OnClickListener(){
    		public void onClick(DialogInterface dialog, int item){
     		switch(item){
     		case 0:// ����
     			fix_data(index);
     			break;
     		case 1:// ����
     			deleteData(index);
     			break;
     		}
    		}
    	})
    	.show();
    }
    private void err_alert(){
   	 
    	new AlertDialog.Builder(this)
    	.setTitle("�Է� ����")
		.setMessage("���ڸ� �Է��ϼ���")
    	.show();
    }
    
    private void confirmAlert(){
    	new AlertDialog.Builder(this)
    	.setTitle("Ȯ��")
		.setMessage("�����ͺ��̽��� �ʱ�ȭ �Ͻðڽ��ϱ� ?(���� �� �� ����)")
		.setPositiveButton("Yes",new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id){
				createTable();
				dropTable();
				dialog.dismiss();
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				// TODO Auto-generated method stub
				
			}
		})
    	.show();
    }

    // DB table ����
    public void createTable(){
    	
    	m_db = openOrCreateDatabase("efficiency.db",Context.MODE_PRIVATE,null);
       	if(!m_db.isOpen()){
    		Log.v("SQLite","openOrCreateDatabase ... Fail");
    		return;
    	}
    	try{
    		strSQL = 
    				"CREATE TABLE IF NOT EXISTS efficiency"+
    				"("+
					"	_id	INTEGER	"		+
    				"	, _date TEXT"		+
    				"	, _distance INTEGER"	+
    				"	, _oil INTEGER"		+
    				"	, t_distance INTEGER"+
    				");";
 
    		m_db.execSQL(strSQL);
    		
    		Log.i("SQLite","Create Table ... OK");
    	}
    	catch(SQLException e){
    	}
    	finally{
    		//m_db.close();
    		//Log.i("SQLite","Database Close ... OK");
    	}
    	
    }
    
   //DB ����
   public void insert(String strDate,int distance,int edOil,int edtDistance){
	   m_cursor = m_db.rawQuery("SELECT * FROM efficiency",null);
	   m_cursor.moveToLast();
	   Log.d("test",Integer.toString(m_cursor.getPosition()));
	   strSQL=
			"INSERT INTO efficiency ( _id, _date, _distance, _oil, t_distance)"+
			" VALUES ( '"+	(m_cursor.getPosition()+1)	+	"','"	+
							strDate		+	"','"	+
							distance	+	"','"	+
							edOil		+	"','"	+
							edtDistance	+	"'"		+
			");";
	   m_db.execSQL(strSQL);  
	   m_cursor.close();
	   //m_db.close();
	   Log.i("SQLite","Insert data ... OK");
	  
   }
   // DB�� ���� ������ ��������.
   public void getData(){
	   
	   m_cursor = m_db.rawQuery("SELECT * FROM efficiency",null);
	   
	   if(m_cursor !=null){
		
		   if(m_cursor.moveToFirst()){
			   do{
				   
					   data.add(new ListItem(m_cursor.getString(1),		// date
							   m_cursor.getInt(2),						// distance
							   m_cursor.getInt(3),						// oil
							   m_cursor.getInt(4)						// total distance
							   ));
					   
					   Log.v("TESTd",Integer.toString(m_cursor.getInt(0)));
			   }while(m_cursor.moveToNext());
		   }
	   }
	   m_cursor.close();
	   m_db.close();
   }
	public void getLastData(){
		   m_cursor = m_db.rawQuery("SELECT * FROM efficiency",null);
		   if(m_cursor !=null){
			   if(m_cursor.moveToLast()){
				   do{
						   data.add(new ListItem(m_cursor.getString(1),		// date
								   m_cursor.getInt(2),						// distance
								   m_cursor.getInt(3),						// oil
								   m_cursor.getInt(4)));					// total distance
				   }while(m_cursor.moveToNext());
			   }
		   }
		   m_cursor.close();
		   m_db.close();
	}
	
	
	public void deleteData(int index){
		createTable();
		int fix_distance=0;
		m_cursor = m_db.rawQuery("SELECT * FROM efficiency",null);
		m_cursor.moveToPosition(index);
		
		
		if(m_cursor.isFirst()||m_cursor.isLast()){
			Log.v("Tee1",Integer.toString(m_cursor.getPosition()));
		}
		else{
			index = index+1;
			m_cursor.moveToPosition(index);
			fix_distance = m_cursor.getInt(4);
			index = index-2;
			m_cursor.moveToPosition(index);
			fix_distance = fix_distance-m_cursor.getInt(4);
			index++;
		}
		
		try{
			strSQL="DELETE FROM efficiency WHERE _id="+index+";";
			 m_db.execSQL(strSQL);
			 data.remove(index);
		}
		catch(SQLException e){
			Log.v("error", e.toString());
		}
		m_cursor.moveToPosition(index);
		strSQL="UPDATE efficiency SET _distance="+fix_distance+" WHERE _id="+(index+1)+";";
		m_db.execSQL(strSQL);
		 do{
			 if(m_cursor.isLast()){
				 Log.v("warring","empty");
			 }
			 else{
				 Log.v("Tee21",Integer.toString(m_cursor.getPosition()));
				 index++;
				 strSQL="UPDATE efficiency SET _id="+(index-1)+" WHERE _id="+index+";";
				 m_db.execSQL(strSQL);
				 Log.v("ttt",Integer.toString(index));
			 }
		 }while(m_cursor.moveToNext());
		 list.invalidate();
		 adapter.notifyDataSetChanged();
		 m_db.close();
	}
	
	public void fix_data(int index){
			_index = index;
			//context �� ���� EfficiencyActivity �� �Ҵ�..
			ListItem temp_item = data.get(index);
			Context mContext = com.smarteco.android.EfficiencyActivity.this;
			// ���̾�α� â�� ���� ���� ��ü
			AlertDialog.Builder builder;
			AlertDialog dialog;
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout=inflater.inflate(R.layout.customdialog,(ViewGroup)findViewById(R.id.customdialog_layout));
			
			builder = new AlertDialog.Builder(mContext);
			
			editDistance = (EditText)layout.findViewById(R.id.ed_distance);
			editOil = (EditText)layout.findViewById(R.id.ed_oil);
			datepick = (DatePicker)layout.findViewById(R.id.datePicker);
			
			builder.setView(layout);
			
			builder.setTitle("���� ������ �Է��ϼ���");
			
			editDistance.setText(Integer.toString(temp_item.getTotaldistance()));
			editOil.setText(Integer.toString(temp_item.getOil()));

			// ���̾�α� â ����..
			/*
			 * �Է� ��ư�� ����� 
			 * editText�� ���� ������ ����ó��...
			 * ���� ������ DB�� �� ����
			 */
			builder.setPositiveButton("����",new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					String str1 = editDistance.getText().toString();
					String str2 = editOil.getText().toString();
					int distance=0;
					if((str1.length() == 0 || str2.length() == 0)){
						err_alert();
					}
					else{
						// date ������ ���ڿ��� ���� . �� �� ��
						// getMonth() ��  0~11 ��, + 1 ����� �Ѵ�.
						String strDate =datepick.getYear()+"-"+(datepick.getMonth()+1)+"-"+datepick.getDayOfMonth();
						Log.v("efe",strDate);
						// DB �� ����..
						createTable();
						m_cursor = m_db.rawQuery("SELECT * FROM efficiency",null);
						m_cursor.moveToPosition(_index);
						if(m_cursor.isFirst()){
							distance = 0;
						}
						else{
							m_cursor.moveToPosition(_index-1);
							distance = Integer.parseInt(editDistance.getText().toString())-m_cursor.getInt(4);	
						}
						strSQL="UPDATE efficiency SET " +
								"_date=\""+strDate+
								"\", _oil="+Integer.parseInt(editOil.getText().toString())+
								", _distance="+distance+
								", t_distance="+Integer.parseInt(editDistance.getText().toString())+
								" WHERE _id="+_index+";";
						m_db.execSQL(strSQL);
						
						m_cursor.moveToPosition(_index);
						if(!m_cursor.isLast()){
							m_cursor.moveToPosition(_index+1);
							
							distance=m_cursor.getInt(4)-Integer.parseInt(editDistance.getText().toString());
							strSQL="UPDATE efficiency SET _distance="+distance+
									" WHERE _id="+(_index+1)+";";
							m_db.execSQL(strSQL);
							arrayListset(_index+1);
						}
						m_db.close();
						Log.v("tee",m_cursor.getString(4));
					}
				}
			});
			// ���̾�α� ��ҹ�ư ����
			builder.setNegativeButton("���",new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog,int which){
					dialog.dismiss();
				}
			});
			// ���̾�α� �����.. show
			dialog=builder.create();
			dialog.show();
			//m_db.close();
		
	}
	
	//// �������� DB �ʱ�ȭ..
	public void dropTable(){
		createTable();
		strSQL="drop table efficiency";
		try{
		 m_db.execSQL(strSQL);
		 data.clear();
		 
		 list.invalidate();
		 adapter.notifyDataSetChanged(); 
		 m_db.close();
		}
		catch(SQLException e){
			Log.v("error", e.toString());
		}
	}
	public void arrayListset(int index){
		m_cursor.moveToPosition(index);
		data.set(index, new ListItem(m_cursor.getString(1),		// date
				m_cursor.getInt(2),		// distance
				m_cursor.getInt(3),		// oil
				m_cursor.getInt(4)		// total distance
				));
		//ListItem item = data.get(index);
		
	}
}
