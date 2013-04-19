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
package net.sf.firemox.ui.component;

import org.jvnet.lafwidget.text.PasswordStrengthChecker;
import org.jvnet.lafwidget.utils.LafConstants.PasswordStrength;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.94
 */
public class PasswordChecker implements PasswordStrengthChecker {
	public PasswordStrength getStrength(char[] password) {
		if (password == null)
			return PasswordStrength.WEAK;
		int length = password.length;
		if (length < 3)
			return PasswordStrength.WEAK;
		if (length < 6)
			return PasswordStrength.MEDIUM;
		return PasswordStrength.STRONG;
	}

	public String getDescription(PasswordStrength strength) {
		if (strength == PasswordStrength.WEAK)
			return "<html>This password is <b>way</b> too weak</html>";
		if (strength == PasswordStrength.MEDIUM)
			return "<html>Come on, you can do<br> a little better than that</html>";
		if (strength == PasswordStrength.STRONG)
			return "OK";
		return null;
	}
}
