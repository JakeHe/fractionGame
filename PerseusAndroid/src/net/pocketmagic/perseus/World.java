package net.pocketmagic.perseus;

import java.util.Random;

public class World {
    static final int WORLD_WIDTH = 10;
    static final int WORLD_HEIGHT = 13;
    static final int SCORE_INCREMENT = 10;
    static final float TICK_INITIAL = 0.25f;
    static final float HALF_TICK_INITIAL = 0.25f;
    static final float TICK_DECREMENT = 0.05f;
    static final int DURATION = 10;
    static final int HALFDURATION = DURATION / 2;
    static final int TWOHALFSECONDS =  2;

    //public Snake snake;
    //public Stain stain;
    // counting number of seconds takes players to respond
    public int second = 0;
    public int halfsecond = 0;
    public boolean gameOver = false;
    public int score = 0;
    public int bigOrSmall;
    

    //boolean fields[][] = new boolean[WORLD_WIDTH][WORLD_HEIGHT];
    Random random = new Random();
    float tickTime = 0;
    static float tick = TICK_INITIAL;
    static float half_tick  = HALF_TICK_INITIAL;
    public int firstNum = 0;
    public int secondNum = 0;
    public Fraction firstFraction;
    public Fraction secondFraction;

    public World() {
        //snake = new Snake();
        //placeStain();
    	//firstNum = random.nextInt(19);
    	//secondNum = random.nextInt(10);
    	firstFraction = new Fraction(random.nextInt(9) + 1, random.nextInt(9) + 1);
    	secondFraction = new Fraction(random.nextInt(9) + 1, random.nextInt(9) + 1);
    	bigOrSmall = firstFraction.compareTo(secondFraction);
    }

    /*
    private void placeStain() {
        for (int x = 0; x < WORLD_WIDTH; x++) {
            for (int y = 0; y < WORLD_HEIGHT; y++) {
                fields[x][y] = false;
            }
        }

        int len = snake.parts.size();
        for (int i = 0; i < len; i++) {
            SnakePart part = snake.parts.get(i);
            fields[part.x][part.y] = true;
        }

        int stainX = random.nextInt(WORLD_WIDTH);
        int stainY = random.nextInt(WORLD_HEIGHT);
        while (true) {
            if (fields[stainX][stainY] == false)
                break;
            stainX += 1;
            if (stainX >= WORLD_WIDTH) {
                stainX = 0;
                stainY += 1;
                if (stainY >= WORLD_HEIGHT) {
                    stainY = 0;
                }
            }
        }
        stain = new Stain(stainX, stainY, random.nextInt(3));
    }
	*/
    public void update(float deltaTime) {
        if (gameOver)
            return;

        tickTime += deltaTime;
        
        
        
        while (tickTime > tick) {
            tickTime -= tick;
            
            //snake.advance();
            halfsecond++;
            
            
            if (halfsecond % DURATION == 0) {
            	firstFraction.setTop(random.nextInt(9) + 1);
            	firstFraction.setBottom(random.nextInt(9) + 1);
            	
            	secondFraction.setTop(random.nextInt(9) + 1);
            	secondFraction.setBottom(random.nextInt(9) + 1);
            	
            	bigOrSmall = firstFraction.compareTo(secondFraction);
            }
            
            if (halfsecond % TWOHALFSECONDS == 0) {
            	second++;
            }
            //if (snake.checkBitten()) {
            //    gameOver = true;
                return;
            }
        	/*
            SnakePart head = snake.parts.get(0);
            if (head.x == stain.x && head.y == stain.y) {
                score += SCORE_INCREMENT;
                snake.eat();
                if (snake.parts.size() == WORLD_WIDTH * WORLD_HEIGHT) {
                    gameOver = true;
                    return;
                } else {
                    placeStain();
                }

                if (score % 100 == 0 && tick - TICK_DECREMENT > 0) {
                    tick -= TICK_DECREMENT;
                }
            }
            */
        }
    }

