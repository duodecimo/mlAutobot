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
package mlautobot.format;

import uk.co.caprica.vlcj.player.direct.BufferFormat;

/**
 *
 * @author duo
 * Implementation of a buffer format for GREY.
 * <p>
 * RV32 is a 24-bit BGR format with 8-bit of padding (no alpha) in a single plane.
 */
public class GREYBufferFormat extends BufferFormat {

    /**
     * Creates a RV32 buffer format with the given width and height.
     *
     * @param width width of the buffer
     * @param height height of the buffer
     */
    public GREYBufferFormat(int width, int height) {
        super("GREY", width, height, new int[] {width * 1}, new int[] {height});
    }
}
