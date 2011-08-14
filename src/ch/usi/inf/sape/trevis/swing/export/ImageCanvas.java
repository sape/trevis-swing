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
package ch.usi.inf.sape.trevis.swing.export;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;


/**
 * A component to show the rendered tree in the ImageExportDialog.
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public final class ImageCanvas extends JComponent {

	private BufferedImage image;
	
	
	public ImageCanvas() {
	}
	
	public void setImage(final BufferedImage image) {
		this.image = image;
		repaint();
		revalidate();
	}
	
	public Dimension getPreferredSize() {
		if (image!=null) {
			return new Dimension(image.getWidth(), image.getHeight());
		} else {
			return new Dimension(300, 300);
		}
	}
	
	protected void paintComponent(final Graphics g) {
		final Graphics2D g2 = (Graphics2D)g;
		g2.setPaint(new Color(245, 245, 245));
		g2.fillRect(0, 0, getWidth(), getHeight());
		if (image!=null) {
			g2.setPaint(createCheckerboardPaint());
			g2.fillRect(0, 0, image.getWidth(), image.getHeight());
			g2.drawImage(image, 0, 0, this);
		}
	}
	
	private Paint createCheckerboardPaint() {
		final int side = 10;
		final int size = 2*side;
		final BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = image.createGraphics();
		g2.setPaint(Color.LIGHT_GRAY);
		g2.fillRect(0, 0, size, size);
		g2.setPaint(Color.DARK_GRAY);
		g2.fillRect(0, 0, side, side);
		g2.fillRect(side, side, side, side);
		g2.dispose();
		return new TexturePaint(image, new Rectangle(0, 0, image.getWidth(), image.getHeight()));
	}
	
}
