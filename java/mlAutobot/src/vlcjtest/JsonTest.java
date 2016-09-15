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
import javax.json.JsonReader;
import javax.json.stream.JsonParser;

/**
 *
 * @author duo
 */
public class JsonTest {

    URL url;
    public JsonTest() {
        try {
            url = new URL("http://192.168.0.4:8080/sensors.json");
            InputStream is = url.openStream();
            JsonParser parser = Json.createParser(is);
            while (parser.hasNext()) {
                JsonParser.Event event = parser.next();
                switch(event) {
                    case START_ARRAY:
                    case END_ARRAY:
                    case START_OBJECT:
                    case END_OBJECT:
                    case VALUE_FALSE:
                    case VALUE_NULL:
                    case VALUE_TRUE:
                        System.out.println(event.toString());
                        break;
                    case KEY_NAME:
                        System.out.print(event.toString() + " " +
                                parser.getString() + " - ");
                        break;
                    case VALUE_STRING:
                    case VALUE_NUMBER:
                        System.out.println(event.toString() + " " +
                                parser.getString());
                        break;
                }
            }       } catch (MalformedURLException ex) {
            Logger.getLogger(JsonTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JsonTest.class.getName()).log(Level.SEVERE, null, ex);
        }
 }

    public static void main(String[] args) throws MalformedURLException {
        new JsonTest();
    }
}
