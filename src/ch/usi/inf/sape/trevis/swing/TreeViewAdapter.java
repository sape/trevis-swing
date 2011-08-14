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
 * A default implementation of a TreeViewListener.
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public class TreeViewAdapter implements TreeViewListener {

	public void rendererChanged(final TreeView view) {}
	public void treeChanged(final TreeView view) {}
	public void topNodeChanged(final TreeView view) {}
	public void currentNodeChanged(final TreeView view) {}
	
}
