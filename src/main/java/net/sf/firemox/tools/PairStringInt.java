/*
 * Created on Oct 8, 2004 
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
package net.sf.firemox.tools;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class PairStringInt implements Comparable<PairStringInt> {

	/**
	 * The stored string instance.
	 */
	public String text;

	/**
	 * The stored int value.
	 */
	public int value;

	/**
	 * Creates a new instance of PairStringInt <br>
	 * 
	 * @param text
	 * @param value
	 */
	public PairStringInt(String text, int value) {
		this.text = text;
		this.value = value;
	}

	public int compareTo(PairStringInt pair) {
		if (value == pair.value) {
			return -1;
		}
		if (value < pair.value) {
			return -1;
		}
		return 1;
	}

	@Override
	public String toString() {
		return "{" + text + ":" + value + "}";
	}

	@Override
	public boolean equals(Object context) {
		return context == this;
	}

	@Override
	public int hashCode() {
		return text.hashCode();
	}
}
