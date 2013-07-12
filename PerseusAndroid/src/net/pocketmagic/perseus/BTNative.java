package net.pocketmagic.perseus;

import android.util.Log;

public class BTNative {
	private static final String 		LOG_TAG 			= "BTNative";

	static final int					MAX_DEVICES			= 50;
	
	class BTDev {
		String 	m_szName;
		String 	m_szAddress;
		int		m_nBTDEVType;
		
		
		BTDev(String _name, String _address) {
			m_szName = _name; m_szAddress = _address;  
		}
	}
	BTDev	BTDevs[];
	int		BTCount;
	
	BTNative() {
		Log.d(LOG_TAG, "Class constructor called.");
		BTDevs = new BTDev[MAX_DEVICES];
		
		//BTDevs[0] = new BTDev("Device 1 WM","00:11:22:33:44:55");
		//BTDevs[1] = new BTDev("358280023367112","10:21:32:33:44:55");
		BTCount = 0;
	}
	
	int DiscoverBluetoothDevices() {
		Log.d(LOG_TAG, "DiscoverBluetoothDevices: called.");
		int discovered = intDiscover(3); //4*1.28 seconds
		
    	Log.d(LOG_TAG, "DiscoverBluetoothDevices: res " + discovered);
		if (discovered >= 0) {
			/*
			 * 		Go through discovered devices
			 */
			for (int i=0; i < discovered; i++) {
				String foundaddr = intReadDiscoveredAddress(i);
				boolean found = false;
				for (int j=0;j< BTCount; j++) {
					if (BTDevs[j].m_szAddress.compareTo(foundaddr) == 0)
					{
						Log.d(LOG_TAG,"Address already in list:"+foundaddr);
						found = true;
						break;
					}
				}
				if (found == false) {
					Log.d(LOG_TAG, "Found new address:"+foundaddr+".Try connect");
					//add
					if (BTCount < MAX_DEVICES) {
						BTDevs[BTCount] = new BTDev(intReadDiscoveredName(i), intReadDiscoveredAddress(i));
						BTCount++;
					} else Log.d(LOG_TAG, "Max devices reached.");
				}
			}
			return discovered;
		} else  return -1;
	}
	
	// -- import native code -- //
	public native String  	intReadVersion();
	// -- bluetooth related
	private native int	  	intDiscover(int scantime);
	private native String 	intReadDiscoveredName(int index);
	private native String 	intReadDiscoveredSDPName(int index);
	private native String 	intReadDiscoveredAddress(int index);
	private native byte[] 	intReadDiscoveredRD(int index);
	private native byte[] 	intReadDiscoveredClass(int index);
	
	static {
		System.loadLibrary("BTL");
	}
}
