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
package net.sf.firemox.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;

import net.sf.firemox.stack.StackManager;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManager;

import com.l2fprod.gui.plaf.skin.Skin;
import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public final class SkinLF {

	/**
	 * Prevent this class to be instanciated.
	 */
	private SkinLF() {
		// nothing to do
	}

	/**
	 * Uninstall the given SkinLF
	 * 
	 * @param lookAndFeelName
	 *          the look And Feel Name
	 * @throws Exception
	 */
	public static void uninstallSkinLF(String lookAndFeelName) throws Exception {
		Log.info("Uninstalling SkinLF : " + lookAndFeelName);
		if (lookAndFeelName.endsWith(".xml")) {
			SkinLookAndFeel.loadThemePackDefinition(
					MToolKit.getFile(lookAndFeelName).toURI().toURL()).unload();
		} else if (lookAndFeelName.startsWith("class:")) {
			((Skin) Class.forName(lookAndFeelName.substring(7)).newInstance())
					.unload();
			// } else if (lookAndFeelName.startsWith("theme:")) {
			// TODO Implement "theme" L&F
		} else if (lookAndFeelName.startsWith("zip:")) {
			SkinLookAndFeel.loadThemePack(lookAndFeelName.substring("zip:".length()))
					.unload();
		}
	}

	/**
	 * Install the given SkinLF
	 * 
	 * @param lookAndFeelName
	 *          the look And Feel Name
	 * @throws Exception
	 */
	public static void installSkinLF(String lookAndFeelName) throws Exception {
		Log.info("Installing SkinLF : " + lookAndFeelName + " ...");
		if (lookAndFeelName.endsWith(".xml")) {
			SkinLookAndFeel.setSkin(SkinLookAndFeel.loadThemePackDefinition(MToolKit
					.getFile(lookAndFeelName).toURI().toURL()));
			UIManager.setLookAndFeel("com.l2fprod.gui.plaf.skin.SkinLookAndFeel");
		} else if (lookAndFeelName.startsWith("class:")) {
			SkinLookAndFeel.setSkin((Skin) Class
					.forName(lookAndFeelName.substring(7)).newInstance());
			UIManager.setLookAndFeel("com.l2fprod.gui.plaf.skin.SkinLookAndFeel");
		} else if (lookAndFeelName.startsWith("theme:")) {
			MetalTheme theme = (MetalTheme) Class.forName(
					lookAndFeelName.substring(7)).newInstance();
			MetalLookAndFeel metal = new MetalLookAndFeel();
			MetalLookAndFeel.setCurrentTheme(theme);
			UIManager.setLookAndFeel(metal);
		} else if (lookAndFeelName.startsWith("zip:")) {
			SkinLookAndFeel.setSkin(SkinLookAndFeel.loadThemePack(lookAndFeelName
					.substring("zip:".length())));
			UIManager.setLookAndFeel("com.l2fprod.gui.plaf.skin.SkinLookAndFeel");
		}
		MagicUIComponents.frameDecorated = true;
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		Log.info(" --> ok");
	}

	/**
	 * Return true if the given skin is a SkinLF one
	 * 
	 * @param lookAndFeelName
	 *          the Look&Feel
	 * @return true if the given skin is a SkinLF one
	 */
	public static boolean isSkinLF(String lookAndFeelName) {
		return lookAndFeelName.endsWith(".xml")
				|| lookAndFeelName.startsWith("class:")
				|| lookAndFeelName.startsWith("zip:")
				|| lookAndFeelName.startsWith("theme:");
	}

	/**
	 * Install the given L&F
	 * 
	 * @param lookAndFeelName
	 *          the look & feel to install.
	 * @param source
	 *          the component requesting this install.
	 */
	public static void installLookAndFeel(String lookAndFeelName, Object source) {
		// apply the new UI
		if (MagicUIComponents.lookAndFeelName == lookAndFeelName) {
			return;
		}
		MagicUIComponents.magicForm.setVisible(false);
		try {
			if (isSkinLF(MagicUIComponents.lookAndFeelName)) {
				uninstallSkinLF(MagicUIComponents.lookAndFeelName);
			}
			MagicUIComponents.lookAndFeelName = lookAndFeelName;
			if (isSkinLF(lookAndFeelName)) {
				installSkinLF(lookAndFeelName);
			} else {
				UIManager.setLookAndFeel(lookAndFeelName);
			}
			SwingUtilities.updateComponentTreeUI(MagicUIComponents.magicForm);
			if (MToolKit.fileChooser != null) {
				SwingUtilities.updateComponentTreeUI(MToolKit.fileChooser);
			}
			MagicUIComponents.magicForm.pack();
			MagicUIComponents.magicForm.setExtendedState(Frame.MAXIMIZED_BOTH);
		} catch (UnsupportedLookAndFeelException exc) {
			JRadioButtonMenuItem button = (JRadioButtonMenuItem) source;
			button.setEnabled(false);
			button.setSelected(false);
			for (int i = 0; i < MagicUIComponents.magicForm.lookAndFeels.length; i++) {
				if (MagicUIComponents.magicForm.lookAndFeels[i].isEnabled()) {
					MagicUIComponents.magicForm.lookAndFeels[i].setSelected(true);
				}
			}
		} catch (Exception exc) {
			Log.error(exc);
		}
		MagicUIComponents.magicForm.setVisible(true);
		SwingUtilities.invokeLater(REFRESH_RUNNER);

		if (UIManager.getLookAndFeel().getSupportsWindowDecorations() != MagicUIComponents.frameDecorated) {
			// Make sure we have nice window decorations.
			JOptionPane.showMessageDialog(MagicUIComponents.magicForm,
					LanguageManager.getString("lfrestart"), LanguageManager
							.getString("restart"), JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * This class refresh the split bar
	 */
	public static final Runnable REFRESH_RUNNER = new Runnable() {
		public void run() {
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			StackManager.PLAYERS[0].handSplitter
					.setDividerLocation((int) ((dim.width - 200) * 0.75));
			if (Configuration.getBoolean("reverseSide", false)) {
				StackManager.PLAYERS[1].handSplitter
						.setDividerLocation((int) ((dim.width - 200) * 0.25));
			} else {
				StackManager.PLAYERS[1].handSplitter
						.setDividerLocation((int) ((dim.width - 200) * 0.75));
			}
			if (MagicUIComponents.splash != null) {
				MagicUIComponents.splash.close();
				MagicUIComponents.splash = null;
			}
			MagicUIComponents.magicForm.setVisible(true);
		}
	};

}
