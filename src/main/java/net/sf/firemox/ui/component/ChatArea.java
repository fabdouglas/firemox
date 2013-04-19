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
import java.awt.Cursor;
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;

import net.sf.firemox.tools.WebBrowser;
import net.sf.firemox.ui.UIHelper;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.83
 */
public class ChatArea extends EditorPane implements MouseListener,
		MouseMotionListener {

	/**
	 * Path containing the smiles.
	 */
	private final static String FILE_ICON_PATH = "smilies/";

	/**
	 * Create a new instance of this class.
	 * 
	 * @param layout
	 *          the specified layout manager
	 */
	public ChatArea(LayoutManager layout) {
		setLayout(layout);
		// Load style TODO Load these settings from a .properties file
		color = Color.BLACK;
		bold = false;
		italic = false;
		strikeThrough = false;
		StyleConstants.setForeground(style, color);
		StyleConstants.setFontSize(style, 12);
		StyleConstants.setBold(style, false);
		StyleConstants.setItalic(style, false);
		StyleConstants.setStrikeThrough(style, false);
		fEmoticonMap = new HashMap<String, String>();
		fEmoticonMap.put("(CHILL)", "chilled.gif");
		fEmoticonMap.put("(CHILLED)", "chilled.gif");
		fEmoticonMap.put("(CRAZY)", "crazy.gif");
		fEmoticonMap.put("%)", "crazy.gif");
		fEmoticonMap.put("%-)", "crazy.gif");
		fEmoticonMap.put("(G)", "google.gif");
		fEmoticonMap.put("(GOOGLE)", "google.gif");
		fEmoticonMap.put("O)", "google.gif");
		fEmoticonMap.put("0)", "google.gif");
		fEmoticonMap.put("(H)", "huh.gif");
		fEmoticonMap.put(":?", "rolleyes.gif");
		fEmoticonMap.put("(ROLL)", "rolleyes.gif");
		fEmoticonMap.put("8|", "what.gif");
		fEmoticonMap.put("8-|", "what.gif");
		fEmoticonMap.put(":|", "what.gif");
		fEmoticonMap.put(":-|", "what.gif");
		fEmoticonMap.put(":@", "worried.gif");
		fEmoticonMap.put(":-@", "worried.gif");
		fEmoticonMap.put(":P", "tongue.gif");
		fEmoticonMap.put(":-P", "tongue.gif");
		fEmoticonMap.put("(BLIND)", "blind.gif");
		fEmoticonMap.put("(WOW)", "ninja.gif");
		fEmoticonMap.put(":D", "wow.gif");
		fEmoticonMap.put(":-D", "wow.gif");
		fEmoticonMap.put("(NINJA)", "ninja.gif");
		fEmoticonMap.put("(CONF)", "confused.gif");
		fEmoticonMap.put(":S", "confused.gif");
		fEmoticonMap.put(":-S", "confused.gif");
		fEmoticonMap.put("(6)", "evil.gif");
		fEmoticonMap.put(":-)", "smile.gif");
		fEmoticonMap.put(":)", "smile.gif");
		fEmoticonMap.put(";)", "wink.gif");
		fEmoticonMap.put(";-)", "wink.gif");
		fEmoticonMap.put("(X)", "love.gif");
		fEmoticonMap.put(":O", "ohhmy.gif");
		fEmoticonMap.put(":-O", "ohhmy.gif");
		fEmoticonMap.put(":(", "cry.gif");
		fEmoticonMap.put(":-(", "cry.gif");
		fEmoticonMap.put(":'(", "cry.gif");
		fEmoticonMap.put(":'-(", "cry.gif");
		fEmoticonMap.put("(L)", "love.gif");
		fEmoticonMap.put("(ICOOL)", "cool.gif");
		fEmoticonMap.put("(COOL)", "cool.gif");
		fEmoticonMap.put("(LUV)", "love.gif");
		fEmoticonMap.put("(LOVE)", "love.gif");
	}

	@Override
	public void append(int id, String text) {
		super.append(id, text);
		StyleConstants.setBold(style, bold);
		StyleConstants.setForeground(style, color);
		StyleConstants.setFontSize(style, 12);
		StyleConstants.setItalic(style, italic);
		StyleConstants.setStrikeThrough(style, strikeThrough);
		final Map<Integer, String> smiles = getSmiliesIndex(text);
		int currentIndex = 0;
		try {
			for (Entry<Integer, String> entry : smiles.entrySet()) {
				final int index = entry.getKey();
				getDocument().insertString(getDocument().getLength(),
						text.substring(currentIndex, index), style);
				insertIcon(UIHelper.getIcon(FILE_ICON_PATH
						+ fEmoticonMap.get(entry.getValue())));
				currentIndex = index + entry.getValue().length();
			}
			getDocument().insertString(getDocument().getLength(),
					text.substring(currentIndex), style);
			// replaceURL(d1);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		if (!locked) {
			setCaretPosition(getDocument().getLength());
		}
	}

	/**
	 * Return a map linking indexes of smiles into the given message.
	 * 
	 * @param msg
	 *          the message that contain the smiles shortcuts.
	 * @return a map linking indexes of smiles into the given message.
	 */
	private Map<Integer, String> getSmiliesIndex(String msg) {
		final Map<Integer, String> result = new TreeMap<Integer, String>();
		for (Map.Entry<String, String> entry : fEmoticonMap.entrySet()) {
			final String key = entry.getKey();
			int index = msg.indexOf(key);
			while (index != -1) {
				result.put(index, key);
				index = msg.indexOf(key, index + 1);
			}
		}
		return result;
	}

	public void mouseClicked(MouseEvent e) {
		int idx = viewToModel(e.getPoint());
		for (int i = 0; i < startIndex.size(); i++) {
			int sp = startIndex.elementAt(i);
			int ep = endIndex.elementAt(i);
			if (idx >= sp & idx <= ep) {
				String url = linkURL.elementAt(i);
				try {
					WebBrowser.launchBrowser(url);
				} catch (Exception e1) {
					// Ignore this error
				}
				break;
			}
		}
	}

	public void mouseMoved(MouseEvent e) {
		int idx = viewToModel(e.getPoint());
		int a = 0;
		for (int i = 0; i < startIndex.size(); i++) {
			int sp = startIndex.elementAt(i);
			int ep = endIndex.elementAt(i);
			if (idx >= sp & idx <= ep) {
				setCursor(new Cursor(Cursor.HAND_CURSOR));
				a++;
				break;
			}
		}
		if (a == 0) {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public void mouseDragged(MouseEvent arg0) {
		// TODO D&D in chat --> not yet implemented
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

	Color color;

	boolean italic;

	boolean strikeThrough;

	boolean underLine;

	boolean bold;

	private Map<String, String> fEmoticonMap = null;

	private Vector<Integer> startIndex = new Vector<Integer>();

	private Vector<Integer> endIndex = new Vector<Integer>();

	private Vector<String> linkURL = new Vector<String>();

}
