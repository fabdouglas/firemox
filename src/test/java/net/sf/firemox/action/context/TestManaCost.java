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
package net.sf.firemox.action.context;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.*;

import net.sf.firemox.stack.ActionManager;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.ui.i18n.LanguageManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.95
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( { LanguageManager.class })
public class TestManaCost {

	@Test
	public void test() {
		mockStatic(LanguageManager.class);
		
		StackManager.actionManager = mock(ActionManager.class);
		ManaCost manaCost = new ManaCost(new int[] { 2, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
		manaCost.getPossibleRequiredManaList();
	}

}
