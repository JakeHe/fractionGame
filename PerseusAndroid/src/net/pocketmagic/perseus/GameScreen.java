package net.pocketmagic.perseus;

import java.io.IOException;
import java.util.List;

import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.os.Handler;

import java.util.Random;

//import net.pocketmagic.perseus.MainMenuScreen.DisplayReceivingData;

import com.badlogic.androidgames.framework.Game;
import com.badlogic.androidgames.framework.Graphics;
import com.badlogic.androidgames.framework.Input.TouchEvent;
import com.badlogic.androidgames.framework.Pixmap;
import com.badlogic.androidgames.framework.Screen;

public class GameScreen extends Screen {
	
    enum GameState {
        Ready,
        Running,
        Paused,
        GameOver
    }
    
    GameState state = GameState.Ready;
    World world;
    int oldScore = 0;
    String score = "0";
    String time = "0";
    //int timeInNum = 0;
    //int firstNum = 0;
    //int secondNum = 0;
    String filtedSensorInput;
    int oldSecond = 0;
    //int filtedInput1;
    //String oldSensorReadings[];
    String newSensorReadings[];
    // need to fix; fix the length of the array to 3 (3 light sensors); not good at all
    int oldSensorReadingsint[] = {0,0,0};
    int newSensorReadingsint[] = {0,0,0};
    boolean valueDropped[] = {false, false, false};
    boolean DropsShouldBe[] = {false, false, false}; 
    
    static final float DROP_PER_RATE = 0.40f;
    
    //flag to indicate if user tried to answer the question
    boolean QUESTION_ANSWERED =  false;
    String userAnswer = "";
    
    //flag to indicate if user tried to answer the question
    boolean userTried = false;
    
    
    Random random = new Random();
    
    public GameScreen(Game game) {
        super(game);
        world = new World();
        filtedSensorInput = game.getSensorInput().substring(0);
        game.setSensorInputToEmpty();
        oldSecond = world.halfsecond;
        //filtedInput1 = game.getSensorData1();
    }

    @Override
    public void update(float deltaTime) {
        List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
        game.getInput().getKeyEvents();
        
        if(state == GameState.Ready)
            updateReady(touchEvents);
        if(state == GameState.Running)
            updateRunning(touchEvents, deltaTime);
        if(state == GameState.Paused)
            updatePaused(touchEvents);
        if(state == GameState.GameOver)
            updateGameOver(touchEvents);        
    }
    
    private void updateReady(List<TouchEvent> touchEvents) {
        if(touchEvents.size() > 0)
            state = GameState.Running;
    }
    
