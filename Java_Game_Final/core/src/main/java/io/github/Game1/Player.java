package io.github.Game1;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Player extends Sprite {

    public Player() {
        super(new Texture("dog.png"));
        setPosition(1, 1);
        this.setSize(1, 1);
    }


    public void input() {
        float speed = 3f; // can adjust 
        float delta = Gdx.graphics.getDeltaTime(); // for all hw frame rate
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            this.translateX(-speed*delta); // Move left
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            this.translateX(speed*delta); // Move right
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            this.translateY(speed*delta); // Move up
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            this.translateY(-speed*delta); // Move down
        }

        
    }
}