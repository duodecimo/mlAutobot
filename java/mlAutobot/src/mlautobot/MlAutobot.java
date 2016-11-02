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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.math.linear.RealMatrix;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;
import uk.co.caprica.vlcj.runtime.streams.NativeStreams;

/**
 *
 * @author duo
 * @see https://duodecimo.github.io/mlAutobot/
 *
 */
public class MlAutobot extends JFrame implements KeyListener, MouseMotionListener {

    private final String MRL = "http://192.168.0.12:8080";
    public static final int FRAMEWIDTH = 32;
    public static final int FRAMEHEIGHT = 24;
    private DirectMediaPlayerComponent mediaPlayerComponent;
    private int frameCounter;

    private final JPanel mainPanel;
    private final JLabel mrlLabel;
    private final JTextField mrlTextFiled;
    private final JButton mrlButton;
    private final JPanel videoSurface;
    private final BufferedImage image;

    enum Mode {
        MANUAL, MANUAL_RECORD, AUTOMATIC
    };
    private Mode driveMode = Mode.MANUAL;
    private boolean left = false;
    private boolean right;
    private boolean forward;
    private boolean reverse;
    private long leftLastChangeMs = 1000;
    private long rightLastChangeMs = 1000;
    private long forwardLastChangeMs = 1000;
    private long reverseLastChangeMs = 1000;
    private PrintStream featuresOut;
    private FeatureWriter featureWriter;

    private NeuralNetwork nn;
    private Point prevMousePoint;
    private Predictor predictor;

    public MlAutobot(File featureOutFile, String theta1File,
            String theta2File, JPanel mainPanel) {
        this.mainPanel = mainPanel;
        try {
            RealMatrix theta1 = NeuralNetwork
                    .loadMatrixFromOctaveDatFile("data/theta1.dat");
            RealMatrix theta2 = NeuralNetwork
                    .loadMatrixFromOctaveDatFile("data/theta2.dat");
            nn = new NeuralNetwork(theta1, theta2);

            this.featuresOut = new PrintStream(featureOutFile);
        } catch (FileNotFoundException e) {
        }


        mediaPlayerComponent = null;

        mrlLabel = new JLabel("MRL:");
        mrlTextFiled = new JTextField(MRL);
        mrlButton = new JButton("Start");
        videoSurface = new VideoSurfacePanel();
        videoSurface.setBackground(Color.black);
        videoSurface.setOpaque(true);
        videoSurface.setPreferredSize(new Dimension(FRAMEWIDTH, FRAMEHEIGHT));
        videoSurface.setMinimumSize(new Dimension(FRAMEWIDTH, FRAMEHEIGHT));
        videoSurface.setMaximumSize(new Dimension(FRAMEWIDTH, FRAMEHEIGHT));
        mainPanel.add(videoSurface, BorderLayout.CENTER);
        image = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration()
                .createCompatibleImage(FRAMEWIDTH, FRAMEHEIGHT);

        //this redirects error outputs ...
        NativeStreams nativeStreams = new NativeStreams("/dev/stdout", "/dev/null");
        BufferFormatCallback bufferFormatCallback = (int sourceWidth, int sourceHeight) -> new RV32BufferFormat(FRAMEWIDTH, FRAMEHEIGHT);
        mediaPlayerComponent = new DirectMediaPlayerComponent(bufferFormatCallback) {
            @Override
            protected RenderCallback onGetRenderCallback() {
                return new MlAutobotRenderCallbackAdapter();
            }
        };
        JPanel mrlPanel = new JPanel(new BorderLayout());
        mrlPanel.add(mrlLabel, BorderLayout.WEST);
        mrlPanel.add(mrlButton, BorderLayout.EAST);
        mrlPanel.add(mrlTextFiled, BorderLayout.CENTER);
        mainPanel.add(mrlPanel, BorderLayout.SOUTH);

        mrlButton.addActionListener((ActionEvent e) -> {
            mediaPlayerComponent.getMediaPlayer().playMedia(mrlTextFiled.getText() + "/video");
        });
    }

