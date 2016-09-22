/*
 * Copyright (C) 2016 duo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mlautobot;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.swing.JPanel;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.stream.JsonParser;
import mlautobot.interfaces.AccelerometerDataCaptureInterface;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

/**
 *
 * @author duo
 * @see https://duodecimo.github.io/mlAutobot/
 * 
 */
public class MlAutobot implements AccelerometerDataCaptureInterface {

    private final String MRL = "http://192.168.0.4:8080";
    //private final JFrame frame;
    //public static final int WIDTH = 600;
    //public static final int HEIGHT = 400;
    public static final int WIDTH = 600;
    public static final int HEIGHT = 400;
    //private final JPanel videoSurface;
    private final BufferedImage image;
    //private final DirectMediaPlayerComponent mediaPlayerComponent;
    private final DirectMediaPlayer directMediaPlayer;
    //private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
    //private final JLabel mrlLabel;
    //private final JLabel accelLabel;
    //private final JTextField mrlTextField;
    //private final JButton startButton;
    //private final JButton pauseButton;
    //private final JButton statsButton;
    private URL url;
    private boolean FOUND_DATA = false;
    private boolean INDEX_READ = false;
    private boolean AX_READ = false;
    private boolean AY_READ = false;
    private String index, ax, ay, az;
    private InputStream inputStream;
    private JsonParser jsonParser;
    private AccelerometerData accelerometerData;
    private float[] gravity = new float[3];
    private float[] linearAcceleration = new float[3];
    private final float G = 0.8f;
    private final FeatureCallback featureCallback;
    private int frameCounter;

    public MlAutobot(FeatureCallback featureCallback) {
        this.featureCallback = featureCallback;
        //frame = new JFrame("ML Autobot Monitor");
        //frame.setBounds(100, 100, WIDTH, HEIGHT);
        //frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        /*frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //mediaPlayerComponent.release();
                directMediaPlayer.release();
                System.exit(0);
            }
        });*/
        
        //videoSurface = new VideoSurfacePanel();
        //videoSurface.setLayout(new BorderLayout());

        BufferFormatCallback bufferFormatCallback = (int sourceWidth, int sourceHeight) -> new RV32BufferFormat(WIDTH, HEIGHT);

        MediaPlayerFactory mediaPlayerFactory = 
                new MediaPlayerFactory("--no-video-title-show", "--grayscale");
        directMediaPlayer = mediaPlayerFactory.newDirectMediaPlayer(bufferFormatCallback, 
                new MlAutobotRenderCallbackAdapter());
/*        mediaPlayerComponent =
                new DirectMediaPlayerComponent(bufferFormatCallback) {
            @Override
            protected String[] onGetMediaPlayerFactoryArgs() {
                return new String[] {"--no-video-title-show"};
            }
            @Override
            protected RenderCallback onGetRenderCallback() {
                return new MlAutobotRenderCallbackAdapter();
            }
        };
  */      
        //mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        
        image = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDefaultConfiguration()
            .createCompatibleImage(WIDTH, HEIGHT);
        
        //mrlLabel = new JLabel("MRL");
        //mrlTextField = new JTextField("http://192.168.0.8:8080", 50);
        //JPanel mrlPane = new JPanel();
        //mrlPane.setLayout(new BorderLayout(10, 20));
        //mrlPane.add(mrlLabel, BorderLayout.WEST);
        //mrlPane.add(mrlTextField, BorderLayout.CENTER);
        //accelLabel = new JLabel();
        //mrlPane.add(accelLabel, BorderLayout.SOUTH);

        //JPanel controlsPane = new JPanel();
        //startButton = new JButton("Start");
        //controlsPane.add(startButton);
        //pauseButton = new JButton("Pause");
        //controlsPane.add(pauseButton);
        
        //videoSurface.add(mrlPane, BorderLayout.NORTH);
        //videoSurface.add(controlsPane, BorderLayout.SOUTH);

        /*startButton.addActionListener((ActionEvent e) -> {
            if(!mrlTextField.getText().isEmpty()) {
                //mediaPlayerComponent.getMediaPlayer().playMedia(mrlTextField.getText() + "/video");
                directMediaPlayer.playMedia(mrlTextField.getText() + "/video");
            }
        });
        
        pauseButton.addActionListener((ActionEvent e) -> {
            //mediaPlayerComponent.getMediaPlayer().pause();
            directMediaPlayer.pause();
        });
        */
        directMediaPlayer.playMedia(MRL + "/video");
        /*
        directMediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void playing(MediaPlayer mediaPlayer) {
                SwingUtilities.invokeLater(() -> {
                    frame.setTitle(String.format(
                            "Showing %s",
                            directMediaPlayer.getMediaMeta().getTitle()
                    ));
                });
            }

            @Override
            public void finished(MediaPlayer mediaPlayer) {
                SwingUtilities.invokeLater(() -> {
                    closeWindow();
                });
            }

            @Override
            public void error(MediaPlayer mediaPlayer) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                            frame,
                            "Failed to play media",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    // lets belive in second chances
                    //closeWindow();
                });
            }
        });
        */
        //frame.setContentPane(videoSurface);
        //frame.setVisible(true);

    }

    //private void closeWindow() {
    //    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    //}

    public static BufferedImage convertToGrayScale(BufferedImage image) {
        BufferedImage result = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = result.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return result;
    }

    public byte[] toByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(baos);
        encoder.encode(image);
        return baos.toByteArray();
    }

