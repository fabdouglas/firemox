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
package net.sf.firemox;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

import net.sf.firemox.deckbuilder.MdbLoader;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.MdbListener;
import net.sf.firemox.ui.UIHelper;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.83
 */
public abstract class AbstractMainForm extends JFrame implements
		ActionListener, MouseListener, WindowListener {

	/**
	 * Create a new instance of this class.
	 * 
	 * @param title
	 *          the form 's title
	 */
	protected AbstractMainForm(String title) {
		super(title);
		setBackground(Color.BLACK);
	}

	/**
	 * Initialize the components of this form.
	 */
	protected void initAbstractMenu() {
		// "TBS" menu
		tbsMenu = UIHelper.buildMenu("menu_options_tbs");
		ButtonGroup group4 = new ButtonGroup();
		final MdbListener mdbListener = new MdbListener(this);
		final File[] mdbs = MToolKit.getFile(IdConst.TBS_DIR).listFiles(
				(FileFilter) FileFilterUtils.suffixFileFilter("xml"));
		String defaultTbs = MToolKit.tbsName;
		for (File mdb : mdbs) {
			String mdbName = FilenameUtils.getBaseName(mdb.getName());
			JRadioButtonMenuItem itemChck = new JRadioButtonMenuItem();
			MToolKit.tbsName = mdbName;
			updateMdbMenu(mdbName, itemChck);
			itemChck.setActionCommand(mdbName);
			itemChck.setFont(MToolKit.defaultFont);
			itemChck.addActionListener(mdbListener);
			group4.add(itemChck);
			tbsMenu.add(itemChck);
			if (mdbName.equals(defaultTbs)) {
				itemChck.setSelected(true);
			}
		}
		MToolKit.tbsName = defaultTbs;
		tbsMenu.add(new JSeparator());

		// "More TBS" menu item
		tbsMenu.add(UIHelper.buildMenu("menu_options_tbs_more", mdbListener));
		tbsMenu.add(new JSeparator());
		final JMenuItem updateMdbMenu = UIHelper.buildMenu(
				"menu_options_tbs_update", mdbListener);
		updateMdbMenu.setEnabled(false);
		tbsMenu.add(updateMdbMenu);
		tbsMenu.add(UIHelper.buildMenu("menu_options_tbs_rebuild", mdbListener));
		optionMenu.add(new JSeparator());
		optionMenu.add(tbsMenu);
	}

	/**
	 * From the specified MDB file, and the specified radio button menu, we fill
	 * this control with the information read from the MDB file.
	 * 
	 * @param mdbName
	 *          the the MDB file containing the TBS rules
	 * @param itemChck
	 *          the radio button representing this TBS
	 */
	private void updateMdbMenu(String mdbName, JRadioButtonMenuItem itemChck) {
		String fullName = null;
		try {
			final FileInputStream in = MdbLoader.openMdb(IdConst.TBS_DIR + "/"
					+ mdbName + "/" + mdbName + ".mdb", true);
			fullName = MToolKit.readString(in);
			itemChck.setToolTipText("<html><b>" + fullName + "</b> "
					+ MToolKit.readString(in) + "</b> : " + MToolKit.readString(in)
					+ "<br>" + MToolKit.readString(in));
			MdbLoader.closeMdb();
		} catch (IOException e) {
			Log
					.error("The mdb file associated to the TBS '"
							+ mdbName
							+ "' has not been found.\nYou must rebuild this TBS before playing with it");
			fullName = "*" + mdbName + " (rebuild it)";
		} catch (Throwable e) {
			Log.error("Exception during the TBS initalisation.\n\tInput file = "
					+ IdConst.TBS_DIR + "/" + mdbName + "/" + mdbName + ".mdb", e);
			fullName = "*" + mdbName + " (rebuild it)";
		}
		itemChck.setText(fullName);
		itemChck.setSelected(mdbName.equalsIgnoreCase(MToolKit.tbsName));
	}

	/**
	 * Set the current TBS name. Calling this method cause the mana symbols to be
	 * downloaded if it's not yet done.
	 * 
	 * @param tbsName
	 *          the TBS to define as current.
	 */
	public void setMdb(String tbsName) {
		MdbLoader.setToolKitMdb(tbsName);
		for (int i = 0; i < tbsMenu.getItemCount(); i++) {
			if (tbsMenu.getItem(i) != null
					&& tbsName.equals(tbsMenu.getItem(i).getActionCommand())) {
				tbsMenu.getItem(i).setSelected(true);
				return;
			}
		}
	}

	/**
	 * Comment for <code>optionMenu</code>
	 */
	protected JMenu optionMenu;

	/**
	 * The TBS menu
	 */
	private JMenu tbsMenu;

	public abstract void windowClosing(WindowEvent e);

	public void windowOpened(WindowEvent e) {
		// nothing to do
	}

	public void windowClosed(WindowEvent e) {
		// nothing to do
	}

	public void windowIconified(WindowEvent e) {
		// nothing to do
	}

	public void windowDeiconified(WindowEvent e) {
		// nothing to do
	}

	public void windowActivated(WindowEvent e) {
		// nothing to do
	}

	public void windowDeactivated(WindowEvent e) {
		// nothing to do
	}

	public void mousePressed(MouseEvent e) {
		// nothing to do
	}

	public void mouseReleased(MouseEvent e) {
		// nothing to do
	}

	public void mouseEntered(MouseEvent e) {
		// nothing to do
	}

	public void mouseExited(MouseEvent e) {
		// nothing to do
	}
}
