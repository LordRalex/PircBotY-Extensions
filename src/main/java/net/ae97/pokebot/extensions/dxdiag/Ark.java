package net.ae97.pokebot.extensions.dxdiag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UrielSalads
 * Copyright (C) 2016 Uriel Salischiker
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Ark {
    public List<CPU> d = new ArrayList<>();

    public static class CPU {
        Map<String, String> __metadata = new HashMap<String, String>();
        int ProductId = 0;
        int CodeNameEPMId = 0;
        String GraphicsModel = null;
    }
}