    private void updateRunning(List<TouchEvent> touchEvents, float deltaTime) {    
    	Graphics g = game.getGraphics();
    	BluetoothSocket m_btSck = game.getBluetoothSocket(); 
        int len = touchEvents.size();
        for(int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if(event.type == TouchEvent.TOUCH_UP) {
                if(event.x < 64 && event.y < 64) {
                    if(Settings.soundEnabled)
                        Assets.click.play(1);
                    state = GameState.Paused;
                    return;
                }
            }
            
            if(inBounds(event, 0, g.getHeight() - 64, 64, 64)) {
                //Settings.soundEnabled = !Settings.soundEnabled;
                //if(Settings.soundEnabled)
                //    Assets.click.play(1);
            	//send data to speaker for testing purpose
        		try {
    				byte[] byteString = ("do\n ").getBytes();
    				byteString[byteString.length - 1] = 0;
    				m_btSck.getOutputStream().write(byteString);
    				
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
            	
            }
            
            if(event.type == TouchEvent.TOUCH_DOWN) {
                if(event.x < 64 && event.y > 416) {
                    //world.snake.turnLeft();
                }
                if(event.x > 256 && event.y > 416) {
                    //world.snake.turnRight();
                }
            }
        }
        
        world.update(deltaTime);
        if(world.gameOver) {
            if(Settings.soundEnabled)
                Assets.bitten.play(1);
            state = GameState.GameOver;
        }
        
        //counting time from 0 ---> 4
        time = "" + world.second % World.HALFDURATION;
        
        // default the dropped flag, every 5 seconds
        if (world.second % World.HALFDURATION == 0) {
        	QUESTION_ANSWERED = false;
        	userTried = false;
        	userAnswer = "";
        	for (int i = 0; i < valueDropped.length; i++) {
        		valueDropped[i] = false;
        		DropsShouldBe[i] = false;
        	}
        }
        
      //update the value every second
        if (world.halfsecond - oldSecond == 1) {
        	oldSecond = world.halfsecond;
        	filtedSensorInput = game.getSensorInput().substring(0);
        	//empty sensor buffer every second
        	game.setSensorInputToEmpty();
        	//filtedInput1 = game.getSensorData1();
        }
        //if (world.second % 10 == 0) {
        	//firstNum = random.nextInt(10);
        	//secondNum = random.nextInt(10);
        	
        //} 
        
        
        if(oldScore != world.score) {
            oldScore = world.score;
            score = "" + oldScore;
            
            //time = "" + world.second;
            
            if(Settings.soundEnabled)
                Assets.eat.play(1);
        }
    }
    
    private void updatePaused(List<TouchEvent> touchEvents) {
        int len = touchEvents.size();
        for(int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if(event.type == TouchEvent.TOUCH_UP) {
                if(event.x > 80 && event.x <= 240) {
                    if(event.y > 100 && event.y <= 148) {
                        if(Settings.soundEnabled)
                            Assets.click.play(1);
                        state = GameState.Running;
                        return;
                    }                    
                    if(event.y > 148 && event.y < 196) {
                        if(Settings.soundEnabled)
                            Assets.click.play(1);
                        game.setScreen(new MainMenuScreen(game));                        
                        return;
                    }
                }
            }
        }
    }
    
    private void updateGameOver(List<TouchEvent> touchEvents) {
        int len = touchEvents.size();
        for(int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if(event.type == TouchEvent.TOUCH_UP) {
                if(event.x >= 128 && event.x <= 192 &&
                   event.y >= 200 && event.y <= 264) {
                    if(Settings.soundEnabled)
                        Assets.click.play(1);
                    game.setScreen(new MainMenuScreen(game));
                    return;
                }
            }
        }
    }
    

    @Override
    public void present(float deltaTime) {
        Graphics g = game.getGraphics();
        
        g.drawPixmap(Assets.background, 0, 0);
        drawWorld(world);
        if(state == GameState.Ready) 
            drawReadyUI();
        if(state == GameState.Running)
            drawRunningUI();
        if(state == GameState.Paused)
            drawPausedUI();
        if(state == GameState.GameOver)
            drawGameOverUI();
        
        drawText(g, time, g.getWidth() / 2 - score.length()*20 / 2, g.getHeight() - 42);                
    }
    
    private void drawWorld(World world) {
        Graphics g = game.getGraphics();
        //Snake snake = world.snake;
        //SnakePart head = snake.parts.get(0);
        //Stain stain = world.stain;
        
        /*
        Pixmap stainPixmap = null;
        if(stain.type == Stain.TYPE_1)
            stainPixmap = Assets.stain1;
        if(stain.type == Stain.TYPE_2)
            stainPixmap = Assets.stain2;
        if(stain.type == Stain.TYPE_3)
            stainPixmap = Assets.stain3;
        int x = stain.x * 32;
        int y = stain.y * 32;      
        g.drawPixmap(stainPixmap, x, y);             
        
        int len = snake.parts.size();
        for(int i = 1; i < len; i++) {
            SnakePart part = snake.parts.get(i);
            x = part.x * 32;
            y = part.y * 32;
            g.drawPixmap(Assets.tail, x, y);
        }
        
        Pixmap headPixmap = null;
        if(snake.direction == Snake.UP) 
            headPixmap = Assets.headUp;
        if(snake.direction == Snake.LEFT) 
            headPixmap = Assets.headLeft;
        if(snake.direction == Snake.DOWN) 
            headPixmap = Assets.headDown;
        if(snake.direction == Snake.RIGHT) 
            headPixmap = Assets.headRight;        
        x = head.x * 32 + 16;
        y = head.y * 32 + 16;
        g.drawPixmap(headPixmap, x - headPixmap.getWidth() / 2, y - headPixmap.getHeight() / 2);
    */
    }
    
    private void drawReadyUI() {
        Graphics g = game.getGraphics();
        
        g.drawPixmap(Assets.ready, 47, 100);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }
    
    private void drawRunningUI() {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.buttons, 0, 0, 64, 128, 64, 64);
        
        
        //drawText(g, Integer.toString(firstNum) , g.getWidth() / 2 - score.length()*20 / 2, g.getHeight() - 342);
        //drawText(g, Integer.toString(secondNum), g.getWidth() / 2 - score.length()*20 / 2, g.getHeight() - 242);
            
        //draw first fraction
        drawText(g, Integer.toString(world.firstFraction.top) , g.getWidth() / 2 - score.length()*20 / 2, g.getHeight() - 382);
        g.drawPixmap(Assets.bar, g.getWidth() / 2 - score.length()*20 / 2 - 5, g.getHeight() - 354);
        drawText(g,  Integer.toString(world.firstFraction.bottom), g.getWidth() / 2 - score.length()*20 / 2, g.getHeight() - 342);
        
        //draw second fraction
        drawText(g, Integer.toString(world.secondFraction.top) , g.getWidth() / 2 - score.length()*20 / 2, g.getHeight() - 282);
        g.drawPixmap(Assets.bar, g.getWidth() / 2 - score.length()*20 / 2 - 5, g.getHeight() - 254);
        drawText(g,  Integer.toString(world.secondFraction.bottom), g.getWidth() / 2 - score.length()*20 / 2, g.getHeight() - 242);
        
        
        //speaker
        g.drawPixmap(Assets.buttons, 0, 416, 0, 0, 64, 64);
        
        
        //display sensor value 
        //drawText(g, Integer.toString(game.getSensorValue()) , g.getWidth() / 2 - score.length()*20 / 2, g.getHeight() - 182);
        g.drawText(filtedSensorInput,  0, g.getHeight() - 182);
        
        //try to find newline
        if (filtedSensorInput.contains("\n")){
            g.drawText("find newline",  g.getWidth() / 2 - score.length()*20 / 2, g.getHeight() - 162);
        } else {
            g.drawText("not find newline",  g.getWidth() / 2 - score.length()*20 / 2, g.getHeight() - 162);

        }
        
        // IMPROVEMENT can be make
        // 1. read value between last two newline character instead of first two
        // 2. increase response time by increasing frequency from 1 second per query to half second per query
        
        int posFirstNewline = -1;
        int posSecondNewline = -1;
        if ((posFirstNewline = filtedSensorInput.indexOf("\n", 0)) != -1){
        	g.drawText("pos " + posFirstNewline,  g.getWidth() / 2 - score.length()*20 / 2, g.getHeight() - 142);
        }
        if (posFirstNewline != -1 && posFirstNewline + 1 < filtedSensorInput.length()) {
        	if ((posSecondNewline = filtedSensorInput.indexOf("\n", posFirstNewline + 1)) != -1){
            	g.drawText("pos " + posSecondNewline,  g.getWidth() / 2 - score.length()*20 / 2, g.getHeight() - 122);
        	}
        	else {
        		g.drawText("pos " + posSecondNewline,  g.getWidth() / 2 - score.length()*20 / 2, g.getHeight() - 122);
        	}
        } else{
        	g.drawText("pos " + posSecondNewline,  g.getWidth() / 2 - score.length()*20 / 2, g.getHeight() - 122);
        }
        
        // get the first sensor value
  if (posFirstNewline != -1 && posSecondNewline != -1) {
        	String valuestr = (String) filtedSensorInput.subSequence(posFirstNewline + 1, posSecondNewline);
        	g.drawText("value " + valuestr ,  g.getWidth() / 2 - score.length()*20 / 2, g.getHeight() - 102);
        	
        	//keep track of the old sensor values
        	for (int i = 0; i < newSensorReadingsint.length; i++) {
        		oldSensorReadingsint[i] = newSensorReadingsint[i];
        	}
        	
        	// extract individual sensor data as integer
        	newSensorReadings = valuestr.trim().split(" ");
        	for (int i = 0; i < newSensorReadings.length; i++) {
        		newSensorReadingsint[i] = Integer.valueOf(newSensorReadings[i]);
        		g.drawText("value " + newSensorReadingsint[i]  ,  g.getWidth() / 2 - score.length()*20 / 2, g.getHeight() - 82 + 20 * i);
        	}
        	
        	for (int i = 0; i < valueDropped.length; i++) {
        		//if values dropped, set the flags
            	if (!valueDropped[i]) {
            		if (dropped(oldSensorReadingsint[i], newSensorReadingsint[i])) {
            			//g.drawText("dropped"  ,  g.getWidth() / 4, g.getHeight() - 82 + 20 * 0);
            			valueDropped[i] = true;
            			// record if user tried to answer the question
            			userTried = true;
            		}
            	}
            	
            	
            	if (valueDropped[i] == false) {
            		g.drawText("same"  ,  g.getWidth() / 4, g.getHeight() - 82 + 20 * i);
            	} else {
            		g.drawText("dropped"  ,  g.getWidth() / 4, g.getHeight() - 82 + 20 * i);
            	}
        	}
        }
      
  		if (world.bigOrSmall > 0 ) {
  			g.drawText("first one is larger", g.getWidth() / 2, g.getHeight() / 2 - 60);
  			// position 0, drop should be high
  			DropsShouldBe[0] = true;
  		} else if (world.bigOrSmall < 0) {
  			g.drawText("second one is larger", g.getWidth() / 2, g.getHeight() / 2 - 60);
  			DropsShouldBe[2] = true;
  		} else {
  			g.drawText("equal ", g.getWidth() / 2, g.getHeight() / 2 - 60);
  			DropsShouldBe[1] = true;
  		}
  		if (userTried && !QUESTION_ANSWERED) {
  			QUESTION_ANSWERED = true;
  			userAnswer = "RIGHT";
  			for (int i = 0; i < DropsShouldBe.length; i++) {
  	  			if (DropsShouldBe[i] != valueDropped[i]){
  	  				userAnswer = "Wrong";
  	  				break;
  	  			}
  		}
  		}
  		if (QUESTION_ANSWERED) {
  			g.drawText(userAnswer, g.getWidth() / 2, 60);
  		}
        //drawText(g, time, g.getWidth() / 2 - score.length()*20 / 2, g.getHeight() - 42);
        
        //g.drawLine(0, 416, 480, 416, Color.BLACK);
       // g.drawPixmap(Assets.buttons, 0, 416, 64, 64, 64, 64);
       // g.drawPixmap(Assets.buttons, 256, 416, 0, 64, 64, 64);
    }
    private boolean dropped(int oldValue, int newValue) {
    	float drop = (float) oldValue - newValue;
    	float dropPer = drop / oldValue;
    	if (dropPer > DROP_PER_RATE) 
    		return true;
    	else 
    		return false;
    } 
    
