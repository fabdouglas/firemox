/*
 * Created on 13 avr. 2005
 * 
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
package net.sf.firemox.ui.component;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.83
 */
public class LogArea extends EditorPane {

	/**
	 * Create a new instance of this class.
	 */
	public LogArea() {
		super();
		// StyleConstants.setItalic(style, false);
		// StyleConstants.setUnderline(style, false);
		// StyleConstants.setStrikeThrough(style, false);
	}

	@Override
	public void append(int id, String text) {
		super.append(id, text);
		try {
			StyleConstants.setBold(style, false);
			getDocument().insertString(getDocument().getLength(), text, style);

			// Temporary patch code.
			getDocument().insertString(this.getDocument().getLength(), "\n", style);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		if (!locked) {
			setCaretPosition(getDocument().getLength());
		}
	}
}
