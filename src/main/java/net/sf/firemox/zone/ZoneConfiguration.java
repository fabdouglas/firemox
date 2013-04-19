/*
 *   Firemox is a turn based strategy simulator
 *   Copyright (C) 2003-2007 Fabrice Daugan
 *
 *   This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 *   This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details.
 *
 *   You should have received a copy of the GNU General Public License along  
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sf.firemox.zone;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;

import net.sf.firemox.tools.MToolKit;

/**
 * Corresponds to the zone configuration : name and borderlayout constraints.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.93
 */
public class ZoneConfiguration {

	/**
	 * The zone name.
	 */
	private final String zoneName;

	/**
	 * The zone id.
	 */
	private final int zoneId;

	/**
	 * The layout constraint when this zone is controled by you.
	 */
	private final String layoutConstraintYou;

	/**
	 * The layout class name.
	 */
	private final String layoutClass;

	/**
	 * The layout constraint when this zone is controled by the opponent.
	 */
	private final String layoutConstraintOpponent;

	/**
	 * Create a new instance of this class from a steam.
	 * 
	 * @param dbStream
	 *          the stream containing the configuration.
	 * @param zoneId
	 *          the zone id of this configuration.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	ZoneConfiguration(InputStream dbStream, int zoneId) throws IOException {
		this.zoneId = zoneId;
		zoneName = MToolKit.readString(dbStream);
		layoutClass = StringUtils.trimToNull(MToolKit.readString(dbStream));
		layoutConstraintYou = MToolKit.readString(dbStream);
		layoutConstraintOpponent = MToolKit.readString(dbStream);
	}

	/**
	 * The layout constraint when this zone is controled by the opponent.
	 * 
	 * @return The layout constraint when this zone is controled by the opponent.
	 */
	public String getLayoutConstraintOpponent() {
		return this.layoutConstraintOpponent;
	}

	/**
	 * The layout constraint when this zone is controled by you.
	 * 
	 * @return The layout constraint when this zone is controled by you.
	 */
	public String getLayoutConstraintYou() {
		return this.layoutConstraintYou;
	}

	/**
	 * The zone name.
	 * 
	 * @return zone name.
	 */
	public String getZoneName() {
		return this.zoneName;
	}

	/**
	 * The zone id.
	 * 
	 * @return zone id.
	 */
	public int getZoneId() {
		return this.zoneId;
	}

	/**
	 * The layout class name.
	 * 
	 * @return The layout class name.
	 */
	public String getLayoutClass() {
		return this.layoutClass;
	}
}