/**
     * This method uses jsonp library from oracle.
     * one needs both the compiling and run time libraries, just download
     * the jar's files and add them to your project libraries.
     * see http://search.maven.org/remotecontent?filepath=javax/json/javax.json-api/1.0/javax.json-api-1.0.jar
     * and http://search.maven.org/remotecontent?filepath=org/glassfish/javax.json/1.0.4/javax.json-1.0.4.jar
     * @return AccelerometerData that has 3 fields:
     * index (BigInteger), ax, ay, az (BigDecimal).
     */
    @Override
    public AccelerometerData getAccelerometerData() {
        accelerometerData = new AccelerometerData();
        try {
            //url = new URL(mrlTextField.getText() + "/sensors.json");
            url = new URL(MRL + "/sensors.json");
            inputStream = url.openStream();
            jsonParser = Json.createParser(inputStream);
            while (jsonParser.hasNext()) {
                JsonParser.Event event = jsonParser.next();
                switch(event) {
                    case START_ARRAY:
                    case END_ARRAY:
                    case START_OBJECT:
                    case END_OBJECT:
                    case VALUE_FALSE:
                    case VALUE_NULL:
                    case VALUE_TRUE:
                        //System.out.println(event.toString());
                        break;
                    case KEY_NAME:
                        if(jsonParser.getString().equals("data")) {
                            FOUND_DATA = true; // data Array found, need to read index
                        }
                        //System.out.print(event.toString() + " " +
                        //        jsonParser.getString() + " - ");
                        break;
                    case VALUE_STRING:
                    case VALUE_NUMBER:
                        if(FOUND_DATA && !INDEX_READ) {
                            index = jsonParser.getString();
                            INDEX_READ = true;
                        } else if(FOUND_DATA && INDEX_READ && !AX_READ) {
                            ax = jsonParser.getString();
                            AX_READ = true;
                        } else if(FOUND_DATA && INDEX_READ && AX_READ && !AY_READ) {
                            ay = jsonParser.getString();
                            AY_READ = true;
                        } else if(FOUND_DATA && INDEX_READ && AX_READ && AY_READ) {
                            az = jsonParser.getString();
                            AY_READ = false;
                            AX_READ = false;
                            INDEX_READ = false;
                            FOUND_DATA = false;
                            accelerometerData.setIndex(new BigInteger(index));
                            accelerometerData.setAx(new BigDecimal(ax));
                            accelerometerData.setAy(new BigDecimal(ay));
                            accelerometerData.setAz(new BigDecimal(az));
                            // no need to read further
                            jsonParser.close();
                            inputStream.close();
                            return accelerometerData;
                        }
                        //System.out.println(event.toString() + " " +
                                //parser.getString());
                        break;
                }
            }
            jsonParser.close();
            inputStream.close();
            return accelerometerData;
        } catch (MalformedURLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private class VideoSurfacePanel extends JPanel {
        private VideoSurfacePanel() {
            adjustVideoSurfacePanel();
        }

        private void adjustVideoSurfacePanel() {
            setBackground(Color.black);
            setOpaque(true);
            setPreferredSize(new Dimension(MlAutobot.WIDTH, MlAutobot.HEIGHT));
            setMinimumSize(new Dimension(MlAutobot.WIDTH, MlAutobot.HEIGHT));
            setMaximumSize(new Dimension(MlAutobot.WIDTH, MlAutobot.HEIGHT));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage(convertToGrayScale(image), null, 0, 0);
        }
    }

    private class MlAutobotRenderCallbackAdapter extends RenderCallbackAdapter {

        private MlAutobotRenderCallbackAdapter() {
            super(new int[WIDTH * HEIGHT]);
        }

        @Override
        protected void onDisplay(DirectMediaPlayer mediaPlayer, int[] rgbBuffer) {
            frameCounter++;
            //if(frameCounter>1) {
                //frameCounter = 0;
                // Simply copy buffer to the image and repaint
                image.setRGB(0, 0, WIDTH, HEIGHT, rgbBuffer, 0, WIDTH);
                accelerometerData = getAccelerometerData();

		gravity[0] = G * gravity[0] + (1 - G) * accelerometerData.getAx().floatValue();
		gravity[1] = G * gravity[1] + (1 - G) * accelerometerData.getAy().floatValue();
		gravity[2] = G * gravity[2] + (1 - G) * accelerometerData.getAz().floatValue();

		linearAcceleration[0] = accelerometerData.getAx().floatValue() - gravity[0];
		linearAcceleration[1] = accelerometerData.getAx().floatValue() - gravity[1];
		linearAcceleration[2] = accelerometerData.getAx().floatValue() - gravity[2];
                //videoSurface.repaint();

            try {
                featureCallback.features(toByteArray(image), WIDTH, HEIGHT, linearAcceleration);
            } catch (IOException ex) {
                Logger.getLogger(MlAutobot.class.getName()).log(Level.SEVERE, null, ex);
            }

                //accelLabel.setText(String.format("x: %f y: %f z: %f", linearAcceleration[0],
                //        linearAcceleration[0], linearAcceleration[0]));
            //}
        }
    }

    /**
     * @param args the command line arguments
     */
/*    public static void main(String[] args) {
        boolean discover = new NativeDiscovery().discover();
        SwingUtilities.invokeLater(() -> {
            MlAutobot mlAutobot = new MlAutobot(new Driver(featureOutFile, theta1File, theta2File));
        });
    }*/
}
