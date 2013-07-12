package com.badlogic.androidgames.framework;

import android.bluetooth.BluetoothSocket;

public interface Game {
    public Input getInput();

    public FileIO getFileIO();

    public Graphics getGraphics();

    public Audio getAudio();

    public void setScreen(Screen screen);

    public Screen getCurrentScreen();

    public Screen getStartScreen();
    
    public BluetoothSocket getBluetoothSocket();
    
    public String getSensorInput();
    
    public int getSensorData1();
    
    public void setSensorInputToEmpty();
}