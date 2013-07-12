package net.pocketmagic.perseus;

import android.bluetooth.BluetoothSocket;

import com.badlogic.androidgames.framework.Screen;
import com.badlogic.androidgames.framework.impl.AndroidGame;

public class MrNomGame extends AndroidGame {
    @Override
    public Screen getStartScreen() {
        return new LoadingScreen(this); 
    }
    
    //set game's Bluetooth member variable
    public BluetoothSocket getBluetoothSocket(){
    	AndroidApplication globalState = (AndroidApplication)this.getApplication();
		return globalState.getBluetoothSocket();
    };
}