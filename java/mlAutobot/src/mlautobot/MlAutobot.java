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
        mrlTextField = new JTextField("", 50);
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
            image = mediaPlayerComponent.getMediaPlayer().getSnapshot();
            byte[] pixels = new byte[image.getHeight() * image.getWidth()];
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    pixels[x*y + y] = (byte) (image.getRGB(x, y) == 0xFFFFFFFF ? 0 : 1);
                }
            }
            String sample = "";
            for(int x=0; x<10; x++) {
                sample += pixels[x] + ":";
            }
            final String fsample = sample;
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                    frame,
                        "height: " + image.getHeight() + "\n" +
                        "width:  " + image.getWidth()  + "\n" +
                        "bytes:  " + pixels.length     + "\n" +
                        "sample: " + fsample
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
        BufferedImage bufferedImage = mediaPlayerComponent.getMediaPlayer().getSnapshot();
        /* bufferedImage.getRGB(0, 0, 0, 0, rgbArray, 0, 0)
                public int[] getRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize)
                Returns an array of integer pixels in the default RGB color model (TYPE_INT_ARGB) and default sRGB color space, from a portion of the image data. Color conversion takes place if the default model does not match the image ColorModel. There are only 8-bits of precision for each color component in the returned data when using this method. With a specified coordinate (x, y) in the image, the ARGB pixel can be accessed in this way:
                pixel   = rgbArray[offset + (y-startY)*scansize + (x-startX)]; 
                An ArrayOutOfBoundsException may be thrown if the region is not in bounds. However, explicit bounds checking is not guaranteed.
                Parâmetros:
                    startX - the starting X coordinate startY - the starting Y coordinate w - width of region h - height of region rgbArray - if not null, the rgb pixels are written here offset - offset into the rgbArray scansize - scanline stride for the rgbArray 
                Retorna:
                    array of RGB pixels. 
                Veja Também:
                    BufferedImage.setRGB(int, int, int), BufferedImage.setRGB(int, int, int, int, int[], int, int)
        */
        return bufferedImage;
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
