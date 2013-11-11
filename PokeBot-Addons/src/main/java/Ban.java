/*
 * Copyright (C) 2013 Lord_Ralex
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

import java.io.Serializable;

/**
 * @author Lord_Ralex
 */
public class Ban implements Serializable {

    private final long unbanTime;
    private final String unbanString;
    private final String channel;

    public Ban(long time, String str, String chan) {
        unbanTime = time;
        unbanString = str;
        channel = chan;
    }

    public long getUnbanTime() {
        return unbanTime;
    }

    public String getUnbanString() {
        return unbanString;
    }

    public String getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "Ban{str=" + unbanString + ",chan=" + channel + ",time=" + unbanTime + "}";
    }
}
