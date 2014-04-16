/*
 * Copyright (C) 2014 Lord_Ralex
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
package org.hoenn.pokebot.configuration;

import java.util.Map;

/**
 * Represents a source of configurable options and settings
 */
public interface Configuration extends ConfigurationSection {

    /**
     * Sets the default values of the given paths as provided.
     * <p>
     * If no source {@link Configuration} was provided as a default collection,
     * then a new {@link MemoryConfiguration} will be created to hold the new
     * default values.
     *
     * @param defaults A map of Path->Values to add to defaults.
     * @throws IllegalArgumentException Thrown if defaults is null.
     */
    public void addDefaults(Map<String, Object> defaults);

    /**
     * Sets the default values of the given paths as provided.
     * <p>
     * If no source {@link Configuration} was provided as a default collection,
     * then a new {@link MemoryConfiguration} will be created to hold the new
     * default value.
     * <p>
     * This method will not hold a reference to the specified Configuration, nor
     * will it automatically update if that Configuration ever changes. If you
     * require this, you should set the default source with {@link
     * #setDefaults(net.ae97.rircbot.configuration.Configuration)}.
     *
     * @param defaults A configuration holding a list of defaults to copy.
     * @throws IllegalArgumentException Thrown if defaults is null or this.
     */
    public void addDefaults(Configuration defaults);

    /**
     * Sets the source of all default values for this {@link Configuration}.
     * <p>
     * If a previous source was set, or previous default values were defined,
     * then they will not be copied to the new source.
     *
     * @param defaults New source of default values for this configuration.
     * @throws IllegalArgumentException Thrown if defaults is null or this.
     */
    public void setDefaults(Configuration defaults);

    /**
     * Gets the source {@link Configuration} for this configuration.
     * <p>
     * If no configuration source was set, but default values were added, then a
     * {@link MemoryConfiguration} will be returned. If no source was set and no
     * defaults were set, then this method will return null.
     *
     * @return Configuration source for default values, or null if none exist.
     */
    public Configuration getDefaults();

    /**
     * Gets the {@link ConfigurationOptions} for this {@link Configuration}.
     * <p>
     * All setters through this method are chainable.
     *
     * @return Options for this configuration
     */
    public ConfigurationOptions options();
}
