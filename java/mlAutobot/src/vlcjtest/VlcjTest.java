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
 *
 *
 * Install and Run
 *
 * Intall IP Web Pro from playStore in your Android mobile.
 * Run it with default configurations, with blank user and password.
 * Check the MRL on the capture screen in Android.
 * Write the MRL with /video added to String MRL, int the beginning of the class.
 * i.e.: private final String MRL = "http://192.168.0.4:8080/video",
 * or  : private final String MRL = "/home/user/Vídeos/guilmore/confortablyNumb.mp4".
 */
package vlcjtest;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

/**
 *
 * @author duo
 */
public class VlcjTest {

    private final String MRL = "http://192.168.0.9:8080/video";
    //private final String MRL = "/home/duo/Vídeos/guilmore/confortablyNumb.mp4";
    private final JFrame frame;
    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private final JButton pauseButton;
    private final JButton rewindButton;
    private final JButton skipButton;
    private final JButton captureButton;
    private BufferedImage image;

    public VlcjTest(String[] args) {
        frame = new JFrame("Duo: My First Media Player");
        frame.setBounds(100, 100, 600, 400);
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mediaPlayerComponent.release();
                System.exit(0);
            }
        });
        
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        contentPane.add(mediaPlayerComponent, BorderLayout.CENTER);

        JPanel controlsPane = new JPanel();
        
        pauseButton = new JButton("Pause");
        controlsPane.add(pauseButton);
        rewindButton = new JButton("Rewind");
        controlsPane.add(rewindButton);
        skipButton = new JButton("Skip");
        controlsPane.add(skipButton);
        captureButton = new JButton("Capture");
        controlsPane.add(captureButton);
        
        
        contentPane.add(controlsPane, BorderLayout.SOUTH);
        
        pauseButton.addActionListener((ActionEvent e) -> {
            mediaPlayerComponent.getMediaPlayer().pause();
        });

        rewindButton.addActionListener((ActionEvent e) -> {
            mediaPlayerComponent.getMediaPlayer().skip(-10000);
        });

        skipButton.addActionListener((ActionEvent e) -> {
            mediaPlayerComponent.getMediaPlayer().skip(10000);
        });

        captureButton.addActionListener((ActionEvent e) -> {
            File file = new File("/home/duo/vlcjTest.png");
            mediaPlayerComponent.getMediaPlayer().saveSnapshot(file);
            image = mediaPlayerComponent.getMediaPlayer().getSnapshot();
            show("Image getSnapshot", image, 5);
        });

        mediaPlayerComponent.getMediaPlayer().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void playing(MediaPlayer mediaPlayer) {
                SwingUtilities.invokeLater(() -> {
                    frame.setTitle(String.format(
                            "Duo: My First Media Player - %s",
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
                    closeWindow();
                });
            }
        });

        frame.setContentPane(contentPane);
        frame.setVisible(true);

        mediaPlayerComponent.getMediaPlayer().playMedia(MRL);
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

    public BufferedImage capture() {
        BufferedImage bufferedImage = mediaPlayerComponent.getMediaPlayer().getSnapshot();
        return bufferedImage;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new NativeDiscovery().discover();
        SwingUtilities.invokeLater(() -> {
            new VlcjTest(args);
        });
    }
    
}
