/*
 * JavaVersion.java
 *
 * Copyright © 2004 Wayne Grant
 * waynedgrant@hotmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * (This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package net.sf.firemox.tools;

import java.util.StringTokenizer;

/**
 * Immutable version class constructed from a Java version string (ir without
 * for the current JREs version). The Java version takes the form:
 * <p>
 * major.middle.minor[_update][-identifier]
 * <p>
 * Object's of this class can be used to compare Java different versions. Note
 * that for the purposes of comparison the identifier is considered only in so
 * much as it is present or not - its actual value is unimportant. Therefore for
 * two otherwise identical versions the presence of an identifier in one will
 * make it a lower version than the other. This is because standard identifier
 * values have not been identified by Sun.
 * 
 * @author Wayne Grant
 * @author <a href="fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class JavaVersion extends Object implements Comparable<JavaVersion> {

	/** Version string delimiter */
	private static final char VERSION_DELIMITER = '.';

	/** Start update number */
	private static final char START_UPDATE = '_';

	/** Start identifier */
	private static final char START_IDENTIFIER = '-';

	/**
	 * Int array of version number
	 */
	private int[] versions;

	/**
	 * Construct a JavaVersion object for the current Java environment.
	 */
	public JavaVersion() {
		this(System.getProperty("java.version"));
	}

	/**
	 * Construct a JavaVersion object from the supplied string.
	 * 
	 * @param sJavaVersion
	 *          The Java version string
	 */
	public JavaVersion(String sJavaVersion) {
		// Get indexes of update and identifier
		int iIndexUpdate = sJavaVersion.indexOf(START_UPDATE);
		int iIndexIdentifier = sJavaVersion.indexOf(START_IDENTIFIER);

		// Defaults for version, update and identifier
		String sVersion = null;

		// No update nor identifier
		if (iIndexUpdate == -1 && iIndexIdentifier == -1) {
			sVersion = sJavaVersion; // Version as a string
		} else if (iIndexUpdate != -1 && iIndexIdentifier == -1) {
			// Update but no identifier
			sVersion = sJavaVersion.substring(0, iIndexUpdate);
		} else if (iIndexUpdate == -1 && iIndexIdentifier != -1) {
			// Identifier but no update
			sVersion = sJavaVersion.substring(0, iIndexIdentifier);
		} else {
			// Update and identifier
			sVersion = sJavaVersion.substring(0, iIndexUpdate);
		}

		// Parse version string for major, middle and minor version numbers
		StringTokenizer strTok = new StringTokenizer(sVersion, ""
				+ VERSION_DELIMITER);

		versions = new int[strTok.countTokens()];
		int index = 0;
		while (strTok.hasMoreElements()) {
			try {
				versions[index++] = Integer.parseInt(strTok.nextToken());
			} catch (NumberFormatException ex) {
				throw new InternalError("Bad version number for index " + index);
			}
		}
	}

	/**
	 * Get Java version's major number.
	 * 
	 * @return Minor number
	 */
	public int getMajor() {
		return versions[0];
	}

	/**
	 * Get Java version's middle number.
	 * 
	 * @return Minor number
	 */
	public int getMiddle() {
		return versions[1];
	}

	/**
	 * Get Java version's minor number.
	 * 
	 * @return Minor number
	 */
	public int getMinor() {
		return versions[2];
	}

	/**
	 * Compare this JavaVersion object with another object.
	 * 
	 * @param object
	 *          Object to compare JavaVersion with
	 * @return 0 if the equal, -1 if less, 1 if more than this supplied
	 */
	public int compareTo(JavaVersion object) {
		int[] cmpVersions = object.versions;

		for (int id = 0; id < versions.length; id++) {
			if (id >= cmpVersions.length) {
				if (versions[id] > 0) {
					return 1;
				}
			} else if (versions[id] > cmpVersions[id]) {
				return 1;
			} else if (versions[id] < cmpVersions[id]) {
				return -1;
			}
		}
		for (int id = versions.length; id < cmpVersions.length; id++) {
			if (cmpVersions[id] > 0) {
				return -1;
			}
		}

		// Versions are equal
		return 0;
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}

		if (compareTo((JavaVersion) object) == 0) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return versions.hashCode();
	}

	/**
	 * Get a string representation of the Java version.
	 * 
	 * @return A string representation of the Java version
	 */
	@Override
	public String toString() {
		String res = "";
		for (int i = 0; i < versions.length; i++) {
			res += ", " + versions[i];
		}
		return res;
	}
}
