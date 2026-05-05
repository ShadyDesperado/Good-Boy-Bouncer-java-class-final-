package io.github.Game1.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import io.github.Game1.Main;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return;
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        try {
            return new Lwjgl3Application(new Main(), getDefaultConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("CRASH: " + e.getMessage());
            throw e;
        }
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Game1");
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        configuration.setWindowedMode(640, 480);
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }
}