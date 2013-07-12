package net.pocketmagic.perseus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;




public class PerseusAndroid extends Activity implements OnClickListener, OnItemClickListener {
	//-- ROBOT --//
	final static String			ROBO_BTADDR				= "00:11:06:28:03:61";
	//-- debugging --//	
	String 						LOG_TAG 				= "PerseusAndroid";
	//-- GUI --//
	final static String			m_szAppTitle			= "PerseusAndroid";
	TabHost						m_tabHost;
	ListView					m_lvSearch;	
	ProgressDialog				m_progDlg;
	
	TextView					m_tvD1, m_tvD2, m_tvD3;
	
	//-- Bluetooth functionality --//
	
	//BTNative					m_BT;					//obsolete
	final static int			MAX_DEVICES				= 50;
	 
	BluetoothAdapter 			m_BluetoothAdapter;
	boolean						m_ASDKScanRunning		= false;
	int							m_nDiscoverResult 		= -1;
	int							m_nRoboDev				= -1;
	final Handler 				m_Handler 				= new Handler();	//used for discovery thread, etc
	final Handler 				m_Handler_Read			= new Handler();    //used for read sensor data thread
    // Intent request codes
    final 		int 			REQUEST_CONNECT_DEVICE 	= 1,
    							REQUEST_ENABLE_BT 		= 2;
    BluetoothSocket 			m_btSck;									//used to handle Android<->Robot communication
    private static final UUID 	SPP_UUID 				= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Thread						m_hReadThread;

	
	
	public static final int 	idMenuTab1Search		= Menu.FIRST +   1,
								idTab2FWD			= Menu.FIRST + 2,
								idTab2BCK			= Menu.FIRST + 3,
								idTab2LFT			= Menu.FIRST + 4,
								idTab2RGT			= Menu.FIRST + 5,
								idLVFirstItem			= Menu.FIRST + 100;	

	class BTDev {
		String 	m_szName;
		String 	m_szAddress;
		int		m_nBTDEVType; //if 1, it's the Perseus ROBOT, if 0 it's a regular device
		
		
		BTDev(String _name, String _address) {
			m_szName = _name; m_szAddress = _address;  
		}
		BTDev(String _name, String _address, int _type) {
			m_szName = _name; m_szAddress = _address; m_nBTDEVType = _type;  
		}
	}
	BTDev	BTDevs[];
	int		BTCount;
	
	private View createTabContent1()
	{
		final Context context = PerseusAndroid.this;
		// Tab container
    	LinearLayout panel = new LinearLayout(context);
  		panel.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
  		panel.setOrientation(LinearLayout.VERTICAL);
  		
  		LinearLayout panelH = new LinearLayout(context);
     	panelH.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
  		panelH.setOrientation(LinearLayout.HORIZONTAL);
  		panelH.setGravity(Gravity.LEFT);
  		panelH.setGravity(Gravity.CENTER_VERTICAL);

  		Button but = new Button(this);
  		but.setText("Search BT Devices");
  		but.setId(idMenuTab1Search);
  		but.setOnClickListener(this);
  		but.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
     	panelH.addView(but);
     	
     	panel.addView(panelH);


		m_lvSearch = new ListView( this );
	 	// clear previous results in the LV
		m_lvSearch.setAdapter(null);      
		m_lvSearch.setOnItemClickListener((OnItemClickListener) this);
		
		/*// -- remove this
		BTDevs[BTCount] = new BTDev("test", ROBO_BTADDR, 1);
		BTCount++;
		
		PopulateLV();
		// -- end*/
	    panel.addView(m_lvSearch);
	    TextView lbBottom = new TextView(context);
    	lbBottom.setText("Press the button to discover Bluetooth devices");
    	lbBottom.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
     	panel.addView(lbBottom);

	    
	    return panel;
	}
	
