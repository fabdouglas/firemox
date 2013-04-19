/*
 * Created on 8 avr. 2005
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
package net.sf.firemox.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import net.sf.firemox.AbstractMainForm;
import net.sf.firemox.Magic;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.tools.WebBrowser;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.xml.XmlConfiguration;

import org.apache.commons.io.FileUtils;

/**
 * An ActionListener that listens to the radio buttons menus
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.83
 * @since 0.85 settings are "per TBS"
 */
public class MdbListener implements ActionListener {

	/**
	 * Create a new instance of MdbListener given an AbstractMainForm instance.
	 * 
	 * @param abstractMainForm
	 */
	public MdbListener(AbstractMainForm abstractMainForm) {
		this.abstractMainForm = abstractMainForm;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if ("menu_options_tbs_more".equals(e.getActionCommand())) {
			// goto "more TBS" page
			try {
				WebBrowser
						.launchBrowser("http://sourceforge.net/project/showfiles.php?group_id="
								+ IdConst.PROJECT_ID + "&package_id=107882");
			} catch (Exception e1) {
				JOptionPane.showOptionDialog(MagicUIComponents.magicForm,
						LanguageManager.getString("error") + " : " + e1.getMessage(),
						LanguageManager.getString("web-pb"), JOptionPane.OK_OPTION,
						JOptionPane.INFORMATION_MESSAGE, UIHelper
								.getIcon("wiz_update_error.gif"), null, null);
			}
			return;
		}
		if ("menu_options_tbs_update".equals(e.getActionCommand())) {
			// update the current TBS
			XmlConfiguration.main(new String[] { "-g", MToolKit.tbsName });
			return;
		}
		if ("menu_options_tbs_rebuild".equals(e.getActionCommand())) {
			/*
			 * rebuild completely the current TBS
			 */
			XmlConfiguration.main(new String[] { "-f", "-g", MToolKit.tbsName });
			return;
		}

		// We change the TBS

		// Wait for confirmation
		if (JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(
				MagicUIComponents.magicForm, LanguageManager
						.getString("warn-disconnect"), LanguageManager
						.getString("disconnect"), JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, UIHelper.getIcon("wiz_update.gif"), null,
				null)) {

			// Save the current settings before changing TBS
			Magic.saveSettings();

			// Copy this settings file to the profile directory of this TBS
			final File propertyFile = MToolKit.getFile(IdConst.FILE_SETTINGS);
			try {
				FileUtils.copyFile(propertyFile, MToolKit.getTbsFile(
						IdConst.FILE_SETTINGS_SAVED, false));

				// Delete the current settings file of old TBS
				propertyFile.delete();

				// Load the one of the new TBS
				abstractMainForm.setMdb(e.getActionCommand());
				Configuration.loadTemplateFile(MToolKit.getTbsFile(
						IdConst.FILE_SETTINGS_SAVED, false).getAbsolutePath());

				// Copy the saved configuration of new TBS
				FileUtils.copyFile(MToolKit.getTbsFile(IdConst.FILE_SETTINGS_SAVED,
						false), propertyFile);
				Log.info("Successful TBS swith to " + MToolKit.tbsName);

				// Restart the game
				System.exit(IdConst.EXIT_CODE_RESTART);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * The AbstractMainForm instance that would receive the "setMdb(String)"
	 * message.
	 * 
	 * @see AbstractMainForm#setMdb(String)
	 */
	private AbstractMainForm abstractMainForm;
}
