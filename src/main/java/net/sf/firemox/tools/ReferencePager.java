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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.firemox.xml.XmlConfiguration;

/**
 * This class is used to maintain references between reference name and offsets.
 * <br>
 * When the pager is enabled, reference name are not written to the given output
 * stream, but the reference is added and would be written later as an index.
 * <br>
 * When the pager is disabled, reference name are directly written to the given
 * output stream.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.94
 */
public class ReferencePager {

	/**
	 * The maximum offset position. Incraese this value assuming pager
	 * implementation has been updated.
	 */
	public static final int MAX_OFFSET = 256 * 256 * 256;

	/**
	 * Create a new instance of this class. <br>
	 * As default, pager is enabled.
	 */
	public ReferencePager() {
		enabled = true;
		references = new ArrayList<Pair<String, Long>>();
	}

	/**
	 * Add a reference to this pager.
	 * 
	 * @param out
	 *          the destination output stream.
	 * @param referenceName
	 *          the reference to write.
	 */
	public void addReference(OutputStream out, String referenceName) {
		if (enabled) {
			if (out instanceof FileOutputStream) {
				FileOutputStream fileOut = (FileOutputStream) out;
				try {
					if (MAX_OFFSET < fileOut.getChannel().position()) {
						XmlConfiguration
								.fatal("The pager implementation does not support this file size : "
										+ fileOut.getChannel().position()
										+ " (max="
										+ MAX_OFFSET
										+ ")");
					}
					references.add(new Pair<String, Long>(referenceName, fileOut
							.getChannel().position()));
				} catch (IOException e) {
					XmlConfiguration.error("Pager error for reference " + referenceName
							+ " : " + e.getMessage());
				}
				return;
			}
			XmlConfiguration
					.error("Pager feature need a FileOutputStream instance as output");
		}
		MToolKit.writeString(out, referenceName);
	}

	/**
	 * Write the pager content.
	 * 
	 * @param out
	 *          the destination output stream.
	 */
	public void write(OutputStream out) {
		// Write the references
		for (Pair<String, Long> reference : references) {
			MToolKit.writeString(out, reference.key);
			MToolKit.writeInt24(out, reference.value.intValue());
		}

		// Free memory
		references.clear();
	}

	/**
	 * Enable/Disable this pager.
	 * 
	 * @param enabled
	 *          the boolean flag.
	 */
	public void enable(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Return <code>true</code> if this pager is enabled.
	 * 
	 * @return <code>true</code> if this pager is enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Is this pager is enabled.
	 */
	private boolean enabled;

	/**
	 * Is this pager is enabled.
	 */
	private final List<Pair<String, Long>> references;
}
