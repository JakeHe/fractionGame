package net.pocketmagic.perseus;

import android.app.Application;
import android.bluetooth.BluetoothSocket;

public class AndroidApplication extends Application {
	private static AndroidApplication sInstance;
	private int globalVariable = 6;
	// make bluetooth socket a gloabla variable so we access it from other activities
	private BluetoothSocket btSocket;
	
	public static AndroidApplication getInstance() {
		return sInstance;
	}

	public void onCreate() {
		super.onCreate();
		sInstance = this;
		sInstance.initializeInstance();
	}
	
	public int getGlobalVariable() {
		return globalVariable;
	}
	
	// set bluetooth socket
	public void setBluetoothSocket(BluetoothSocket socket) {
		btSocket = socket;
	} 
	
	// get bluetooth socket
	public BluetoothSocket getBluetoothSocket() {
		return btSocket;
	}
	
	protected void initializeInstance() {
		
	}
	
	
}