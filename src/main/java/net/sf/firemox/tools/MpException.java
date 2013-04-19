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
package net.sf.firemox.tools;

import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class MpException extends Exception {

	/**
	 * Create a new instance of this class.
	 * 
	 * @param i18nMessage
	 *          resource bundle message key
	 */
	public MpException(String i18nMessage) {
		super(i18nMessage);
		this.i18nMessage = i18nMessage;
	}

	/**
	 * Return the translated message corresponding to the message key.
	 * 
	 * @return the translated message corresponding to the message key.
	 */
	public String i18nMessage() {
		return LanguageManager.getString(i18nMessage);
	}

	/**
	 * The untranslated message to use as resource bundle key.
	 */
	private String i18nMessage;
}
