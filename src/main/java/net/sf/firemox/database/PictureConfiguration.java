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
package net.sf.firemox.database;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
class PictureConfiguration {

	/**
	 * The URL where card picture can be downloaded from. This URL contains
	 * dynamic properties computed at each request. The computed URL without
	 * <code>proxyBaseUrl</code> will be used to create the local picture file.
	 */
	private UrlTokenizer pictureUrl;

	/**
	 * Is the base of <code>pictureURL</code> to build the full URL.
	 */
	private String proxyBaseUrl;

	/**
	 * Create a new instance of this class.
	 * 
	 * @param pictureUrl
	 *          The URL where card picture can be downloaded from
	 * @param proxyBaseUrl
	 *          Is the base of <code>pictureURL</code> to build the full URL.
	 */
	PictureConfiguration(UrlTokenizer pictureUrl, String proxyBaseUrl) {
		this.proxyBaseUrl = proxyBaseUrl;
		this.pictureUrl = pictureUrl;
	}

	/**
	 * Return the URL where card picture can be downloaded from. This URL contains
	 * dynamic properties computed at each request. The computed URL without
	 * <code>proxyBaseUrl</code> will be used to create the local picture file.
	 * 
	 * @return the URL where card picture can be downloaded from.
	 */
	public UrlTokenizer getPictureUrl() {
		return pictureUrl;
	}

	/**
	 * Return the base of <code>pictureURL</code> to build the full URL.
	 * 
	 * @return the base of <code>pictureURL</code> to build the full URL.
	 */
	public String getProxyBaseUrl() {
		return proxyBaseUrl;
	}

}
