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
package org.hoenn.pokebot.api.channels;

import org.hoenn.pokebot.api.recipients.ActionRecipient;
import org.hoenn.pokebot.api.recipients.MessageRecipient;
import org.hoenn.pokebot.api.recipients.ModeRecipient;
import org.hoenn.pokebot.api.recipients.Nameable;
import org.hoenn.pokebot.api.recipients.NoticeRecipient;
import org.hoenn.pokebot.permissions.Permissible;

/**
 *
 * @author Joshua
 */
public abstract class Channel implements MessageRecipient, NoticeRecipient, ModeRecipient, Permissible, Nameable, ActionRecipient {

    public abstract boolean isSecret();

    public abstract String[] getOps();

    public abstract String[] getVoiced();

    public abstract boolean hasOp(String name);

    public abstract boolean hasVoice(String name);

    public abstract String[] getUserList();

    public void kickUser(String name) {
        kickUser(name, null);
    }

    public abstract void kickUser(String name, String reason);

    public abstract void ban(String mask);

    public abstract void opUser(String user);

    public abstract void deopUser(String user);

    public abstract void voiceUser(String user);

    public abstract void devoiceUser(String user);

}
