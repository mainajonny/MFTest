package com.morefun.mftest;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import morefun.hardware.MFManager;
//import com.morefun.server.MFService;

public class MainActivity extends ListActivity {
	private static final String TAG = "MFTest";
	private static final String TESTACTION = "com.morefun.hwbroadcast";
	
    private MFManager mManager = null;
	private Handler handler;
	
	private Boolean m_bLedPower = false;
	private Boolean m_bExLcdPower = false;
	private Boolean m_bMagcardPower = false;
	
	private String strMagcode1, strMagcode2, strMagcode3;
	private String strIccard, strRfid;
	
    @Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "register the broadcast receiver...");
		IntentFilter filter = new IntentFilter();
		filter.addAction(TESTACTION);
		registerReceiver(receiver, filter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "unregister the broadcast receiver...");
		unregisterReceiver(receiver);
	}

    private void initManager()
    {
    	new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                	// Get MFManager.
                    if (mManager == null) {
                        Log.i(TAG, "Creat a new MFManager object.");
                        mManager = new MFManager();
            		}
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                handler.sendMessage(handler.obtainMessage());
            }
        }).start();
    }

    @SuppressLint("HandlerLeak") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        initManager();
        
        List<String> items = fillList();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, items);

        setListAdapter(adapter);
        
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
				case 0:
					break;
                case 1:	// get magreader data...
                	showMessage("Track1: " + strMagcode1 + "\n"
                			+ "Track2: " + strMagcode2 + "\n"
                			+ "Track3: " + strMagcode3 + "\n");                	
					break; 
                case 2: // get iccard
                	showMessage("ICCard: " + strIccard);
                	break;
                /*case 3: // get psam1
                	showMessage("PSAM1: " + strPsam);
                	break;
                case 4: // get psam2
                	showMessage("PSAM2: " + strPsam);
                	break;*/
                case 5: // get rfid
                	showMessage("RFID: " + strRfid);
                	break;
				/*case 7: // get ps2
					showMessage("PS2: " + strPs2);
					break;*/
                case 401:	// magreader error
                	Toast.makeText(getApplicationContext(), "Magnetic card read error...", Toast.LENGTH_SHORT).show(); 
                	break;
                case 402:	// iccard error
                	Toast.makeText(getApplicationContext(), "IC card read error...", Toast.LENGTH_SHORT).show(); 
                	break;
                case 403:	// psam1 error
                	Toast.makeText(getApplicationContext(), "PSAM1 card read error...", Toast.LENGTH_SHORT).show(); 
                	break;
                case 404:	// psam2 error
                	Toast.makeText(getApplicationContext(), "PSAM2 card read error...", Toast.LENGTH_SHORT).show(); 
                	break;
                case 405:	// RFID error
                	Toast.makeText(getApplicationContext(), "RFID card read error...", Toast.LENGTH_SHORT).show(); 
                	break;
                }
            }
		};
    }

    private List<String> fillList() {
    	List<String> items = new ArrayList<String>();

    	items.add("Beep");
    	items.add("LED");
    	items.add("EXLCD");
    	items.add("CashBox");
    	items.add("Printer");
    	items.add("MagicCard");
    	items.add("IC Card");
    	items.add("RFID");

    	//items.clear();
    	return items;
    }
    
    @Override   
    protected void onListItemClick(ListView l, View v, int position, long id) {  
        //Toast.makeText(this, "You click: " + position, Toast.LENGTH_SHORT).show();  
        super.onListItemClick(l, v, position, id);
        
        if (mManager == null) {
        	Toast.makeText(this, "Fatal error, mManager is null.", Toast.LENGTH_SHORT).show(); 
        	return;
        }
        
        switch(position) {
        case 0: // BEEP
        	mManager.beep(200);
        	break;
        case 1: // LED
        	if (m_bLedPower) {
        		updateViewItem(v, "LED - ON");
        	} else {
        		updateViewItem(v, "LED - OFF");
        	}
        	//mManager.SetLed(0, m_bLedPower);
        	m_bLedPower = !m_bLedPower;
        	break;
        case 2: // EXLCD
        	testExLcd();
        	break;
        case 3: // CASHBOX
        	mManager.setCashboxOpen();
        	break;
        case 4: // PRINTER
        	testPrinter();
        	break;
        case 5: // MAGCARD
        	testMagcard(v);
        	break;
        case 6: // ICCARD
        	mManager.OpenIccard(0);
        	break;
        case 7: // RFID
        	mManager.OpenRFIDreader(); 
        	break;
        }
    }  
    
    private void updateViewItem(View v, String str)
    {
    	TextView tvText = (TextView) v.findViewById(android.R.id.text1);
    	tvText.setText(str);
    }
    
    private void testExLcd()
    {
    	if ( m_bExLcdPower ) {
    		mManager.Extlcd_close();
    		
    		m_bExLcdPower = false;
    	} else {
    		mManager.Extlcd_open();

    		String html="<html><head></head><body>";
        	//html += "<p><img src=\"/data/xino/unipay.bmp\" /></p>";
    		html += "<p><font color=\"#000000\"><big>福建魔方</big><br />电子科技有限公司<br /><big><big>MOREFUN-ET</big></big></font></p>";
            html += "</body></html>";
            mManager.Extlcd_write(html);

            m_bExLcdPower = true;
    	}
    }
    
    private void testPrinter()
    {
    	String strContent = "";
          	
		strContent += mManager.Print_heat_factor(15);
		strContent += mManager.Print_cn_font_size(2);
		strContent += mManager.Print_en_font_size(6);
		strContent += mManager.Print_line_space(6);
		strContent += mManager.Print_align(1);
		strContent += mManager.Print_cn_font_zoom(1, 2);
		
		strContent += "打印测试TestPrint \r\n \r\n";
		
		strContent += mManager.Print_align(0);
		strContent += mManager.Print_cn_font_zoom(1, 1);
		strContent += "`1234567890-=[]\\;',./\r\n";
		strContent += "~!@#$%^&*()_+{}|:\"<>?\r\n";
		strContent += "qwertyuiopasdfghjklzxcvbnm\r\n";
		strContent += "QWERTYUIOPASDFGHJKLZXCVBNM\r\n";            		
		strContent += "\r\n \r\n";
		
		strContent += mManager.Print_img("/vendor/data/TestPrint1.bmp");
		strContent += "\r\n \r\n";
		strContent += mManager.Print_img("/vendor/data/TestPrint2.bmp");
		strContent += "\r\n \r\n \r\n \r\n \r\n \r\n \r\n";
        
		mManager.PrintInit("ASCII-12-24", 12, 24);
		mManager.PrintWrite(strContent);
    }
    
    private void testMagcard(View v)
    {
    	if (m_bMagcardPower) {
    		mManager.CloseMagreader();
    		updateViewItem(v, "Magnetic card - ready");
    		m_bMagcardPower = false;
    	} else {
    		if (mManager.OpenMagreader()) {
    			updateViewItem(v, "Magnetic card");
        		m_bMagcardPower = true;           			
    		}
    	}
    }
    
    BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(TESTACTION.equals(intent.getAction())){
                Log.i(TAG, "get the broadcast from MFService..." + intent.getIntExtra("mf_recv_type", 0));
                
				Message msg = new Message();
				msg.what = intent.getIntExtra("mf_recv_type", 0);
				// magreader
				strMagcode1 = intent.getStringExtra("Magcard_track_a_str");
				strMagcode2 = intent.getStringExtra("Magcard_track_b_str");
				strMagcode3 = intent.getStringExtra("Magcard_track_c_str");
				// iccard
				strIccard = intent.getStringExtra("iccard_str");
				// rfid
				strRfid = intent.getStringExtra("rfid_uid_str");
				
				handler.sendMessage(msg);
			}
		}
	};
	
	private void showMessage(String string)
    {
        new AlertDialog.Builder(this)
        .setTitle("Info")
        .setMessage(string)
        .setPositiveButton( "OK" ,
        new DialogInterface.OnClickListener() {
        public void onClick(
        	DialogInterface dialoginterface, int i) {
        	}        
        }).show();    
    }
}
