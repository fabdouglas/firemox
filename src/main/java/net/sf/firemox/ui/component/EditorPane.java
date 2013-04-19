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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.border.EtchedBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * @author benny
 */
public class EditorPane extends JTextPane implements ActionListener {

	/**
	 */
	protected EditorPane() {
		style = new SimpleAttributeSet();
		setBorder(new EtchedBorder());
		setEditable(false);
	}

	/**
	 * Set your name and the contact's name
	 * 
	 * @param you
	 * @param contact
	 */
	public void setContact(String you, String contact) {
		this.you = you;
		this.contact = contact;
	}

	/**
	 * Append a new message.
	 * 
	 * @param id
	 *          source id. This identifier is used to determine the text color.
	 * @param text
	 *          text to append.
	 */
	public synchronized void append(int id, String text) {
		StyleConstants.setBold(style, true);
		try {
			StyleConstants.setBold(style, true);
			switch (id) {
			case 0:
				StyleConstants.setForeground(style, Color.GREEN.darker());
				getDocument().insertString(getDocument().getLength(),
						new StringBuilder("[").append(you).append("] ").toString(), style);
				break;
			case 1:
				StyleConstants.setForeground(style, Color.MAGENTA.darker());
				getDocument().insertString(getDocument().getLength(),
						new StringBuilder("[").append(contact).append("] ").toString(),
						style);
				break;
			default:
				StyleConstants.setForeground(style, Color.GRAY);
				getDocument().insertString(getDocument().getLength(), "[syst] ", style);
				break;
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if ("disptime".equals(action)) {
			setDispTime(((JToggleButton) e.getSource()).isSelected());
		} else if ("clear".equals(action)) {
			this.setText("");
		} else if ("lock".equals(action)) {
			this.setLocked(((JToggleButton) e.getSource()).isSelected());
		}
	}

	/**
	 * Is this text form is locked (not auto scroll)
	 * 
	 * @param locked
	 */
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	/**
	 * Is this text form is locked (not auto scroll)
	 * 
	 * @return true if this text form is locked (not auto scroll)
	 */
	public boolean isLocked() {
		return locked;
	}

	/**
	 * Is the time is displayed before each sentence
	 * 
	 * @param dispTime
	 */
	public void setDispTime(boolean dispTime) {
		this.dispTime = dispTime;
	}

	/**
	 * Is the time is displayed before each sentence
	 * 
	 * @return true if the time is displayed.
	 */
	public boolean isDispTime() {
		return dispTime;
	}

	private boolean dispTime;

	/**
	 * Is this text form is locked (not auto scroll)
	 */
	protected boolean locked;

	/**
	 * Your name
	 */
	protected String you;

	/**
	 * Contact's name
	 */
	protected String contact;

	/**
	 * The preferred display style of this editor
	 */
	protected SimpleAttributeSet style;

}