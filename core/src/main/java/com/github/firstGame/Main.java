package com.github.firstGame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;
    private Texture backgroundTexture;
    private Texture bucketTexture;
    private Texture dropTexture;
    private Sound dropSound;
    private Music music;
    private SpriteBatch spriteBatch;
    private FitViewport viewport;
    private Sprite bucketSprite; // declaring a new Sprite variable
    private Vector2 touchPosition;
    private Array<Sprite> dropSprites;
    private float timer;
    private Rectangle bucketRectangle;
    private Rectangle dropRectangle;

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
        backgroundTexture = new Texture("background.png");
        bucketTexture = new Texture("bucket.png");
        dropTexture = new Texture("drop.png");
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(12,7);

        bucketSprite = new Sprite(bucketTexture); // Initialize the sprite based on the texture
        bucketSprite.setSize(1, 1); // Define the size of the sprite
        touchPosition = new Vector2();
        dropSprites = new Array<>();
        bucketRectangle = new Rectangle();
        dropRectangle = new Rectangle();

        // instructions for the music
        music.setLooping(true);
        music.setVolume(.3f); // setting the volume of the music
        music.play();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // updating the viewport
    }


    @Override
    public void render() {
        // organize code into three methods
        input();
        logic();
        draw();
    }

    private void input(){
        float speed = 4f; // set the bucket x to what it currently is right now plus a little bit more (the .25)
        float delta = Gdx.graphics.getDeltaTime(); // Delta time is the measured time between frames

        // keyboard control
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)){ // move bucket to the right
            bucketSprite.translateX(speed * delta); // speed times delta to counteract differences in framerate
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) { // move bucket to the left
            bucketSprite.translateX(-speed * delta);
        }

        // Mouse and Touch control
        if (Gdx.input.isTouched()){ // if the user touches or clicks the screen
            touchPosition.set(Gdx.input.getX(), Gdx.input.getY()); // getting the coordinates where the user clicked or touched
            viewport.unproject(touchPosition); // converting the units to the world units of the viewport

            // Note: the y-coordinate doesn't matter. We just need the x-coordinate
            bucketSprite.setCenterX(touchPosition.x); // changing the x-coordinate (the horizontally) of the bucket
        }
    }

    private void logic(){
        // storing the width and height of the world in local variables
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        // storing the width and height of the bucket in local variables
        float bucketWidth = bucketSprite.getWidth();
        float bucketHeight = bucketSprite.getHeight();

        float delta = Gdx.graphics.getDeltaTime(); // getting the current delta time

        // initialising the bucket rectangle to the coordinates of the actual bucket
        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(), bucketWidth, bucketHeight);

        // limiting the x coordinate of the bucket between 0 and the width of the world
        // Note: the right limit needs to be subtracted by the width of the bucket because the origin of the bucket is on it´s bottom left
        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketWidth));

        // looping through the array with all raindrops
        for (int i = dropSprites.size - 1; i >= 0; i--){
            Sprite dropSprite = dropSprites.get(i); // getting the raindrop in the array at index i
            float dropWidth = dropSprite.getWidth(); // initialising the width of the rain drop in a local variable
            float dropHeight = dropSprite.getHeight(); // initialising the height of the rain drop in a local variable

            dropSprite.translateY(-2f * delta); // move the raindrop downwards while it depends on delta

            // initialising the raindrop rectangle to the coordinates of the raindrop at index i
            dropRectangle.set(dropSprite.getX(), dropSprite.getY(), dropWidth, dropHeight);

            // removing the raindrop as soon as it's outside the viewport
            if (dropSprite.getY() < -dropHeight){
                dropSprites.removeIndex(i);
            }
            // removing the raindrop if it overlaps the bucket
            else if (bucketRectangle.overlaps(dropRectangle)) {
                dropSprites.removeIndex(i);
                dropSound.play(); // playing the sound when the raindrop has been collected
            }
        }

        // creating the rain drop every second
        timer += delta; // adding the current delta to the timer
        if (timer > 1f){ // if it has been more than a second
            createDroplet(); // create the raindrop
            timer = 0; // reset the timer
        }
    }

    private void draw(){
        ScreenUtils.clear(Color.BLACK); // clears the screen to provide getting weird graphical errors
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined); // shows how the viewport is applied to the spriteBatch
        spriteBatch.begin();
        // drawing everything we need for the game

        // storing the variables of the worldWidth and worldHeight
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight); // draw the background at first

        // drawing every single raindrop which is in the array dropSprites onto the background
        for (Sprite dropSprite : dropSprites){
            dropSprite.draw(spriteBatch);
        }

        bucketSprite.draw(spriteBatch); // and in the end drawing the bucket

        spriteBatch.end(); // ending
    }

    private void createDroplet() {
        // create local variables for the width and height of the raindrop
        float dropWidth = 1;
        float dropHeight = 1;

        // create a local variable for the width and height of the game world
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        // creating the actual raindrop
        Sprite dropSprite = new Sprite(dropTexture); // creating a sprite for the raindrop
        dropSprite.setSize(dropWidth, dropHeight); // setting the size of the raindrop
        dropSprite.setX(MathUtils.random(0f, worldWidth - dropWidth)); // setting the x-coordinate randomly of the raindrop
        dropSprite.setY(worldHeight); // setting the y-coordinate of the raindrop (top border)
        dropSprites.add(dropSprite); // Add it to the list
    }
}
