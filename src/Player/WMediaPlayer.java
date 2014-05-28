package Player;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.URI;


/**
 * Created by AGE2 Normal on 5/26/14.
 */
public class WMediaPlayer extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        stage.setTitle("Movie Player");
        Group root = new Group();

        URI uri = new File("U:/Final Project/sample/Los Angeles International Airport.mp4").toURI();


        Media media = new Media(uri.toString());
        //Media media = new Media("file://U:/Final%20Project/sample/GUNDAM%20BUILD%20FIGHTERS.mp4");

        final MediaPlayer player = new MediaPlayer(media);
        MediaView view = new MediaView(player);

        final Timeline slideIn = new Timeline();
        final Timeline slideOut = new Timeline();
        root.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                slideOut.play();
            }
        });
        root.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                slideIn.play();
            }
        });
        final VBox vbox = new VBox();
        final Slider slider = new Slider();
        vbox.getChildren().add(slider);


        final HBox hbox = new HBox();
        final int bands = player.getAudioSpectrumNumBands();
        final Rectangle [] rects = new Rectangle[bands];
        for (int i =0; i < rects.length; i++){
            rects[i] = new Rectangle();
            rects[i].setFill(Color.NAVY);
            hbox.getChildren().add(rects[i]);
        }
        vbox.getChildren().add(hbox);
        root.getChildren().add(view);
        root.getChildren().add(vbox);



        Scene scene = new Scene(root, 600, 400, Color.AQUA);
        stage.setScene(scene);
        stage.show();
        player.play();
        player.setOnReady(new Runnable() {
            @Override
            public void run() {
                int w = player.getMedia().getWidth();
                int h = player.getMedia().getHeight();

                hbox.setMinWidth(w);
                int bandWidth = w / rects.length;
                for (Rectangle r : rects) {
                    r.setWidth(bandWidth);
                    r.setHeight(2);
                }

                stage.setMinWidth(w);
                stage.setMinHeight(w);

                vbox.setMinSize(w, 100);
                vbox.setTranslateY(h - 100);

                slider.setMin(0.0);
                slider.setValue(0.0);
                slider.setMax(player.getTotalDuration().toSeconds());

                slideOut.getKeyFrames().addAll(
                        new KeyFrame(new Duration(0),
                                new KeyValue(vbox.translateYProperty(), h - 100),
                                new KeyValue(vbox.opacityProperty(), 0.9)
                        ),
                        new KeyFrame(new Duration(300),
                                new KeyValue(vbox.translateYProperty(), h),
                                new KeyValue(vbox.opacityProperty(), 0.0)

                        )
                );

                slideIn.getKeyFrames().addAll(
                        new KeyFrame(new Duration(0),
                                new KeyValue(vbox.translateYProperty(), h),
                                new KeyValue(vbox.opacityProperty(), 0.0)
                        ),
                        new KeyFrame(new Duration(300),
                                new KeyValue(vbox.translateYProperty(), h - 100),
                                new KeyValue(vbox.opacityProperty(), 0.9)

                        )
                );
            }
        });
        player.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration duration, Duration current) {
                slider.setValue(current.toSeconds());
            }
        });
        slider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (slider.isValueChanging()){
                    player.seek(Duration.seconds(slider.getValue()));
                }
            }
        });
        slider.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                player.seek(Duration.seconds(slider.getValue()));
            }
        });
            player.setAudioSpectrumListener(new AudioSpectrumListener() {
                @Override
                public void spectrumDataUpdate(double v, double v2, float[] mags, float[] floats2) {
                    for (int i = 0; i < rects.length; i++) {
                        double h = mags[i] + 60;
                        if (h > 2) {
                            rects[i].setHeight(h);
                        }
                    }
                }
            });
    }

    }
