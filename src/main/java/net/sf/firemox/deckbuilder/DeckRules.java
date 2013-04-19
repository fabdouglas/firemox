/*
 * Created on 9 avr. 2005
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
package net.sf.firemox.deckbuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.ui.wizard.Ok;

import org.apache.commons.io.IOUtils;

/**
 * @author <a href="mailto:goldeneyemdk@users.sourceforge.net">Sebastien Genete
 *         </a>
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.83
 */
public class DeckRules extends Ok {

	/**
	 * Create a new instance of DeckRules
	 * 
	 * @param parent
	 */
	public DeckRules(JFrame parent) {
		super(LanguageManager.getString("jdeckrules", MdbLoader.getTbsFullName()),
				LanguageManager.getString("jdeckrules.tooltip", MdbLoader
						.getTbsFullName()), "wiz_library_wiz.png", LanguageManager
						.getString("close"), 490, 300);
		// ... Set initial text, scrolling, and border.
		final JTextArea textRules = new JTextArea();
		textRules.setEditable(false);
		textRules.setLineWrap(true);
		textRules.setWrapStyleWord(true);
		textRules.setAutoscrolls(true);
		textRules.setTabSize(2);
		textRules.setText("No defined rules");
		BufferedReader inGPL = null;
		try {
			inGPL = new BufferedReader(new FileReader(MToolKit
					.getTbsFile("decks/DECK_CONSTRAINTS-"
							+ LanguageManager.getLanguage().getLocale() + "-lang.info")));
			textRules.read(inGPL, "Deck constraints");
		} catch (IOException e) {
			// Ignore this error
		} finally {
			IOUtils.closeQuietly(inGPL);
		}

		final JScrollPane scrollingArea = new JScrollPane(textRules);
		gameParamPanel.add(scrollingArea);
		pack();
	}
}
