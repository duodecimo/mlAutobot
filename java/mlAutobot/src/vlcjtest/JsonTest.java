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
package vlcjtest;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.stream.JsonParser;

/**
 *
 * @author duo
 */
public class JsonTest {

    URL url;
    boolean FOUND_DATA = false;
    boolean INDEX_READ = false;
    boolean AX_READ = false;
    boolean AY_READ = false;
    String index, ax, ay, az;
    InputStream inputStream;
    JsonParser jsonParser;
    public JsonTest() {
        try {
            reportData();
            Thread.sleep(1000);
            reportData();
            Thread.sleep(1000);
            reportData();
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(JsonTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void reportData() {
        try {
            url = new URL("http://192.168.0.4:8080/sensors.json");
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
                            System.out.println("index: " + index + ", " +
                                    "ax: " + ax + ", " +
                                    "ay: " + ay + ", " +
                                    "az: " + az + "\n");
                        }
                        //System.out.println(event.toString() + " " +
                                //parser.getString());
                        break;
                }
            }
            jsonParser.close();
            inputStream.close();
        } catch (MalformedURLException ex) {
            Logger.getLogger(JsonTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JsonTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) throws MalformedURLException {
        new JsonTest();
    }
}
