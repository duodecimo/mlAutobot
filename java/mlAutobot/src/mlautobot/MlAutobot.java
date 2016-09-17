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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import mlautobot.interfaces.BufferedImageCaptureInterface;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
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
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

/**
 *
 * @author duo
 */
public class MlAutobot implements BufferedImageCaptureInterface {

    //private final String MRL = "http://192.168.0.4:8080/video";
    private final JFrame frame;
    public static final int WIDTH = 600;
    public static final int HEIGHT = 400;
    private final JPanel videoSurface;
    private final BufferedImage image;
    private final DirectMediaPlayerComponent mediaPlayerComponent;
    //private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private final JLabel mrlLabel;
    private final JLabel accelLabel;
    private final JTextField mrlTextField;
    private final JButton startButton;
    private final JButton pauseButton;
    //private final JButton statsButton;
    private URL url;
    boolean FOUND_DATA = false;
    boolean INDEX_READ = false;
    boolean AX_READ = false;
    boolean AY_READ = false;
    String index, ax, ay, az;
    InputStream inputStream;
    JsonParser jsonParser;
    AccelerometerData accelerometerData;

    public MlAutobot(String[] args) {
        frame = new JFrame("ML Autobot Monitor");
        frame.setBounds(100, 100, WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mediaPlayerComponent.release();
                System.exit(0);
            }
        });
        
        videoSurface = new VideoSurfacePanel();
        videoSurface.setLayout(new BorderLayout());

        BufferFormatCallback bufferFormatCallback = (int sourceWidth, int sourceHeight) -> new RV32BufferFormat(WIDTH, HEIGHT);

        mediaPlayerComponent = new DirectMediaPlayerComponent(bufferFormatCallback) {
            @Override
            protected RenderCallback onGetRenderCallback() {
                return new MlAutobotRenderCallbackAdapter();
            }
        };

        //mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        
        image = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDefaultConfiguration()
            .createCompatibleImage(WIDTH, HEIGHT);
        
        mrlLabel = new JLabel("MRL");
        mrlTextField = new JTextField("http://192.168.0.4:8080", 50);
        JPanel mrlPane = new JPanel();
        mrlPane.setLayout(new BorderLayout(10, 20));
        mrlPane.add(mrlLabel, BorderLayout.WEST);
        mrlPane.add(mrlTextField, BorderLayout.CENTER);
        accelLabel = new JLabel();
        mrlPane.add(accelLabel, BorderLayout.SOUTH);

        JPanel controlsPane = new JPanel();
        startButton = new JButton("Start");
        controlsPane.add(startButton);
        pauseButton = new JButton("Pause");
        controlsPane.add(pauseButton);
        //statsButton = new JButton("Stats");
        //controlsPane.add(statsButton);
        
        videoSurface.add(mrlPane, BorderLayout.NORTH);
        videoSurface.add(controlsPane, BorderLayout.SOUTH);

        startButton.addActionListener((ActionEvent e) -> {
            if(!mrlTextField.getText().isEmpty()) {
                mediaPlayerComponent.getMediaPlayer().playMedia(mrlTextField.getText() + "/video");            
            }
        });
        
        pauseButton.addActionListener((ActionEvent e) -> {
            mediaPlayerComponent.getMediaPlayer().pause();
        });

        /*        statsButton.addActionListener((ActionEvent e) -> {
        image = convertToGrayScale(mediaPlayerComponent.getMediaPlayer().getSnapshot(176, 144));
        show("amostra", image, 1);
        //capture the pixels of the 176x144 black and white
        //byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        accelerometerData = getAccelerometerData();
        SwingUtilities.invokeLater(() -> {
        JOptionPane.showMessageDialog(
        frame,
        "ax: " + accelerometerData.getAx() + "\n" +
        "ay:  " + accelerometerData.getAy()  + "\n" +
        "az: " + accelerometerData.getAz()
        ,
        "Accelerometer data",
        JOptionPane.INFORMATION_MESSAGE
        );
        });
        });*/

        mediaPlayerComponent.getMediaPlayer().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void playing(MediaPlayer mediaPlayer) {
                SwingUtilities.invokeLater(() -> {
                    frame.setTitle(String.format(
                            "Showing %s",
                            mediaPlayerComponent.getMediaPlayer().getMediaMeta().getTitle()
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

        frame.setContentPane(videoSurface);
        frame.setVisible(true);

    }

    private void closeWindow() {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    @SuppressWarnings("serial")
    private static void show(String title, final BufferedImage img, int i) {
        JFrame f = new JFrame(title);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setContentPane(new JPanel() {
            @Override
            protected void paintChildren(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                g2.drawImage(img, null, 0, 0);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(img.getWidth(), img.getHeight());
            }
        });
        f.pack();
        f.setLocation(50 + (i * 50), 50 + (i * 50));
        f.setVisible(true);
    }

    @Override
    public BufferedImage captureImage() {
        BufferedImage bufferedImage = mediaPlayerComponent.getMediaPlayer().getSnapshot(176, 144);
        return convertToGrayScale(bufferedImage);
    }

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

    public AccelerometerData getAccelerometerData() {
        accelerometerData = new AccelerometerData();
        try {
            url = new URL(mrlTextField.getText() + "/sensors.json");
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
            g2.drawImage(image, null, 0, 0);
        }
    }

    private class MlAutobotRenderCallbackAdapter extends RenderCallbackAdapter {

        private MlAutobotRenderCallbackAdapter() {
            super(new int[WIDTH * HEIGHT]);
        }

        @Override
        protected void onDisplay(DirectMediaPlayer mediaPlayer, int[] rgbBuffer) {
            // Simply copy buffer to the image and repaint
            image.setRGB(0, 0, WIDTH, HEIGHT, rgbBuffer, 0, WIDTH);
            accelerometerData = getAccelerometerData();
            accelLabel.setText("ax: " + accelerometerData.getAx() + "\n"
                    + "ay:  " + accelerometerData.getAy() + "\n"
                    + "az: " + accelerometerData.getAz());
            videoSurface.repaint();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        boolean discover = new NativeDiscovery().discover();
        SwingUtilities.invokeLater(() -> {
            MlAutobot mlAutobot = new MlAutobot(args);
        });
    }
}
