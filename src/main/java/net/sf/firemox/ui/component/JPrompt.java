/*
 * Firemox is a turn based strategy simulator Copyright (C) 2003-2007 Fabrice
 * Daugan
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package net.sf.firemox.ui.component;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.Picture;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.ToolKit;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public final class JPrompt extends JDialog implements ActionListener {

	/**
	 * Create a new instance of JPrompt
	 */
	private JPrompt() {
		super(MagicUIComponents.magicForm);
		setModal(true);
		// inherit main frame
		final Container con = this.getContentPane();
		con.setLayout(new BoxLayout(con, BoxLayout.X_AXIS));
		pressme = new JButton(LanguageManager.getString("ok"));
		pressme.setMnemonic('o'); // associate hotkey
		pressme.addActionListener(this); // register button
		cancel = new JButton(LanguageManager.getString("cancel"));
		cancel.addActionListener(this); // cancel button
		answer = new JTextField();
		answer.setMaximumSize(new Dimension(32000, 20));
		pressme.setPreferredSize(new Dimension(50, 20));

		final JLabel promptLbl = new JLabel(LanguageManager.getString("entLoc"));
		con.add(promptLbl);
		con.add(answer);
		con.add(pressme);
		setTitle(LanguageManager.getString("webLoc"));
		setBounds(100, 100, 350, 70); // position frame
		answer.requestFocus();
		getRootPane().setDefaultButton(pressme);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		ToolKit.addCancelByEscapeKey(this, cancel);
	}

	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == pressme) {
			value = answer.getText();
			if (value.length() > 0 && value.charAt(value.length() - 1) == '/') {
				value = value.substring(0, value.length() - 1);
			}
			setVisible(false);
		} else if (source == cancel) {
			value = null;
			setVisible(false);
		}
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			cancel.doClick();
		}
	}

	/**
	 * Create the one instance of this class. The 'static final' are not used for
	 * the 'instance' field, since this class contains object with textes
	 * depending on current language.
	 * 
	 * @return the unique instance of this class.
	 */
	public static JPrompt getInstance() {
		if (instance == null) {
			instance = new JPrompt();
		}
		return instance;
	}

	@Override
	public void setVisible(boolean visible) {
		try {
			if (visible) {
				MagicUIComponents.magicForm.setIconImage(Picture
						.loadImage(IdConst.IMAGES_DIR + "mp_wiz.gif"));
			} else {
				MagicUIComponents.magicForm.setIconImage(Picture
						.loadImage(IdConst.IMAGES_DIR + "mp.gif"));
			}
		} catch (Exception e) {
			// IGNORING
		}
		super.setVisible(visible);
	}

	/**
	 * The instance of JPrompt
	 */
	private static JPrompt instance;

	/**
	 * The ok [default] button
	 */
	private final JButton pressme;

	/**
	 * The cancel button
	 */
	private final JButton cancel;

	/**
	 * The current value
	 */
	public static String value = "";

	/**
	 * The text field containing the last answer
	 */
	public final JTextField answer;

}