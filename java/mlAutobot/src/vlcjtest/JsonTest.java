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
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 *
 * @author duo
 */
public class JsonTest {

    URL url;

    public JsonTest() throws MalformedURLException {
        url = new URL("http://192.168.0.4:8080/sensors.json");
        try (InputStream is = url.openStream();
                JsonReader rdr = Json.createReader(is)) {

            JsonObject obj = rdr.readObject();
            JsonArray results = obj.getJsonArray("data");
            results.getValuesAs(JsonObject.class).stream().map((result) -> {
                System.out.print(result.getJsonObject("from").getString("name"));
                return result;
            }).map((result) -> {
                System.out.print(": ");
                return result;
            }).map((result) -> {
                System.out.println(result.getString("message", ""));
                return result;
            }).forEach((_item) -> {
                System.out.println("-----------");
            });
        } catch (IOException ex) {
            Logger.getLogger(JsonTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