    private void drawPausedUI() {
        Graphics g = game.getGraphics();
        
        g.drawPixmap(Assets.pause, 80, 100);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    private void drawGameOverUI() {
        Graphics g = game.getGraphics();
        
        g.drawPixmap(Assets.gameOver, 62, 100);
        g.drawPixmap(Assets.buttons, 128, 200, 0, 128, 64, 64);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }
    
    public void drawText(Graphics g, String line, int x, int y) {
        int len = line.length();
        for (int i = 0; i < len; i++) {
            char character = line.charAt(i);

            if (character == ' ') {
                x += 20;
                continue;
            }

            int srcX = 0;
            int srcWidth = 0;
            if (character == '.') {
                srcX = 200;
                srcWidth = 10;
            } else {
                srcX = (character - '0') * 20;
                srcWidth = 20;
            }

            g.drawPixmap(Assets.numbers, x, y, srcX, 0, srcWidth, 32);
            x += srcWidth;
        }
    }
    
    @Override
    public void pause() {
        if(state == GameState.Running)
            state = GameState.Paused;
        
        if(world.gameOver) {
            Settings.addScore(world.score);
            Settings.save(game.getFileIO());
        }
    }

    @Override
    public void resume() {
        
    }

    @Override
    public void dispose() {
        
    }
    private boolean inBounds(TouchEvent event, int x, int y, int width, int height) {
        if(event.x > x && event.x < x + width - 1 && 
           event.y > y && event.y < y + height - 1) 
            return true;
        else
            return false;
    }
}