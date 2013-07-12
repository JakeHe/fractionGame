package net.pocketmagic.perseus;

import java.io.IOException;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.Menu;
//import com.badlogic.androidgames.mrnom.MrNomGame;
//import com.badlogic.androidgames.mrnom.SingleTouchTest;

//Main window contains two buttons
// button 1 to set up sensors
// button 2 to launch games


public class SecondLevel extends Activity {
	public static final int idSetupSensors = Menu.FIRST + 1,
							idStartBluetooth = Menu.FIRST + 2,
							idLaunchGame = Menu.FIRST + 3,
							START_BLUETOOTH = 5;
	
	
	private class ButtonHandler implements View.OnClickListener
    {
	public void onClick(View v)
	{
		handleButtonClick(v);
	}	
    }
	
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        
      //first button -- launch activity to set up bluetooth
    	Button startActStartBluetooth = new Button(this);
    	startActStartBluetooth.setId(idStartBluetooth);
    	startActStartBluetooth.setText("Start Bluetooth");
    	startActStartBluetooth.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    	layout.addView(startActStartBluetooth);
    	startActStartBluetooth.setOnClickListener(new ButtonHandler());
    	
    	
    	// second button -- launch activity to set up sensors
        Button startActSetupSensors = new Button(this);
        startActSetupSensors.setId(idSetupSensors);
        startActSetupSensors.setText("Set up sensors");
    	layout.addView(startActSetupSensors, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

    	// activating the button
    	startActSetupSensors.setOnClickListener(new ButtonHandler());
        
    	
    	
    	
    	//third button -- launch activity to set up bluetooth
    	Button startActLaunchGame = new Button(this);
    	startActLaunchGame.setId(idLaunchGame);
    	startActLaunchGame.setText("Start Fraction game");
    	startActLaunchGame.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    	layout.addView(startActLaunchGame);
    	startActLaunchGame.setOnClickListener(new ButtonHandler());
    	
    	
       	// setting the view to our layout
        setContentView(layout);
    }
    
    private void handleButtonClick(View v)
    {
    	// use button id to know which button is clicked
    	
    	int cmdId = v.getId();
    	
    	// launch activity to set up sensors
    	if (cmdId == idSetupSensors) {
    		//
    		AndroidApplication appState = (AndroidApplication) this.getApplication();
    		Toast.makeText(this, "globalVariable = " + appState.getGlobalVariable(), Toast.LENGTH_LONG).show();
    	}
    	
    	// launch activity to set up bluetooth 
    	if (cmdId == idStartBluetooth ){
    		//startActivity(new Intent(this, SingleTouchTest.class));
    	}
    	
    	// launch games
    	if (cmdId == idLaunchGame) {
    		//startActivity(new Intent(this, RenderViewTest.class));
    		//testing bluetooth
    		AndroidApplication globalState = (AndroidApplication)this.getApplication();
    		BluetoothSocket m_btSck = globalState.getBluetoothSocket();
    		
    		try {
				byte[] byteString = ("do\n ").getBytes();
				byteString[byteString.length - 1] = 0;
				m_btSck.getOutputStream().write(byteString);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    
}