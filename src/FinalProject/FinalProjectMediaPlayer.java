package FinalProject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.net.URI;

/**
 * Created by Zeta on 5/28/2014.
 */
public class FinalProjectMediaPlayer extends Application  {

    URI uri = new File ("C:/Users/AGE2 Normal/Documents/CSIS10A/Final Project/sample/GUNDAM BUILD FIGHTERS.mp4").toURI();
    Media media = new Media (uri.toString());


    public static void main (String [] args){

        launch(args);
    }
    @Override

    public void start (final Stage stage) throws Exception {

        stage.setTitle("Basic Media Player");
        Group root = new Group();
        Scene scene = new Scene(root, 800,600);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        MediaView mp = new MediaView(mediaPlayer);
        final DoubleProperty width = mp.fitWidthProperty();
        final DoubleProperty height = mp.fitHeightProperty();
        width.bind(Bindings.selectDouble(mp.sceneProperty(), "width"));
        height.bind(Bindings.selectDouble(mp.sceneProperty(), "height"));
        mp.setPreserveRatio(true);
        mediaPlayer.setAutoPlay(true);



        StackPane roots = new StackPane();
        roots.getChildren().add(mp);


        MediaControl mediaControl = new MediaControl(mediaPlayer);
        scene.setRoot(mediaControl);
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
        mediaPlayer.play();


    }
    public static class MediaControl extends BorderPane {

       private MediaPlayer player;
       private MediaView mediaView;
       private final boolean repeat = false;
       private boolean stopRequested = false;
       private boolean atEndOfMedia = false;
       private Duration duration;
       private Slider timeSlider;
       private Label playTime;
       private Slider volumeSlider;
       private HBox mediaBar;

        public MediaControl(final MediaPlayer player) {
            this.player = player;
            setStyle("-fx-background-color: #4682b4;");

            BorderPane borderPane = new BorderPane();
            mediaView = new MediaView(player);
            borderPane.setCenter(mediaView);



            borderPane.setCenter(mediaView);
            borderPane.setStyle("-fx-background-color: steelblue;");
            setCenter(borderPane);



            mediaBar = new HBox();
            mediaBar.setAlignment(Pos.CENTER);
            mediaBar.setPadding(new Insets(10, 15, 10, 15));
            BorderPane.setAlignment(mediaBar, Pos.CENTER);

            final Button playButton = new Button("|>");

            playButton.setOnAction(new EventHandler<ActionEvent>() {

                public void handle(ActionEvent event) {
                    MediaPlayer.Status status = player.getStatus();

                    if (status == MediaPlayer.Status.UNKNOWN || status == MediaPlayer.Status.HALTED) {

                        return;
                    }

                    if(status == MediaPlayer.Status.PAUSED || status == MediaPlayer.Status.READY || status == MediaPlayer.Status.STOPPED) {
                        if (atEndOfMedia) {
                            player.seek(player.getStartTime());
                            atEndOfMedia = false;
                        }

                        player.play();

                    } else {

                        player.pause();
                    }

                }
            });

            player.currentTimeProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observe) {

                    updateValues();
                }


            });

            player.setOnPlaying(new Runnable() {
                @Override
                public void run() {

                    if (stopRequested) {

                        player.pause();
                        stopRequested = false;

                    } else {

                        playButton.setText("||");
                    }
                }
            });

            player.setOnPaused(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Paused");
                    playButton.setText("|>");
                }
            });

            player.setOnReady(new Runnable() {
                @Override
                public void run() {
                    duration = player.getMedia().getDuration();
                    updateValues();
                }
            });

            player.setCycleCount(repeat ? MediaPlayer.INDEFINITE : 1);
            player.setOnEndOfMedia(new Runnable() {
                @Override
                public void run() {

                    if (!repeat) {

                        playButton.setText("|>");
                        stopRequested = true;
                        atEndOfMedia = true;
                    }
                }
            });

            mediaBar.getChildren().add(playButton);
            Label spacer = new Label("   ");
            mediaBar.getChildren().add(spacer);

            Label timeDuration = new Label("Duration:");
            mediaBar.getChildren().add(timeDuration);

            timeSlider = new Slider();
            HBox.setHgrow(timeSlider, Priority.ALWAYS);
            timeSlider.setMinWidth(50);
            timeSlider.setMaxWidth(Double.MAX_VALUE);
            timeSlider.valueProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observe) {

                    if (timeSlider.isValueChanging()) {

                        player.seek(duration.multiply(timeSlider.getValue() / 100.0));
                    }
                }
            });

            mediaBar.getChildren().add(timeSlider);

            playTime = new Label();
            playTime.setPrefWidth(150);
            playTime.setMinWidth(100);
            mediaBar.getChildren().add(playTime);

            Label volumeLabel = new Label("<|))");
            mediaBar.getChildren().add(volumeLabel);

            volumeSlider = new Slider();
            volumeSlider.setPrefWidth(80);
            volumeSlider.setMaxWidth(Region.USE_PREF_SIZE);
            volumeSlider.setMinWidth(40);
            volumeSlider.valueProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observe) {

                    if (volumeSlider.isValueChanging()) {
                        player.setVolume(volumeSlider.getValue() / 100.0);

                    }
                }
            });

            mediaBar.getChildren().add(volumeSlider);
            setBottom(mediaBar);

        }

        private void updateValues() {

            if(playTime != null && timeSlider != null && volumeSlider != null) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Duration currentTime = player.getCurrentTime();
                        playTime.setText(formatTime(currentTime,duration));
                        timeSlider.setDisable(duration.isUnknown());

                        if (!timeSlider.isDisabled() && duration.greaterThan(Duration.ZERO) && !timeSlider.isValueChanging()) {
                            timeSlider.setValue(currentTime.divide(duration).toMillis() * 100.0);
                        }

                        if(!volumeSlider.isValueChanging()) {
                            volumeSlider.setValue((int) Math.round(player.getVolume() * 100));
                        }
                    }
                });
            }
        }

        private static String formatTime (Duration elapsed, Duration duration) {

            int Elapsed = (int) Math.abs(elapsed.toSeconds());
            int Hours = Elapsed / (60 * 60);

            if (Hours > 0) {
                Elapsed -= Hours * 60 * 60;
            }

            int Minutes = Elapsed / 60;
            int Seconds = Elapsed - Hours * 60 * 60 - Minutes * 60;

            if (duration.greaterThan(Duration.ZERO)) {

                int Durations = (int) Math.abs(duration.toSeconds());
                int hours = Durations / (60 * 60);

                if(hours > 0) {

                    Durations -= hours * 60 * 60;

                }

                int minutes = Durations / 60;
                int seconds = Durations - hours * 60 * 60 - minutes * 60;

                if (hours > 0) {

                    return String.format ("%d:%02d:%02d/%d:%02d:%02d", Hours, Minutes, Seconds, hours, minutes, seconds);

                }else{

                    return String.format("%d:%02d:%02d", Hours, Minutes, Seconds);
                }

            }else if (Hours > 0) {
                return String.format("%d:%02d:%02d", Hours, Minutes, Seconds);

            }else{

                return String.format("%02d:%02d", Minutes, Seconds);
            }
        }
    }
}
