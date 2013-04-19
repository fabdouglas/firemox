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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.sf.firemox.ui.MListModel;

import org.apache.commons.io.IOUtils;

/**
 * MSaveDeck.java Created on 2003/12/18 This class contains methodes saving the
 * list of cards and quantity into a file. This new file will contain the cards
 * with their quantity with this format :<br>
 * <i>card's name </i> <b>; </b> <i>qty </i> <b>\n </b> <br>
 * So this one will be compatible with Firemox deck file format.
 * 
 * @author Jan Blaha
 * @author Fabrice Daugan
 */

public final class MSaveDeck {

	/**
	 * Create a new instance of this class.
	 */
	private MSaveDeck() {
		super();
	}

	/**
	 * Saves deck to ASCII file from specified staticTurnLists. This new file will
	 * contain the card names sorted in alphabetical order with their quantity
	 * with this format :<br>
	 * <i>card's name </i> <b>; </b> <i>qty </i> <b>\n </b> <br>
	 * 
	 * @param fileName
	 *          Name of new file.
	 * @param names
	 *          ListModel of card names.
	 * @param parent
	 *          the parent
	 * @return true if the current deck has been correctly saved. false otherwise.
	 */
	public static boolean saveDeck(String fileName,
			MListModel<MCardCompare> names, JFrame parent) {
		PrintWriter outStream = null;
		try {
			// create the deckfile. If it was already existing, it would be scrathed.
			outStream = new PrintWriter(new FileOutputStream(fileName));
		} catch (FileNotFoundException ex) {
			JOptionPane.showMessageDialog(parent,
					"Cannot create/modify the specified deck file:" + fileName + "\n"
							+ ex.getMessage(), "File creation problem",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		Object[] namesArray = names.toArray();
		MCardCompare[] cards = new MCardCompare[namesArray.length];
		System.arraycopy(namesArray, 0, cards, 0, namesArray.length);

		// sorts names
		Arrays.sort(cards, new MCardCompare());
		// writes lines corresponding to this format : "card;quantity\n"
		for (int i = 0; i < cards.length; i++) {
			outStream.println(cards[i].toString());
		}
		IOUtils.closeQuietly(outStream);

		// successfull deck save
		JOptionPane.showMessageDialog(parent, "Saving file "
				+ fileName.substring(fileName.lastIndexOf("/") + 1)
				+ " was successfully completed.", "Save success",
				JOptionPane.INFORMATION_MESSAGE);
		return true;
	}
}