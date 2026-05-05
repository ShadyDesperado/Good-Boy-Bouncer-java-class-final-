package io.github.Game1;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Ball extends Sprite {
    private float velocityX = 2f;
    private float velocityY = 2f;

    public Ball() {
        super(new Texture("ball.png"));
        setPosition(4, 3);
        this.setSize(1.5f, 1.5f);
    }

    public void update(FitViewport viewport) {
        this.translateX(velocityX * Gdx.graphics.getDeltaTime());
        this.translateY(velocityY * Gdx.graphics.getDeltaTime());

        if (this.getX() < 0) {
            this.setX(0);
            velocityX = Math.abs(velocityX);
        } else if (this.getX() > viewport.getWorldWidth() - this.getWidth()) {
            this.setX(viewport.getWorldWidth() - this.getWidth());
            velocityX = -Math.abs(velocityX);
        }

        if (this.getY() < 0) {
            this.setY(0);
            velocityY = Math.abs(velocityY);
        } else if (this.getY() > viewport.getWorldHeight() - this.getHeight()) {
            this.setY(viewport.getWorldHeight() - this.getHeight());
            velocityY = -Math.abs(velocityY);
        }
    }
    public float getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(float velocityY) {
        this.velocityY = velocityY;
    }
}