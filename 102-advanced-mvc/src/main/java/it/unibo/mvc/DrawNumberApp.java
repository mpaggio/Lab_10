package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param fileName
     *            path of the file to read
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final String fileName, final DrawNumberView... views) throws FileNotFoundException {
        final Configuration.Builder configBuilder = new Configuration.Builder();
        final Configuration config;
        final File configFile = new File(fileName);
        final InputStream configFileInputStream = new FileInputStream(configFile);
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }

        try (var configFileStream = new BufferedReader(new InputStreamReader(configFileInputStream))) {
            for (String fileLine = configFileStream.readLine(); fileLine != null; fileLine = configFileStream.readLine()) {
                final StringTokenizer st = new StringTokenizer(fileLine, ": ");
                final String configurationName = st.nextToken();
                final String configurationValue = st.nextToken();
                if (configurationName.contains("minimum")) {
                    configBuilder.setMin(Integer.parseInt(configurationValue));
                } else if (configurationName.contains("maximum")) {
                    configBuilder.setMax(Integer.parseInt(configurationValue));
                } else if (configurationName.contains("attempts")) {
                    configBuilder.setAttempts(Integer.parseInt(configurationValue));
                }
            }
        } catch (IOException | NullPointerException e) {
            displayError(e.getMessage());
        }
        config = configBuilder.build();
        if (config.isConsistent()) {
            this.model = new DrawNumberImpl(config);
        } else {
            displayError("Configuration is not consistent");
            this.model = new DrawNumberImpl(new Configuration.Builder().build());
        }
    }

    /**
     * @param message
     *          message to be displayed
     */
    private void displayError(final String message) {
        for (final DrawNumberView view : views) {
                view.displayError(message);
            }
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp("src/main/resources/config.yml", 
            new DrawNumberViewImpl(), 
            new DrawNumberViewImpl(),
            new PrintStreamView(System.out),
            new PrintStreamView("output.log"));
    }

}