    private class VideoSurfacePanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage(image, null, 0, 0);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        //System.out.println("keyPressed " + keyCode + " " + e.getWhen());
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                left = true;
                leftLastChangeMs = System.currentTimeMillis();
                break;
            case KeyEvent.VK_RIGHT:
                right = true;
                rightLastChangeMs = System.currentTimeMillis();
                break;
            case KeyEvent.VK_UP:
                forward = true;
                forwardLastChangeMs = System.currentTimeMillis();
                break;
            case KeyEvent.VK_DOWN:
                reverse = true;
                reverseLastChangeMs = System.currentTimeMillis();
                break;
            default:
                break;
        }

        e.consume();
        //sendControls(left, right, forward, reverse);
        //view.update(left, right, forward, reverse, driveMode);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        //System.out.println("keyReleased " + keyCode);
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                left = false;
                leftLastChangeMs = System.currentTimeMillis();
                break;
        //sendControls(left, right, forward, reverse);
        //view.update(left, right, forward, reverse, driveMode);
            case KeyEvent.VK_RIGHT:
                right = false;
                rightLastChangeMs = System.currentTimeMillis();
                break;
            case KeyEvent.VK_UP:
                forward = false;
                forwardLastChangeMs = System.currentTimeMillis();
                break;
            case KeyEvent.VK_DOWN:
                reverse = false;
                reverseLastChangeMs = System.currentTimeMillis();
                break;
            case KeyEvent.VK_M:
                driveMode = Mode.MANUAL;
                break;
            case KeyEvent.VK_R:
                driveMode = driveMode == Mode.MANUAL ? Mode.MANUAL_RECORD
                        : Mode.MANUAL;
                forward = driveMode == Mode.MANUAL_RECORD;
                forwardLastChangeMs = System.currentTimeMillis();
                break;
            case KeyEvent.VK_A:
                driveMode = Mode.AUTOMATIC;
                break;
            case KeyEvent.VK_F:
                forward = !forward;
                forwardLastChangeMs = System.currentTimeMillis();
                break;
            default:
                break;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (prevMousePoint != null) {
            double diff = e.getPoint().getX() - prevMousePoint.getX();
            if (driveMode != Mode.AUTOMATIC) {
                if (diff > 0) {
                    if (left) {
                        left = false;
                        leftLastChangeMs = System.currentTimeMillis();
                    } else if (System.currentTimeMillis() - leftLastChangeMs > 250) {
                        right = true;
                        rightLastChangeMs = System.currentTimeMillis();
                    }
                }
                if (diff < 0) {
                    if (right) {
                        right = false;
                        rightLastChangeMs = System.currentTimeMillis();
                    } else if (System.currentTimeMillis() - rightLastChangeMs > 250) {
                        left = true;
                        leftLastChangeMs = System.currentTimeMillis();
                    }
                }
            }
            //sendControls(left, right, forward, reverse);
            //view.update(left, right, forward, reverse, driveMode);
        }
        prevMousePoint = e.getPoint();
    }

    private class MlAutobotRenderCallbackAdapter extends RenderCallbackAdapter {

        private MlAutobotRenderCallbackAdapter() {
            super(new int[FRAMEWIDTH * FRAMEHEIGHT]);
        }

        @Override
        protected void onDisplay(DirectMediaPlayer mediaPlayer, int[] rgbBuffer) {
            frameCounter++;
            if (frameCounter > 30) {
                // Simply copy buffer to the image and repaint
                image.setRGB(0, 0, FRAMEWIDTH, FRAMEHEIGHT, rgbBuffer, 0, FRAMEWIDTH);
                videoSurface.repaint();
                //process the image
                frameCounter = 0;
                System.out.println(">>> Image size : " + rgbBuffer.length);
                System.out.println(">>> Image bytes: " + (rgbBuffer.length / (FRAMEWIDTH * FRAMEHEIGHT)));
            }
        }
    }

    public static void main(final String[] args) throws IOException {
        new NativeDiscovery().discover();
        File out = File.createTempFile("nnrccar", "features");
        System.out.println("Features writing to: "
                + out.getAbsoluteFile().toString());
        JPanel mainPanel = new JPanel(new BorderLayout(), true);
        MlAutobot mlAutobot = new MlAutobot(out,
                "data/theta1.dat", "data/theta2.dat", mainPanel);
        mlAutobot.setTitle("ML Autobot Captured Video");
        mlAutobot.setBounds(100, 100, 600, 500);
        mlAutobot.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mlAutobot.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        mlAutobot.setContentPane(mainPanel);
        mlAutobot.addKeyListener(mlAutobot);
        mlAutobot.addMouseMotionListener(mlAutobot);
        mlAutobot.setVisible(true);

    }
}
