/*
 * Created on Jan 7, 2005
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

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import net.sf.firemox.tools.WebBrowser;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class JLink extends JLabel implements MouseListener {

	/**
	 * Create a new instance of this class.
	 * 
	 * @param link
	 *          the url link.
	 * @param text
	 *          the displayed text.
	 * @param allign
	 *          the allignment.
	 */
	public JLink(String link, String text, int allign) {
		this(link, text, null, allign);
	}

	/**
	 * Create a new instance of this class.
	 * 
	 * @param link
	 *          the url link.
	 * @param text
	 *          the displayed text.
	 */
	public JLink(String link, String text) {
		this(link, text, null, 0);
	}

	/**
	 * Create a new instance of this class.
	 * 
	 * @param link
	 *          the url link.
	 * @param icon
	 *          the displayed icon.
	 * @param allign
	 *          the allignment.
	 */
	public JLink(String link, Icon icon, int allign) {
		this(link, null, icon, allign);
	}

	/**
	 * Create a new instance of this class.
	 * 
	 * @param link
	 *          the url link.
	 * @param icon
	 *          the displayed icon.
	 */
	public JLink(String link, Icon icon) {
		this(link, null, icon, 0);
	}

	/**
	 * Create a new instance of this class.
	 * 
	 * @param link
	 *          the url link.
	 * @param text
	 *          the displayed text.
	 * @param icon
	 *          the displayed icon.
	 */
	public JLink(String link, String text, Icon icon) {
		this(link, text, icon, 0);
	}

	/**
	 * Create a new instance of this class.
	 * 
	 * @param link
	 *          the url link.
	 * @param text
	 *          the displayed text.
	 * @param icon
	 *          the displayed icon.
	 * @param allign
	 *          the allignment.
	 */
	public JLink(String link, String text, Icon icon, int allign) {
		super(text == null ? null : new StringBuilder("<html><a href=\"").append(
				link).append("\">").append(text).append("<a>").toString(), icon, allign);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setHorizontalAlignment(SwingConstants.LEFT);
		this.link = link;
		addMouseListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		try {
			WebBrowser.launchBrowser(link);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(MagicUIComponents.magicForm,
					LanguageManager.getString("error") + " : " + ex.getMessage(),
					LanguageManager.getString("web-pb"), JOptionPane.INFORMATION_MESSAGE,
					null);
		}
	}

	public void mousePressed(MouseEvent e) {
		// Ignore this event
	}

	public void mouseReleased(MouseEvent e) {
		// Ignore this event
	}

	public void mouseEntered(MouseEvent e) {
		// Ignore this event
	}

	public void mouseExited(MouseEvent e) {
		// Ignore this event
	}

	/**
	 * The link
	 */
	private String link;
}
