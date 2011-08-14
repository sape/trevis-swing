/*
 * This file is licensed to You under the "Simplified BSD License".
 * You may not use this software except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/bsd-license.php
 *
 * See the COPYRIGHT file distributed with this work for information
 * regarding copyright ownership.
 */
package ch.usi.inf.sape.trevis.swing;


/**
 * A Surface defines the area into which to render a tree.
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public final class Surface {

	private final int width;
	private final int height;
	
	
	public Surface(final int width, final int height) {
		this.width = width;
		this.height = height;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
}
