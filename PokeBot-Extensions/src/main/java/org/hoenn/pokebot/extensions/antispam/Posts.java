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
package org.hoenn.pokebot.extensions.antispam;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lord_Ralex
 */
public class Posts {

    private final List<Post> posts = new ArrayList<>();
    private final int MAX_MESSAGES;
    private final int DUPE_RATE;
    private final int SPAM_RATE;
    private final long BUFFER = 10 * 60 * 60 * 1000;

    public Posts(int max, int dupe, int spam) {
        MAX_MESSAGES = max;
        DUPE_RATE = dupe;
        SPAM_RATE = spam;
    }

    public boolean addPost(String lastPost, long timestamp) {
        synchronized (posts) {
            posts.add(new Post(timestamp, lastPost));
            if (posts.size() == MAX_MESSAGES) {
                boolean areSame = true;
                for (int i = 1; i < posts.size() && areSame; i++) {
                    if (!posts.get(i - 1).getMessage().equalsIgnoreCase(posts.get(i).getMessage())) {
                        areSame = false;
                    }
                }
                if (areSame) {
                    if (posts.get(posts.size() - 1).getTime() - posts.get(0).getTime() < DUPE_RATE) {
                        return true;
                    }
                }
                if (posts.get(posts.size() - 1).getTime() - posts.get(0).getTime() < SPAM_RATE) {
                    return true;
                }
                posts.remove(0);
            }
        }
        return false;
    }

    public void cleanLog() {
        synchronized (posts) {
            for (int i = 0; i < posts.size(); i++) {
                if (posts.get(i).getTime() < System.currentTimeMillis() - BUFFER) {
                    posts.remove(i);
                    i--;
                }
            }
        }
    }

    public boolean isEmpty() {
        return posts.isEmpty();
    }
}
