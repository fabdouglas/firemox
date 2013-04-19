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

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class Log extends Ok {

	/**
	 * Creates a new instance of Log <br>
	 */
	public Log() {
		super(LanguageManager.getString("wiz_log.title"), LanguageManager
				.getString("wiz_log.description"), "wiz_log.gif", null, 600, 400);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		final JTabbedPane pane = new JTabbedPane();
		final File log = MToolKit.getFile(IdConst.FILE_DEBUG);
		if (!log.exists()) {
			JOptionPane.showMessageDialog(this,
					"File 'debug.log' has not beeen found", LanguageManager
							.getString("error"), JOptionPane.ERROR_MESSAGE);
		}
		try {
			final BufferedReader reader = new BufferedReader(new FileReader(log));
			String line = null;
			final List<String> sessions = new ArrayList<String>();
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#Session")) {
					// it's a new session
					final JScrollPane scroll = new JScrollPane(
							ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
							ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					scroll.setAutoscrolls(true);
					scroll.setMinimumSize(new Dimension(500, 200));
					scroll.setViewportView(new JTextArea(line.substring(1) + "\n\n"));
					pane.add(scroll, line.substring(line.indexOf(";") + 1));
					sessions
							.add(line.substring(line.indexOf(":") + 1, line.indexOf(";")));
				} else if (line.indexOf('#') != -1) {
					int index = sessions.indexOf(line.substring(0, line.indexOf('#')));
					if (index >= 0) {
						// the session has been found
						((JTextArea) ((JScrollPane) pane.getComponent(index)).getViewport()
								.getView())
								.append(line.substring(line.indexOf('#') + 1) + "\n");
					}
					// else --> truncated session
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.gameParamPanel.add(pane);
		pane.setMinimumSize(new Dimension(500, 200));
		doLayout();
	}

}
