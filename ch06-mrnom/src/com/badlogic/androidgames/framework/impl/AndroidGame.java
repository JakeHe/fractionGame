package com.badlogic.androidgames.framework.impl;

import java.io.IOException;


//import net.pocketmagic.perseus.GameScreen.DisplayReceivingData;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.bluetooth.BluetoothSocket;

import com.badlogic.androidgames.framework.Audio;
import com.badlogic.androidgames.framework.FileIO;
import com.badlogic.androidgames.framework.Game;
import com.badlogic.androidgames.framework.Graphics;
import com.badlogic.androidgames.framework.Input;
import com.badlogic.androidgames.framework.Screen;

public abstract class AndroidGame extends Activity implements Game {
    AndroidFastRenderView renderView;
    Graphics graphics;
    Audio audio;
    Input input;
    FileIO fileIO;
    Screen screen;
    WakeLock wakeLock;
    BluetoothSocket m_btSck;
    final Handler 				m_Handler_Read			= new Handler();
	Thread						m_hReadThread;
	String sensorInput = "";
	int sensorData1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        int frameBufferWidth = isLandscape ? 480 : 320;
        int frameBufferHeight = isLandscape ? 320 : 480;
        Bitmap frameBuffer = Bitmap.createBitmap(frameBufferWidth,
                frameBufferHeight, Config.RGB_565);
        
        float scaleX = (float) frameBufferWidth
                / getWindowManager().getDefaultDisplay().getWidth();
        float scaleY = (float) frameBufferHeight
                / getWindowManager().getDefaultDisplay().getHeight();

        renderView = new AndroidFastRenderView(this, frameBuffer);
        graphics = new AndroidGraphics(getAssets(), frameBuffer);
        fileIO = new AndroidFileIO(getAssets());
        audio = new AndroidAudio(this);
        input = new AndroidInput(this, renderView, scaleX, scaleY);
        screen = getStartScreen();
        m_btSck = getBluetoothSocket();
        StartReadThread();
        setContentView(renderView);
        
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "GLGame");
    }

    @Override
    public void onResume() {
        super.onResume();
        wakeLock.acquire();
        screen.resume();
        renderView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        wakeLock.release();
        renderView.pause();
        screen.pause();

        if (isFinishing())
            screen.dispose();
    }

    @Override
    public Input getInput() {
        return input;
    }

    @Override
    public FileIO getFileIO() {
        return fileIO;
    }

    @Override
    public Graphics getGraphics() {
        return graphics;
    }

    @Override
    public Audio getAudio() {
        return audio;
    }

    @Override
    public void setScreen(Screen screen) {
        if (screen == null)
            throw new IllegalArgumentException("Screen must not be null");

        this.screen.pause();
        this.screen.dispose();
        screen.resume();
        screen.update(0);
        this.screen = screen;
    }
    
    public int getSensorData1() {
    	return sensorData1;
    }
  
    
    public Screen getCurrentScreen() {
        return screen;
    }
    
    public String getSensorInput() {
    	return this.sensorInput;
    }
    public void setSensorInputToEmpty(){
    	this.sensorInput = "";
    }
    
    // Worker functions
  	int StartReadThread()
  	{
  		// signal connect event for this BT dev
  		//ConnectionEvent(1,nIndex,null);
  		
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
	                    //Log.i(LOG_TAG, "StartReadThread: Data received:"+ bread);
	                    // TRY to display receiving data 
	                    m_Handler_Read.post(new DisplayReceivingData(new String(buf, 0, bread)));
  	                   
	        			
	        			/*
	        			int nextInt = m_btSck.getInputStream().read();
	        			m_Handler_Read.post(new DisplayReceivingData(nextInt));
	        			*/
  	                } catch (IOException e) {
  	                	//Log.d(LOG_TAG, "StartReadThread: disconnected", e);
  	                } 
  	        		
  	        		// signal disconnect event
  		        	if (buf == null)
  		        	{
  		        		//ConnectionEvent(2,nIndex, null);
  		        		break;
  		        	}
  	        		else //signal incoming data
  	        		{
  	        			//ConnectionEvent(3,nIndex, buf);
  	        		}
  	        	}
  	        		
  	        }
          };
          m_hReadThread.start();
  		return 0;
  	}
     
     // wrapper class for Runnable since I want to pass data(sensor data) 
     //out of the thread (m_hReadThread)
     private class DisplayReceivingData implements Runnable {
    	 //int sensor1;
     	
    	 String dataRead;
    	 
     	DisplayReceivingData(String inputData) {
     		dataRead = inputData;
     	}
     	
     	
     	
    	 /*
    	 DisplayReceivingData(int input1) {
    		 sensor1 = input1;
    	 }
    	 */
     	public void run() {
     		
     		//concatenate input to a larger input pool, and set it to "" every new second
     		sensorInput += dataRead;
     		//sensorInput = dataRead;
     		//sensorValue = 19;
     		/*
     		int locationLastNewLine = -1;
     		int locationSecondLastNewLine = -1;
     		int sensorValue1 = -1;
     		locationLastNewLine = dataRead.lastIndexOf('\n');
     		if (locationLastNewLine != -1) {
     			locationSecondLastNewLine = dataRead.lastIndexOf('\n', locationLastNewLine);
     		}
     		if (locationLastNewLine != -1 && locationSecondLastNewLine != -1) {
     			sensorValue1 = Integer.parseInt(dataRead.substring(locationSecondLastNewLine, locationLastNewLine));
     			sensorValue = sensorValue1;
     		} else {
     			sensorValue = 0;
     		}
     		*/
     		
     		/*
     		int value = 1;
     		try {
     			value = dataRead.length();
     		} catch (NumberFormatException e) {
     		    value = 2;
     		}
     		sensorValue = value;
     		*/
     		// append receiving data to text view
     		//m_tvD2.setText(dataRead);
     		//m_tvD2.append(dataRead);
     		// from http://stackoverflow.com/questions/3506696/auto-scrolling-textview-in-android-to-bring-text-into-view
     	    // find the amount we need to scroll.  This works by
     	    // asking the TextView's internal layout for the position
     	    // of the final line and then subtracting the TextView's height
     	    //final int scrollAmount = m_tvD2.getLayout().getLineTop(m_tvD2.getLineCount())
     	    ///        - m_tvD2.getHeight();
     	    // if there is no need to scroll, scrollAmount will be <=0
     	    //if(scrollAmount >0)
     	    //    m_tvD2.scrollTo(0, scrollAmount);
     	    //else
     	    //    m_tvD2.scrollTo(0,0);

     	}
     }
}