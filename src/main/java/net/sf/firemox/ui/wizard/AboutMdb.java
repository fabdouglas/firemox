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
package net.sf.firemox.ui.wizard;

import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import net.sf.firemox.deckbuilder.MdbLoader;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManager;

import org.apache.commons.io.IOUtils;

/**
 * AboutMdb.java Created on 16 févr. 2004
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.1
 */
public class AboutMdb extends Ok {

	/**
	 * Creates a new instance of AboutMdb <br>
	 * 
	 * @param parent
	 */
	public AboutMdb(JFrame parent) {
		super(LanguageManager.getString("about.tbs"), "<html><b>"
				+ LanguageManager.getString("tbsname") + ": </b>"
				+ MdbLoader.getTbsFullName() + "<br><b>"
				+ LanguageManager.getString("author") + ": </b>"
				+ MdbLoader.getAuthor() + "<br><b>" + LanguageManager.getString("info")
				+ ": </b>" + MdbLoader.getMoreInfo() + "<br><b>"
				+ LanguageManager.getString("version") + ": </b>"
				+ MdbLoader.getVersion(), "mp64.gif", LanguageManager
				.getString("close"), 420, 320);
		JTextArea disclaimer = new JTextArea();
		disclaimer.setEditable(false);
		disclaimer.setLineWrap(true);
		disclaimer.setWrapStyleWord(true);
		disclaimer.setAutoscrolls(true);
		// Then try and read it locally
		final InputStream inGPL = MToolKit.getResourceAsStream(MToolKit.mdbFile);
		if (inGPL != null) {
			disclaimer.setText(MdbLoader.getDisclaimer().replaceAll("\t", "")
					.replaceAll("\n", ""));
			IOUtils.closeQuietly(inGPL);
		}
		JScrollPane disclaimerSPanel = new JScrollPane();
		disclaimerSPanel
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		MToolKit.addOverlay(disclaimerSPanel);
		disclaimerSPanel.setViewportView(disclaimer);
		gameParamPanel.add(disclaimerSPanel);
		setLocation((getToolkit().getScreenSize().width - 420) / 2, (getToolkit()
				.getScreenSize().height - 320) / 2);
	}

}