	private View createTabContent2()
	{
		final Context context = PerseusAndroid.this;
		
		// Tab container
    	LinearLayout panel = new LinearLayout(context);
    	panel.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
    	panel.setOrientation(LinearLayout.VERTICAL);
    	

    	LinearLayout panelH = new LinearLayout(context);
  		panelH.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
  		panelH.setOrientation(LinearLayout.HORIZONTAL);
  		panelH.setGravity(Gravity.CENTER);
  		panelH.setGravity(Gravity.CENTER_VERTICAL);
  		// Enable a new bottom
     	Button but = new Button(this);but.setText("____");but.setEnabled(false);but.setFocusable(false);but.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));panelH.addView(but);
  		but = new Button(this);
  		but.setText("DO");
  		but.setGravity(Gravity.CENTER);
  		but.setId(idTab2FWD);
  		but.setOnClickListener(this);
  		but.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
     	panelH.addView(but);
     	but = new Button(this);but.setText("____");but.setEnabled(false);but.setFocusable(false);but.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));panelH.addView(but);
     	panel.addView(panelH);
     	
     	panelH = new LinearLayout(context);
     	panelH.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
  		panelH.setOrientation(LinearLayout.HORIZONTAL);
  		panelH.setGravity(Gravity.CENTER);
  		panelH.setGravity(Gravity.CENTER_VERTICAL);
  		but = new Button(this);
  		but.setText("RE");
  		but.setId(idTab2LFT);
  		but.setOnClickListener(this);
  		but.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)); 		
     	panelH.addView(but);
     	but = new Button(this);but.setText("____");but.setEnabled(false);but.setFocusable(false);but.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));panelH.addView(but);
  		but = new Button(this);
  		but.setText("MI");
  		but.setId(idTab2RGT);
  		but.setOnClickListener(this);
  		but.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
     	panelH.addView(but);
     	panel.addView(panelH);
     	
    	panelH = new LinearLayout(context);
     	panelH.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
  		panelH.setOrientation(LinearLayout.HORIZONTAL);
  		panelH.setGravity(Gravity.CENTER);
  		panelH.setGravity(Gravity.CENTER_VERTICAL);
     	but = new Button(this);but.setText("____");but.setEnabled(false);but.setFocusable(false);but.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));panelH.addView(but);
     	but = new Button(this);
  		but.setText("FA");
  		but.setId(idTab2BCK);
  		but.setOnClickListener(this);
  		but.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
     	panelH.addView(but);
     	but = new Button(this);but.setText("____");but.setEnabled(false);but.setFocusable(false);but.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));panelH.addView(but);
     	panel.addView(panelH);
   
     	//m_tvD1 = new TextView(this);
     	//m_tvD1.setText("FRONT LEFT PING:");
     	//panel.addView(m_tvD1);
     	m_tvD2 = new TextView(this);
     	m_tvD2.setLines(18);
     	m_tvD2.setWidth(300);
     	m_tvD2.setText(":");
     	m_tvD2.setMovementMethod(new ScrollingMovementMethod()); // make it scrollable
     	panel.addView(m_tvD2);
     	//m_tvD3 = new TextView(this);
     	//m_tvD3.setText("TEMPERATURE:");
     	//panel.addView(m_tvD3);
  		
  		return panel;
	}
	/*//get rid of tab 3
	private View createTabContent3() {
		final Context context = PerseusAndroid.this;
		
		// Tab container
		LinearLayout panel = new LinearLayout(context);
		panel.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		panel.setOrientation(LinearLayout.VERTICAL);
		
		TextView mesg = new TextView(context);
		mesg.setText("Press teh button to discover aliens");
		mesg.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		panel.addView(mesg);
		panel.addView(new RenderView(context));
		
		
		return panel;
	}
	*/ // get rid of tab 3
	//draw something
	/*
	private class RenderView extends View {
		Paint paint;
		public RenderView(Context context) {
			super(context);
		paint = new Paint();
		}
		protected void onDraw(Canvas canvas) {
		canvas.drawRGB(255, 255, 255);
		paint.setColor(Color.RED);
		canvas.drawLine(0, 0, canvas.getWidth()-1, canvas.getHeight()-1, paint);
		paint.setStyle(Style.STROKE);
		paint.setColor(0xff00ff00);
		canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, 40, paint);
		paint.setStyle(Style.FILL);
		paint.setColor(0x770000ff);
		canvas.drawRect(100, 100, 200, 200, paint);
		invalidate();
		}
	}
	*/
	
	
	/** This function creates the Main interface: the TAB host **/
	private View createMainTABHost() {
		// construct the TAB Host
    	TabHost tabHost = new TabHost(this);
    	tabHost.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 
        // the tabhost needs a tabwidget, that is a container for the visible tabs
        TabWidget tabWidget = new TabWidget(this);
        tabWidget.setId(android.R.id.tabs);
        tabHost.addView(tabWidget, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT)); 
 
        // the tabhost needs a frame layout for the views associated with each visible tab
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setId(android.R.id.tabcontent);
        frameLayout.setPadding(0, 65, 0, 0);
        tabHost.addView(frameLayout, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)); 
 
        // setup must be called if you are not initialising the tabhost from XML
        tabHost.setup(); 
 
      
        // create the tabs
        TabSpec ts;
     
        ImageView iv;
        
        ts = tabHost.newTabSpec("TAB_TAG_1");
        ts.setIndicator("Search");
        ts.setContent(new TabHost.TabContentFactory()
        {
            public View createTabContent(String tag)
            {
            	return createTabContent1();
             } //TAB 1 done
        });
        tabHost.addTab(ts);
        // -- set the image for this tab
        iv = (ImageView)tabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.icon);
        if (iv != null) iv.setImageDrawable(getResources().getDrawable(R.drawable.bt));

 
        ts = tabHost.newTabSpec("TAB_TAG_2");
        ts.setIndicator("Control");
        ts.setContent(new TabHost.TabContentFactory(){
             public View createTabContent(String tag)
             {
            	 return createTabContent2();
             }
        });
        tabHost.addTab(ts);
        // -- set the image for this tab
        iv = (ImageView)tabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.icon);
        if (iv != null) iv.setImageDrawable(getResources().getDrawable(R.drawable.perseus));
        
     /*  
        //adding tag 3
        ts = tabHost.newTabSpec("TAG_TAG_3");
        ts.setIndicator("Game");
        
        
        
        ts.setContent(new TabHost.TabContentFactory() {	
			@Override
			public View createTabContent(String tag) {
				return createTabContent3();
			}
		});
		
		
        
        //specify an intent to use an activity (AndroidGame in this case ) as the
        // tab content
        
        //ts.setContent(new Intent(this, FirstTab.class));
         
         
        
        tabHost.addTab(ts);
        */
        
        return tabHost;
	}
	

    
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        
        
        //m_BT = new BTNative();
        BTDevs = new BTDev[MAX_DEVICES]; 
        
        m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (m_BluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (!m_BluetoothAdapter.isEnabled()) 
        {
        	// enable bluetooth
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }  
        
        
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter); 

        
        // disable the titlebar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // create the interface
        
        
        m_tabHost = (TabHost)createMainTABHost();    
        setContentView(m_tabHost);
        
        
        /*
        setContentView(R.layout.tab);
        
        // TabHost will have Tabs 
        TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);
        TabSpec firstTabSpec = tabHost.newTabSpec("tid1");
        firstTabSpec.setIndicator("First Tab Name").setContent(new Intent(this,FirstTab.class));
        tabHost.addTab(firstTabSpec);
        */
    }
    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
                        
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //if (device.getBondState() != BluetoothDevice.BOND_BONDED) // If it's already paired, skip it, because it's been listed already
            	//-- ignore duplicates
				boolean duplicate = false;
				for (int j=0;j<BTCount;j++)
					if (BTDevs[j].m_szAddress.compareTo(device.getAddress()) == 0) { duplicate = true; break; }
				if (duplicate)
					; //this is a duplicate
				else
				{
					if (device.getAddress().compareTo(ROBO_BTADDR) == 0)
						BTDevs[BTCount] = new BTDev(device.getName(), device.getAddress(), 1);
					else
						BTDevs[BTCount] = new BTDev(device.getName(), device.getAddress(), 0);
	                BTCount++;
				}
                
            // When discovery is finished
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            	m_ASDKScanRunning = false; 
            }
        }
    };	


	@Override
	public void onClick(View v) {
		int cmdId = v.getId();
		if (cmdId == idMenuTab1Search)
		{
			startDiscoverBluetoothDevices();
			
			
		}
		if (cmdId == idTab2FWD)
		{
			if (m_btSck != null)
				try {
					byte[] byteString = ("do\n ").getBytes();
					byteString[byteString.length - 1] = 0;
					m_btSck.getOutputStream().write(byteString);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		if (cmdId == idTab2LFT)
		{
			Intent i = new Intent();
			setResult(RESULT_OK,i);
			finish();
			
			if (m_btSck != null)
				try {
					byte[] byteString = ("re\n ").getBytes();
					byteString[byteString.length - 1] = 0;
					m_btSck.getOutputStream().write(byteString);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		if (cmdId == idTab2RGT)
		{
			if (m_btSck != null)
				try {
					byte[] byteString = ("mi\n ").getBytes();
					byteString[byteString.length - 1] = 0;
					m_btSck.getOutputStream().write(byteString);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		if (cmdId == idTab2BCK)
		{
			if (m_btSck != null)
				try {
					byte[] byteString = ("fa\n ").getBytes();
					byteString[byteString.length - 1] = 0;
					m_btSck.getOutputStream().write(byteString);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (m_tabHost.getCurrentTab() == 0) //we are on SEARCH page (0)
		{
			int nIndex = -1, nCounter = 0;
			for (int i=0;i<BTCount;i++)
			{
				if (arg2 == nCounter) {
					nIndex = i;
					break;
				}
				nCounter++;
			}
			// connect to 
			if (BTDevs[nIndex].m_nBTDEVType == 1)
			{
				//connect to ROBOT
				Connect(nIndex);
				// assign socket to global variable
				AndroidApplication appState = (AndroidApplication) this.getApplication();
				appState.setBluetoothSocket(m_btSck);
				
				//StartReadThread(nIndex);
			}
			else 
				Toast.makeText(getBaseContext(), 
						"This is not ROBO", Toast.LENGTH_SHORT).show();


		}
		
	}
	
	// put the /BTDEvs in the listview
	void PopulateLV()
	{
		ArrayList<Device> m_Devices = new ArrayList<Device>();
		Device device;
        for (int i=0;i<BTCount;i++) {
        	if (BTDevs[i].m_szAddress.compareTo(PerseusAndroid.ROBO_BTADDR) == 0) {
        		BTDevs[i].m_nBTDEVType = 1;
        		m_nRoboDev = i;
        	}
        	else 
        		BTDevs[i].m_nBTDEVType = 0;
        	device = new Device(BTDevs[i].m_szName, 
        			BTDevs[i].m_szAddress, 
        			BTDevs[i].m_nBTDEVType,
        			0, 
        			idLVFirstItem+i);
        	m_Devices.add(device);
        }
    CustomAdapter lvAdapter =  new CustomAdapter(this, m_Devices);
    if (lvAdapter!=null) m_lvSearch.setAdapter(lvAdapter);
    if (m_nRoboDev >= 0)
    	Toast.makeText(getBaseContext(), "ROBO found as " + BTDevs[m_nRoboDev].m_szAddress, 
    			Toast.LENGTH_LONG).show();
	}
	
	/** Bluetooth Functions **/
	
	// not Blocking, uses events
	int ASDKDiscoverBluetoothDevices()
	{
		if (m_BluetoothAdapter.isDiscovering()) 
    		m_BluetoothAdapter.cancelDiscovery();
        
		int current_devs = BTCount;
		// Request discover from BluetoothAdapter
    	if (!m_BluetoothAdapter.startDiscovery()) return -1; //error
    	
    	m_ASDKScanRunning = true;

    	//  blocking operation:wait to complete
        while (m_ASDKScanRunning);
         
        return BTCount - current_devs;
	}

	final Runnable mUpdateResultsDiscover = new Runnable() {
        public void run() {
        	doneDiscoverBluetoothDevices();
        }
    };
    protected void startDiscoverBluetoothDevices() {
    	// Show Please wait dialog
    	m_progDlg = ProgressDialog.show(this,
    			m_szAppTitle, "Scanning, please wait",
    			true);
    	
    	// Fire off a thread to do some work that we shouldn't do directly in the UI thread
	    Thread t = new Thread() {
	    	public void run() 
	    	{
	    		// blocking operation
            		m_nDiscoverResult = ASDKDiscoverBluetoothDevices();
            	//show results
	        	m_Handler.post(mUpdateResultsDiscover);
	    	}
	    };
	    t.start();
    }
    
    private void doneDiscoverBluetoothDevices() 
    {
    	m_progDlg.dismiss();
    	if (m_nDiscoverResult == -1)
    		Toast.makeText(getBaseContext(), "Bluetooth ERROR (is bluetooth on?)", Toast.LENGTH_LONG).show();
    	else if (m_nDiscoverResult == 0)
    		Toast.makeText(getBaseContext(), "No Bluetooth devices found", Toast.LENGTH_LONG).show();
    	else {
    		m_nRoboDev = -1;
    		// populate
			PopulateLV();
    	}
    }
    int Connect(int nIndex)
	{
		if (nIndex >= BTCount || nIndex<0) return -1; //invalid device
		
		//ANDROID SDK IMPLEMENTATION
		//--connect serial port
		BluetoothDevice ROBOBTDev = m_BluetoothAdapter.getRemoteDevice(BTDevs[nIndex].m_szAddress);
		try {
			m_btSck = ROBOBTDev.createRfcommSocketToServiceRecord(SPP_UUID);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try { //This is a blocking call and will only return on a successful connection or an exception
			m_btSck.connect();	             
		} catch (IOException e) {
             // Close the socket
             try { m_btSck.close();} catch (IOException e2) { e2.printStackTrace();}
             return -2; 
         }
		return 0;
	}
    int Disconnect(int nIndex)
	{
		if (nIndex >= BTCount || nIndex<0) return -1; //invalid device
		
		// DISCONNECT ASDK SOCKETS
		if (m_btSck != null) {
			try {
				m_btSck.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
            }
			m_btSck = null;
		}
		return 0;
	}
   
    
    // wrapper class for Runnable since I want to pass data(sensor data) 
    //out of the thread (m_hReadThread)
    private class DisplayReceivingData implements Runnable {
    	String dataRead;
    	DisplayReceivingData(String dataFromThread) {
    		dataRead = dataFromThread;
    	}
    	public void run() {
    		
    		// append receiving data to text view
    		//m_tvD2.setText(dataRead);
    		m_tvD2.append(dataRead);
    		// from http://stackoverflow.com/questions/3506696/auto-scrolling-textview-in-android-to-bring-text-into-view
    	    // find the amount we need to scroll.  This works by
    	    // asking the TextView's internal layout for the position
    	    // of the final line and then subtracting the TextView's height
    	    final int scrollAmount = m_tvD2.getLayout().getLineTop(m_tvD2.getLineCount())
    	            - m_tvD2.getHeight();
    	    // if there is no need to scroll, scrollAmount will be <=0
    	    if(scrollAmount >0)
    	        m_tvD2.scrollTo(0, scrollAmount);
    	    else
    	        m_tvD2.scrollTo(0,0);

    	}
    }
    
    // Worker functions
	int StartReadThread(final int nIndex)
	{
		// signal connect event for this BT dev
		ConnectionEvent(1,nIndex,null);
		
		m_hReadThread = new Thread() {
	        public void run() 
	        {
	        	
	        	while (true) 
	        	{
	        		byte buf[] = null;
        		
	        		try {
	                    // Read from the InputStream
	        			byte[] buffer = new byte[1024]; 
	        			int bread = m_btSck.getInputStream().read(buffer);
	        			buf = new byte[bread];
	        			System.arraycopy(buffer, 0, buf, 0, bread);
	        			

	                    // Send the obtained bytes to the UI Activity
	                    Log.i(LOG_TAG, "StartReadThread: Data received:"+ bread);
	                    // TRY to display receiving data 
	                    m_Handler_Read.post(new DisplayReceivingData(new String(buf, 0, bread)));
	                   
	                } catch (IOException e) {
	                	Log.d(LOG_TAG, "StartReadThread: disconnected", e);
	                } 
	        		
	        		// signal disconnect event
		        	if (buf == null)
		        	{
		        		ConnectionEvent(2,nIndex, null);
		        		break;
		        	}
	        		else //signal incoming data
	        		{
	        			ConnectionEvent(3,nIndex, buf);
	        		}
	        	}
	        		
	        }
        };
        m_hReadThread.start();
		return 0;
	}
	
	// Worker event function called on various events
	int ConnectionEvent(int type, int nDevId, byte buf[])
	{
		if (nDevId >= BTCount)return -1;
		
		if (type == 1) { //connected
			m_tabHost.post(new Runnable() { public void run() {m_tabHost.setCurrentTab(1);} });
		}
		if (type == 2) { //disconnect
			// DISCONNECT NATIVE SOCKETS
			//Toast.makeText(this, "Disconnected from ROBO", Toast.LENGTH_LONG).show();
			Disconnect(nDevId);
			
			m_tabHost.post(new Runnable() { public void run() {m_tabHost.setCurrentTab(0);} });
		}
		if (type == 3) { 
			if (buf.length == 0) return -1;
			
			int nTHeader = buf[0]& 0xFF; 
			int nTType = (nTHeader >>> 4) & 0xF;	//transaction type
			int nTParam = (nTHeader) & 0xF;		//transaction parameters
			
			
		}
		return 0;
	}
	



    
}

