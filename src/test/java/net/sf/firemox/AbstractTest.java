/*
 *   Magic-Project is a turn based strategy simulator
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

import java.io.FileInputStream;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sf.firemox.clickable.target.player.Opponent;
import net.sf.firemox.clickable.target.player.You;
import net.sf.firemox.deckbuilder.MdbLoader;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.zone.ZoneManager;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Abstract class of all tests of this application.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.95
 */
public class AbstractTest {

	/**
	 * Initialize minimum context.
	 */
	@BeforeClass
	public static void intitialize() {
		System.out.println("intitialize()");
		try {
			LanguageManager.initLanguageManager(LanguageManager.DEFAULT_LANGUAGE);
			StackManager.PLAYERS[0] = new You(new ZoneManager(0, new JScrollPane(),
					new JScrollPane(), new JScrollPane(), new JPanel()), new JPanel());
			StackManager.PLAYERS[1] = new Opponent(new ZoneManager(1, new JScrollPane(),
					new JScrollPane(), new JScrollPane(), new JPanel()), new JPanel());
			ZoneManager.init(new JScrollPane(), new JScrollPane());
			MdbLoader.setToolKitMdb("test");
			FileInputStream dbStream = MdbLoader.loadMDB(MToolKit.mdbFile, 0);

			// load the registers and place where aborted spells would be placed
			StackManager.getInstance().init(dbStream, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("ok()");
	}

	/**
	 * Clean the objects.
	 */
	@AfterClass
	public static void clean() {
		// Nothing to do currently
	}

	/**
	 * Prepare the test.
	 */
	@Before
	public void prepare() {
		System.out.println("prepare()");
	}

}
