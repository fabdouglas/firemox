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

import java.awt.Component;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.JOptionPane;

import net.sf.firemox.token.IdConst;
import net.sf.firemox.ui.UIHelper;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * This module checks the available version.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.95
 */
public class VersionChecker {

	/**
	 * Default private constructor.
	 */
	private VersionChecker() {
		super();
	}

	/**
	 * Check the available version and compare it to the current one.
	 * 
	 * @param parentComponent
	 */
	public static void checkVersion(Component parentComponent) {
		try {
			final URL mainPage = new URL(
					"http://sourceforge.net/project/showfiles.php?group_id="
							+ IdConst.PROJECT_ID);
			final BufferedReader br = new BufferedReader(new InputStreamReader(
					(InputStream) mainPage.openConnection().getContent(), "iso-8859-1"));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.indexOf("/project/showfiles.php?group_id="
						+ IdConst.PROJECT_ID + "&amp;package_id=97891&amp;") != -1) {
					String version = line.substring(line.lastIndexOf("\">") + 2);
					version = version.substring(0, version.indexOf("<")).trim();
					if (IdConst.VERSION_NAME.equals(version)) {
						// up-to-date version
						JOptionPane.showMessageDialog(parentComponent, LanguageManager
								.getString("menu_help_check-update.last-version"),
								LanguageManager.getString("version"),
								JOptionPane.INFORMATION_MESSAGE);
					} else {
						// different version
						if (version.compareTo(IdConst.VERSION_NAME) < 0) {
							// We have a development version
							JOptionPane.showMessageDialog(parentComponent, LanguageManager
									.getString("menu_help_check-update.dev-version"),
									LanguageManager.getString("version"),
									JOptionPane.INFORMATION_MESSAGE);
						} else if (JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(
								parentComponent, LanguageManager.getString(
										"menu_help_check-update.new-version", version),
								LanguageManager.getString("version"),
								JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
								UIHelper.getIcon("wiz_update.gif"), null, null)) {
							try {
								WebBrowser
										.launchBrowser("http://sourceforge.net/project/showfiles.php?group_id="
												+ IdConst.PROJECT_ID + "&package_id=97891");
							} catch (Exception e1) {
								JOptionPane.showOptionDialog(parentComponent, LanguageManager
										.getString("error")
										+ " : " + e1.getMessage(), LanguageManager
										.getString("web-pb"), JOptionPane.OK_OPTION,
										JOptionPane.INFORMATION_MESSAGE, UIHelper
												.getIcon("wiz_update_error.gif"), null, null);
							}
						}

					}
					return;
				}
			}
		} catch (Throwable e2) {
			// Ignore this error
			Log.info("Error in version detection", e2);
		}
		JOptionPane.showMessageDialog(parentComponent, LanguageManager
				.getString("menu_help_check-update.unknow-version"), LanguageManager
				.getString("version"), JOptionPane.INFORMATION_MESSAGE);
	}

}
