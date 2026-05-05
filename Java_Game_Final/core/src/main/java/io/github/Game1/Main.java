package io.github.Game1;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main implements ApplicationListener {

    private enum GameState { START, PLAYING, PAUSED, GAME_OVER, HIGH_SCORES }

    private static class ScoreEntry {
        String name;
        int score;

        ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    private GameState state = GameState.START;
    private GameState previousState = GameState.START;

    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private FitViewport viewport;
    private FitViewport uiViewport;
    private BitmapFont font;
    private GlyphLayout layout = new GlyphLayout();
    private Vector3 touch = new Vector3();

    private Texture dogImage;
    private Texture backgroundImage;
    private Player dog;
    private Sprite background;
    private Ball ball;
    private Sound bark;

    private int score = 0;
    private float wait = 0f;
    private boolean newHighScore = false;
    private boolean askingForName = false;
    private String nameInput = "";

    private static final int MAX_SCORES = 5;
    private static final String SCORES_FILE = "highscores.txt";
    private List<ScoreEntry> highScores = new ArrayList<>();

    private Rectangle btnPlay, btnHighScores, btnContinue, btnBack, btnRestart, btnBackToStart, btnViewHighScores;

    @Override
    public void create() {
        dogImage = new Texture("dog.png");
        backgroundImage = new Texture("background-clouds.jpg");
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = new BitmapFont();
        dog = new Player();
        background = new Sprite(backgroundImage);
        viewport = new FitViewport(8, 5);
        uiViewport = new FitViewport(800, 500);

        background.setSize(viewport.getWorldWidth(), viewport.getWorldHeight());
        background.setPosition(0, 0);

        dog.setSize(1, 1);
        dog.setPosition(viewport.getWorldWidth() / 2f - dog.getWidth() / 2f, 1);

        ball = new Ball();
        ball.setPosition(4, 3);

        bark = Gdx.audio.newSound(Gdx.files.internal("bark.mp3"));

        loadHighScores();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height, true);
        uiViewport.update(width, height, true);
    }

    @Override
    public void render() {
        switch (state) {
            case START: renderStart(); break;
            case PLAYING: renderPlaying(); break;
            case PAUSED: renderPaused(); break;
            case GAME_OVER: renderGameOver(); break;
            case HIGH_SCORES: renderHighScores(); break;
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        dogImage.dispose();
        backgroundImage.dispose();
        batch.dispose();
        shapes.dispose();
        font.dispose();
        bark.dispose();
    }

    private void renderStart() {
        drawWorldBackground();
        drawOverlay(0.55f);

        float cx = uiViewport.getWorldWidth() / 2f;
        float cy = uiViewport.getWorldHeight() / 2f;

        btnPlay = centeredBtn(cx, cy + 10);
        btnHighScores = centeredBtn(cx, cy - 55);

        batch.begin();
        drawCenteredText("Good Boy, Bouncer!", cx, uiViewport.getWorldHeight() - 60, 2.0f, Color.ORANGE);
        drawCenteredText("Keep the ball in the air as long as you can!", cx, uiViewport.getWorldHeight() - 110, 0.8f, Color.WHITE);
        drawCenteredText("Use W-A-S-D to move, hit spacebar to pause the game", cx, uiViewport.getWorldHeight() - 150, 0.8f, Color.WHITE);
        drawButton(btnPlay, "Play");
        drawButton(btnHighScores, "High Scores");
        batch.end();

        if (Gdx.input.justTouched()) {
            float tx = toUiX(Gdx.input.getX(), Gdx.input.getY());
            float ty = toUiY(Gdx.input.getX(), Gdx.input.getY());

            if (btnPlay.contains(tx, ty)) {
                startGame();
            }

            if (btnHighScores.contains(tx, ty)) {
                previousState = GameState.START;
                state = GameState.HIGH_SCORES;
            }
        }
    }

    private void renderPlaying() {
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.BACK) ||
            Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
            state = GameState.PAUSED;
            return;
        }

        dog.input();

        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        background.draw(batch);
        dog.draw(batch);
        ball.update(viewport);
        ball.draw(batch);
        onCollision();
        batch.end();

        uiViewport.apply();
        batch.setProjectionMatrix(uiViewport.getCamera().combined);

        batch.begin();
        drawCenteredText("Score: " + score, uiViewport.getWorldWidth() - 90, uiViewport.getWorldHeight() - 30, 1.4f, Color.WHITE);
        batch.end();

        if (ball.getY() < dog.getHeight()) {
            triggerGameOver();
        }
    }

    private void renderPaused() {
        drawWorldGameplay();
        drawOverlay(0.55f);

        float cx = uiViewport.getWorldWidth() / 2f;
        float cy = uiViewport.getWorldHeight() / 2f;

        btnContinue = centeredBtn(cx, cy + 20);
        btnHighScores = centeredBtn(cx, cy - 45);

        batch.begin();
        drawCenteredText("Pawsed!", cx, uiViewport.getWorldHeight() - 60, 1.4f, Color.ORANGE);
        drawButton(btnContinue, "Continue");
        drawButton(btnHighScores, "High Scores");
        batch.end();

        if (Gdx.input.justTouched()) {
            float tx = toUiX(Gdx.input.getX(), Gdx.input.getY());
            float ty = toUiY(Gdx.input.getX(), Gdx.input.getY());

            if (btnContinue.contains(tx, ty)) {
                state = GameState.PLAYING;
            }

            if (btnHighScores.contains(tx, ty)) {
                previousState = GameState.PAUSED;
                state = GameState.HIGH_SCORES;
            }
        }

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.BACK) ||
            Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
            state = GameState.PLAYING;
        }
    }

    private void renderGameOver() {
        drawWorldBackground();
        drawOverlay(0.65f);

        float cx = uiViewport.getWorldWidth() / 2f;
        float cy = uiViewport.getWorldHeight() / 2f;

        btnRestart = centeredBtn(cx, cy - 35);
        btnViewHighScores = centeredBtn(cx, cy - 100);
        btnBackToStart = centeredBtn(cx, cy - 165);

        batch.begin();
        drawCenteredText("Game Over", cx, uiViewport.getWorldHeight() - 60, 1.4f, Color.WHITE);
        drawCenteredText("Score: " + score, cx, cy + 80, 1.1f, Color.WHITE);

        if (askingForName) {
            drawCenteredText("Enter Name: " + nameInput, cx, cy + 35, 1.0f, Color.WHITE);
            drawCenteredText("Press ENTER to save", cx, cy, 0.8f, Color.LIGHT_GRAY);
        } else if (newHighScore) {
            drawCenteredText("New High Score!", cx, cy + 25, 1.0f, new Color(1f, 0.85f, 0f, 1f));
        } else {
            drawCenteredText("No high score this time! :(", cx, cy + 25, 0.9f, Color.LIGHT_GRAY);
        }

        if (!askingForName) {
            drawButton(btnRestart, "Restart");
            drawButton(btnViewHighScores, "High Scores");
            drawButton(btnBackToStart, "Back to Menu");
        }

        batch.end();

        if (askingForName) {
            handleNameInput();
            return;
        }

        if (Gdx.input.justTouched()) {
            float tx = toUiX(Gdx.input.getX(), Gdx.input.getY());
            float ty = toUiY(Gdx.input.getX(), Gdx.input.getY());

            if (btnRestart.contains(tx, ty)) {
                startGame();
            }

            if (btnViewHighScores.contains(tx, ty)) {
                previousState = GameState.GAME_OVER;
                state = GameState.HIGH_SCORES;
            }

            if (btnBackToStart.contains(tx, ty)) {
                state = GameState.START;
            }
        }
    }

    private void renderHighScores() {
        drawWorldBackground();
        drawOverlay(0.65f);

        float cx = uiViewport.getWorldWidth() / 2f;
        float top = uiViewport.getWorldHeight() - 60;

        btnBack = centeredBtn(cx, 55);

        batch.begin();
        drawCenteredText("Top 5 High Scores", cx, top, 1.3f, Color.WHITE);

        if (highScores.isEmpty()) {
            drawCenteredText("No scores yet!", cx, top - 70, 1.0f, Color.LIGHT_GRAY);
        } else {
            for (int i = 0; i < highScores.size(); i++) {
                ScoreEntry entry = highScores.get(i);
                drawCenteredText((i + 1) + ". " + entry.name + " - " + entry.score, cx, top - 60 - i * 40, 1.0f, Color.WHITE);
            }
        }

        drawButton(btnBack, "Back");
        batch.end();

        if (Gdx.input.justTouched()) {
            float tx = toUiX(Gdx.input.getX(), Gdx.input.getY());
            float ty = toUiY(Gdx.input.getX(), Gdx.input.getY());

            if (btnBack.contains(tx, ty)) {
                state = previousState;
            }
        }
    }

    private void handleNameInput() {
        for (int key = com.badlogic.gdx.Input.Keys.A; key <= com.badlogic.gdx.Input.Keys.Z; key++) {
            if (Gdx.input.isKeyJustPressed(key) && nameInput.length() < 12) {
                nameInput += com.badlogic.gdx.Input.Keys.toString(key);
            }
        }

        for (int key = com.badlogic.gdx.Input.Keys.NUM_0; key <= com.badlogic.gdx.Input.Keys.NUM_9; key++) {
            if (Gdx.input.isKeyJustPressed(key) && nameInput.length() < 12) {
                nameInput += com.badlogic.gdx.Input.Keys.toString(key).replace("Num ", "");
            }
        }

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE) && nameInput.length() < 12) {
            nameInput += " ";
        }

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.BACKSPACE) && nameInput.length() > 0) {
            nameInput = nameInput.substring(0, nameInput.length() - 1);
        }

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
            String finalName = nameInput.trim();

            if (finalName.isEmpty()) {
                finalName = "Player";
            }

            newHighScore = tryAddHighScore(finalName, score);
            askingForName = false;
        }
    }

    private void drawWorldBackground() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        background.draw(batch);
        batch.end();

        uiViewport.apply();
        batch.setProjectionMatrix(uiViewport.getCamera().combined);
        shapes.setProjectionMatrix(uiViewport.getCamera().combined);
    }

    private void drawWorldGameplay() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        background.draw(batch);
        dog.draw(batch);
        ball.draw(batch);
        batch.end();

        uiViewport.apply();
        batch.setProjectionMatrix(uiViewport.getCamera().combined);
        shapes.setProjectionMatrix(uiViewport.getCamera().combined);
    }

    private void startGame() {
        score = 0;
        wait = 0f;
        newHighScore = false;
        askingForName = false;
        nameInput = "";
        dog.setPosition(viewport.getWorldWidth() / 2f - dog.getWidth() / 2f, 1);
        ball.setPosition(4, 3);
        state = GameState.PLAYING;
    }

    private void triggerGameOver() {
        state = GameState.GAME_OVER;
        nameInput = "";

        if (isHighScoreEligible(score)) {
            askingForName = true;
        } else {
            askingForName = false;
            newHighScore = false;
        }
    }

    private boolean isHighScoreEligible(int newScore) {
        if (highScores.size() < MAX_SCORES) {
            return true;
        }

        for (ScoreEntry entry : highScores) {
            if (newScore > entry.score) {
                return true;
            }
        }

        return false;
    }

    private void onCollision() {
        Rectangle dogHitbox = dog.getBoundingRectangle();
        Rectangle ballHitbox = ball.getBoundingRectangle();

        if (wait > 0) {
            wait -= Gdx.graphics.getDeltaTime();
        }

        if (wait <= 0 && dogHitbox.overlaps(ballHitbox)) {
            score++;
            bark.play();
            wait = 1f;

            float halfHeight = viewport.getWorldHeight() / 2f;

            ball.setPosition(
                MathUtils.random(0, viewport.getWorldWidth() - ball.getWidth()),
                MathUtils.random(halfHeight, viewport.getWorldHeight() - ball.getHeight())
            );

            ball.setVelocityY(Math.abs(ball.getVelocityY()));
        }
    }

    public void moveX(int i) {
        dog.translateX(i * Gdx.graphics.getDeltaTime());
        dog.setX(MathUtils.clamp(dog.getX(), 0, viewport.getWorldWidth() - dog.getWidth()));
    }

    public void moveY(int i) {
        dog.translateY(i * Gdx.graphics.getDeltaTime());
        dog.setY(MathUtils.clamp(dog.getY(), 0, viewport.getWorldHeight() - dog.getHeight()));
    }

    private boolean tryAddHighScore(String name, int newScore) {
        highScores.add(new ScoreEntry(name, newScore));
        highScores.sort((a, b) -> Integer.compare(b.score, a.score));

        boolean madeIt = false;

        for (int i = 0; i < highScores.size() && i < MAX_SCORES; i++) {
            ScoreEntry entry = highScores.get(i);

            if (entry.name.equals(name) && entry.score == newScore) {
                madeIt = true;
                break;
            }
        }

        if (highScores.size() > MAX_SCORES) {
            highScores = new ArrayList<>(highScores.subList(0, MAX_SCORES));
        }

        saveHighScores();
        return madeIt;
    }

    private void loadHighScores() {
        highScores.clear();

        com.badlogic.gdx.files.FileHandle fh = Gdx.files.local(SCORES_FILE);

        if (!fh.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new StringReader(fh.readString()))) {
            String line;

            while ((line = br.readLine()) != null && highScores.size() < MAX_SCORES) {
                line = line.trim();

                if (!line.isEmpty()) {
                    String[] parts = line.split(",", 2);

                    if (parts.length == 2) {
                        highScores.add(new ScoreEntry(parts[0], Integer.parseInt(parts[1])));
                    } else {
                        highScores.add(new ScoreEntry("Player", Integer.parseInt(line)));
                    }
                }
            }

            highScores.sort((a, b) -> Integer.compare(b.score, a.score));
        } catch (Exception e) {
            highScores.clear();
        }
    }

    private void saveHighScores() {
        com.badlogic.gdx.files.FileHandle fh = Gdx.files.local(SCORES_FILE);
        StringBuilder sb = new StringBuilder();

        for (ScoreEntry entry : highScores) {
            sb.append(entry.name).append(",").append(entry.score).append('\n');
        }

        fh.writeString(sb.toString(), false);
    }

    private void drawOverlay(float alpha) {
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0, 0, 0, alpha);
        shapes.rect(0, 0, uiViewport.getWorldWidth(), uiViewport.getWorldHeight());
        shapes.end();
        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
    }

    private Rectangle centeredBtn(float cx, float cy) {
        float w = 190;
        float h = 42;
        return new Rectangle(cx - w / 2f, cy - h / 2f, w, h);
    }

    private void drawButton(Rectangle r, String label) {
        batch.end();

        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0.15f, 0.15f, 0.15f, 0.85f));
        shapes.rect(r.x, r.y, r.width, r.height);
        shapes.end();
        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);

        batch.begin();
        drawCenteredText(label, r.x + r.width / 2f, r.y + 27, 0.9f, Color.WHITE);
    }

    private void drawCenteredText(String text, float cx, float y, float scale, Color color) {
        font.getData().setScale(scale);
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, text, cx - layout.width / 2f, y);
    }

    private float toUiX(float screenX, float screenY) {
        touch.set(screenX, screenY, 0);
        uiViewport.unproject(touch);
        return touch.x;
    }

    private float toUiY(float screenX, float screenY) {
        touch.set(screenX, screenY, 0);
        uiViewport.unproject(touch);
        return touch.y;
    }
}