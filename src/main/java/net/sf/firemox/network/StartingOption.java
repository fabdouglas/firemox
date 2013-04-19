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
package net.sf.firemox.network;

import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.94
 */
public enum StartingOption {

	/**
	 * The player starting the game is determined usin a random value.
	 */
	random,

	/**
	 * The server starts the game.
	 */
	server,

	/**
	 * The client starts the game.
	 */
	client;

	/**
	 * Returns the locale value of this element.
	 * 
	 * @return the locale value of this element.
	 */
	public String getLocaleValue() {
		return LanguageManager.getString("wiz_server.whoStarts." + this.toString());
	}

}
