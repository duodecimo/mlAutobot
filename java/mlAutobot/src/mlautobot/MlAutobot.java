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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import mlautobot.interfaces.BufferedImageCaptureInterface;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

/**
 *
 * @author duo
 */
public class MlAutobot implements BufferedImageCaptureInterface {

    //private final String MRL = "http://192.168.0.4:8080/video";
    private final String MRL = "/home/duo/Vídeos/guilmore/confortablyNumb.mp4";
    private final JFrame frame;
    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private final JLabel mrlLabel;
    private final JTextField mrlTextField;
    private final JButton startButton;
    private final JButton pauseButton;
    private final JButton statsButton;
    private BufferedImage image;

    public MlAutobot(String[] args) {
        frame = new JFrame("ML Autobot Monitor");
        frame.setBounds(100, 100, 600, 400);
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
        
        mrlLabel = new JLabel("MRL");
        mrlTextField = new JTextField("http://192.168.0.9:8080/video", 50);
        JPanel mrlPane = new JPanel();
        mrlPane.setLayout(new BorderLayout(10, 20));
        mrlPane.add(mrlLabel, BorderLayout.WEST);
        mrlPane.add(mrlTextField, BorderLayout.CENTER);

        JPanel controlsPane = new JPanel();
        startButton = new JButton("Start");
        controlsPane.add(startButton);
        pauseButton = new JButton("Pause");
        controlsPane.add(pauseButton);
        statsButton = new JButton("Stats");
        controlsPane.add(statsButton);
        
        contentPane.add(mrlPane, BorderLayout.NORTH);
        contentPane.add(controlsPane, BorderLayout.SOUTH);

        startButton.addActionListener((ActionEvent e) -> {
            if(!mrlTextField.getText().isEmpty()) {
                mediaPlayerComponent.getMediaPlayer().playMedia(mrlTextField.getText());            
            }
        });
        
        pauseButton.addActionListener((ActionEvent e) -> {
            mediaPlayerComponent.getMediaPlayer().pause();
        });

        statsButton.addActionListener((ActionEvent e) -> {
            image = convertToGrayScale(mediaPlayerComponent.getMediaPlayer().getSnapshot(176, 144));
            show("amostra", image, 1);
            //capture the pixels of the 176x144 black and white
            byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                    frame,
                        "height: " + image.getHeight() + "\n" +
                        "width:  " + image.getWidth()  + "\n" +
                        "pixels: " + pixels.length
                    ,
                   "Image stats",
                    JOptionPane.INFORMATION_MESSAGE
                );
            });
        });

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

        frame.setContentPane(contentPane);
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
    public BufferedImage capture() {
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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new NativeDiscovery().discover();
        SwingUtilities.invokeLater(() -> {
            MlAutobot mlAutobot = new MlAutobot(args);
        });
    }

}
