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

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 *
 * @author duo
 */
public class AccelerometerData {
    private BigInteger index;
    private BigDecimal ax;
    private BigDecimal ay;
    private BigDecimal az;

    public BigInteger getIndex() {
        return index;
    }

    public void setIndex(BigInteger index) {
        this.index = index;
    }

    public BigDecimal getAx() {
        return ax;
    }

    public void setAx(BigDecimal ax) {
        this.ax = ax;
    }

    public BigDecimal getAy() {
        return ay;
    }

    public void setAy(BigDecimal ay) {
        this.ay = ay;
    }

    public BigDecimal getAz() {
        return az;
    }

    public void setAz(BigDecimal az) {
        this.az = az;
    }
}
