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
 * 
 */
package net.sf.firemox.action.target;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.token.IdTargets;
import net.sf.firemox.token.IdTokens;
import net.sf.firemox.token.Register;

/**
 * @since 0.86
 */
public final class TargetFactory {

	private TargetFactory() {
		super();
	}

	/**
	 * <li>target type [Register]</li>
	 * <li>test [Test]</li>
	 * <li>options [int]</li>
	 * <li>target action [Target]</li>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @return next Target object read from the specified stream.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static net.sf.firemox.action.Target readNextTarget(
			InputStream inputFile) throws IOException {
		final Register register = Register.deserialize(inputFile);
		if (register.ordinal() == IdTokens.PRIVATE_NAMED_TARGETABLE)
			return new PrivateObject(inputFile);
		if (register.isGlobal()) {
			// read the test, hop and option
			final int options = inputFile.read();
			if ((options & 0x30) != IdTargets.RAISE_EVENT_NOT
					&& (options & 0x0F) != IdTargets.ALL) {
				return new RealTarget(register.ordinal(), options, inputFile);
			}
			return new SilentTarget(register.ordinal(), options, inputFile);
		}
		return SingletonTarget.getInstance(register.getTargetable());
	}
}