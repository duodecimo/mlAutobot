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
import com.sun.jna.Memory;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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
import javax.swing.SwingUtilities;
import mlautobot.format.GREYBufferFormat;
import mlautobot.interfaces.AccelerometerDataCaptureInterface;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.runtime.streams.NativeStreams;

/**
 *
 * @author duo
 * @see https://duodecimo.github.io/mlAutobot/
 *
 */
public class MlAutobot implements AccelerometerDataCaptureInterface {

    private final String MRL = "http://192.168.0.3:8080";
    public static final int WIDTH = 144;
    public static final int HEIGHT = 176;
    private final BufferedImage image;
    private final DirectMediaPlayer directMediaPlayer;
    private URL url;
    private boolean FOUND_DATA = false;
    private boolean INDEX_READ = false;
    private boolean AX_READ = false;
    private boolean AY_READ = false;
    private String index, ax, ay, az;
    private InputStream inputStream;
    private JsonParser jsonParser;
    private AccelerometerData accelerometerData;
    private final float[] gravity;
    private final float[] linearAcceleration;
    private final float G = 0.8f;
    private final FeatureCallback featureCallback;
    private int frameCounter;

    public MlAutobot(FeatureCallback featureCallback) {
        this.featureCallback = featureCallback;
        gravity = new float[3];
        linearAcceleration = new float[3];
        //this redirects error outputs ...
        NativeStreams nativeStreams = new NativeStreams("/dev/stdout", "/dev/null");
        BufferFormatCallback bufferFormatCallback = (int sourceWidth, int sourceHeight)
                -> new GREYBufferFormat(WIDTH, HEIGHT);

        MediaPlayerFactory mediaPlayerFactory
                = new MediaPlayerFactory("--no-video-title-show", "--grayscale");
        directMediaPlayer = mediaPlayerFactory.newDirectMediaPlayer(bufferFormatCallback,
                new MlAutobotRenderCallbackAdapter());

        image = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration()
                .createCompatibleImage(WIDTH, HEIGHT);

        directMediaPlayer.playMedia(MRL + "/video");
    }

    public byte[] toByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(baos);
        encoder.encode(image);
        baos.flush();
        byte[] result = baos.toByteArray();
        /*System.out.println("image (size: " + image.getHeight() + ", " +
                image.getWidth() + ") converted to byte array size: " + result.length
        + " (/ 176 = " + result.length/176 + ")");
         */
        return result;
    }

    /**
     * This method uses jsonp library from oracle. one needs both the compiling
     * and run time libraries, just download the jar's files and add them to
     * your project libraries. see
     * http://search.maven.org/remotecontent?filepath=javax/json/javax.json-api/1.0/javax.json-api-1.0.jar
     * and
     * http://search.maven.org/remotecontent?filepath=org/glassfish/javax.json/1.0.4/javax.json-1.0.4.jar
     *
     * @return AccelerometerData that has 3 fields: index (BigInteger), ax, ay,
     * az (BigDecimal).
     */
    @Override
    public AccelerometerData getAccelerometerData() {
        accelerometerData = new AccelerometerData();
        try {
            url = new URL(MRL + "/sensors.json");
            inputStream = url.openStream();
            jsonParser = Json.createParser(inputStream);
            while (jsonParser.hasNext()) {
                JsonParser.Event event = jsonParser.next();
                switch (event) {
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
                        if (jsonParser.getString().equals("data")) {
                            FOUND_DATA = true; // data Array found, need to read index
                        }
                        //System.out.print(event.toString() + " " +
                        //        jsonParser.getString() + " - ");
                        break;
                    case VALUE_STRING:
                    case VALUE_NUMBER:
                        if (FOUND_DATA && !INDEX_READ) {
                            index = jsonParser.getString();
                            INDEX_READ = true;
                        } else if (FOUND_DATA && INDEX_READ && !AX_READ) {
                            ax = jsonParser.getString();
                            AX_READ = true;
                        } else if (FOUND_DATA && INDEX_READ && AX_READ && !AY_READ) {
                            ay = jsonParser.getString();
                            AY_READ = true;
                        } else if (FOUND_DATA && INDEX_READ && AX_READ && AY_READ) {
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

    private class MlAutobotRenderCallbackAdapter implements RenderCallback {

        @Override
        public void display(DirectMediaPlayer mediaPlayer, Memory[] nativeBuffer, BufferFormat bufferFormat) {
            onDisplay(mediaPlayer, nativeBuffer[0].getByteArray(0L, (int) nativeBuffer[0].size()));
            System.out.println("Buffer Format height: " + bufferFormat.getHeight() +
                    " width: " + bufferFormat.getWidth() + 
                    " Native Buffer length: " + nativeBuffer[0].size());
            /*176x144=25344 Im getting 25376, 32 bytes exceeding*/
        }

        protected void onDisplay(DirectMediaPlayer mediaPlayer, byte[] imageBuffer) {
            SwingUtilities.invokeLater(() -> {
                frameCounter++;
                if(frameCounter>30) {
                    frameCounter = 0;
                    accelerometerData = getAccelerometerData();

                    gravity[0] = G * gravity[0] + (1 - G) * accelerometerData.getAx().floatValue();
                    gravity[1] = G * gravity[1] + (1 - G) * accelerometerData.getAy().floatValue();
                    gravity[2] = G * gravity[2] + (1 - G) * accelerometerData.getAz().floatValue();

                    linearAcceleration[0] = accelerometerData.getAx().floatValue() - gravity[0];
                    linearAcceleration[1] = accelerometerData.getAx().floatValue() - gravity[1];
                    linearAcceleration[2] = accelerometerData.getAx().floatValue() - gravity[2];
                    //System.out.println("Sending image bytes, size: " + imageBuffer.length +
                    //        "(should be: " + (176*144) + ")\n with buffer format: " +
                    //        new GREYBufferFormat(176,144).toString());
                    featureCallback.features(imageBuffer, WIDTH, HEIGHT, linearAcceleration);
                }
            });
        }
    }
}
