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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import ch.usi.inf.sape.trevis.model.ContextTree;
import ch.usi.inf.sape.trevis.swing.Surface;
import ch.usi.inf.sape.trevis.swing.TreeView;


/**
 * A dialog to export a tree into an image file (e.g., into a PNG).
 * 
 * @author Matthias.Hauswirth@usi.ch
 */
public final class ImageExportDialog extends JDialog {

	private TreeView treeView;
	private JTextField widthField;
	private JTextField heightField;
	private JCheckBox transparentBackgroundBox;
	private ImageCanvas imageCanvas;
	private JButton saveButton;
	private JButton copyButton;
	private JButton cancelButton;
	private BufferedImage image;
	
	
	public ImageExportDialog(final Frame parent, final TreeView view) {
		super(parent, true);
		init(view);
	}
	
	public ImageExportDialog(final Dialog parent, final TreeView view) {
		super(parent, true);
		init(view);
	}
	
	private void init(final TreeView view) {
		this.treeView = view;
		final ContextTree tree = view.getTree();
		setTitle(tree==null?"Export tree to Image":"Export '"+tree.getName()+"' to Image");
		setResizable(true);
		final JPanel content = new JPanel(new BorderLayout());
		content.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		final JPanel optionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		optionPanel.add(new JLabel("Width (pixels): "));
		widthField = new JTextField(6);
		widthField.setText("600");
		optionPanel.add(widthField);
		optionPanel.add(new JLabel("Height (pixels): "));
		heightField = new JTextField(6);
		optionPanel.add(heightField);
		heightField.setText("600");
		transparentBackgroundBox = new JCheckBox("Transparent background");
		transparentBackgroundBox.setEnabled(true);
		optionPanel.add(transparentBackgroundBox);
		content.add(optionPanel, BorderLayout.NORTH);
		imageCanvas = new ImageCanvas();
		content.add(new JScrollPane(imageCanvas));
		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);
		copyButton = new JButton("Copy to clipboard");
		buttonPanel.add(copyButton);
		saveButton = new JButton("Save...");
		saveButton.setMnemonic('S');
		buttonPanel.add(saveButton);
		content.add(buttonPanel, BorderLayout.SOUTH);
		setContentPane(content);
		getRootPane().setDefaultButton(saveButton);
		
		
		updateImage();
		pack();
		setLocationRelativeTo(getParent());
		
		final KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		rootPane.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(final ActionEvent ev) {
				setVisible(false);
			}
		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		transparentBackgroundBox.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent ev) {
				updateImage();
			}
		});
		widthField.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ev) {
				updateImage();
			}
		});
		heightField.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ev) {
				updateImage();
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ev) {
				setVisible(false);
			}
		});
		copyButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ev) {
				copy();
			}
		});
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ev) {
				save();
			}
		});
	}
	
	private void updateImage() {
		try {
			final int width = Integer.parseInt(widthField.getText());
			final int height = Integer.parseInt(heightField.getText());
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2 = image.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			if (!transparentBackgroundBox.isSelected()) {
				g2.setColor(Color.WHITE);
				g2.fillRect(0, 0, image.getWidth(), image.getHeight());
			}
			treeView.render(g2, new Surface(width, height));
			g2.dispose();
			imageCanvas.setImage(image);
		} catch (final NumberFormatException ex) {
			try {
				final int width = Integer.parseInt(widthField.getText());
				heightField.selectAll();
				heightField.requestFocus();
			} catch (final NumberFormatException ex1) {
				widthField.selectAll();
				widthField.requestFocus();
			}
		}
	}
	
	private void copy() {
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(new Transferable() {
			public boolean isDataFlavorSupported(final DataFlavor flavor) {
				System.out.println("Transferable.isDataFlavorSupported("+flavor+")");
				return flavor.equals(DataFlavor.imageFlavor);
			}
			public DataFlavor[] getTransferDataFlavors() {
				System.out.println("Transferable.getTransferDataFlavors()");
				return new DataFlavor[] { DataFlavor.imageFlavor };
			}
			public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
				System.out.println("Transferable.getTransferData("+flavor+")");
			    if (isDataFlavorSupported(flavor)) {
			        return image;
			      }
			      return null;
			}
		}, new ClipboardOwner() {
			public void lostOwnership(final Clipboard clipboard, final Transferable contents) {
				System.out.println("ClipboardOwner.lostOwnership()");
			}
		});
	}
	
	private static final class ImageFormatFileFilter extends FileFilter {
		private final String formatName;
		public ImageFormatFileFilter(final String formatName) {
			this.formatName = formatName;
		}
		public String getFormatName() {
			return formatName;
		}
		public String getDescription() {
			return formatName.toUpperCase()+" Image";
		}
		public boolean accept(final File file) {
			return file.getName().toLowerCase().endsWith("."+formatName.toLowerCase());
		}		
	}
	
	private void save() {
		JFileChooser chooser = new JFileChooser();
		final String[] formatNames = ImageIO.getWriterFormatNames();
		final HashSet<String> formatNameSet = new HashSet<String>();
		for (final String formatName : formatNames) {
			formatNameSet.add(formatName.toLowerCase());
		}
		final String[] sortedUniqueFormatNames = new String[formatNameSet.size()];
		formatNameSet.toArray(sortedUniqueFormatNames);
		Arrays.sort(sortedUniqueFormatNames);
		ImageFormatFileFilter pngFilter = null;
		for (final String formatName : sortedUniqueFormatNames) {
			final ImageFormatFileFilter filter = new ImageFormatFileFilter(formatName);
			//System.out.println(formatName+" "+filter.getDescription());
			if (formatName.equals("png")) {
				//System.out.println("  PNG");
				pngFilter = filter;
			}
			chooser.addChoosableFileFilter(filter);
		}
		chooser.setAcceptAllFileFilterUsed(false);
		if (pngFilter!=null) {
			//System.out.println("PNG: "+pngFilter.getDescription());
			chooser.setFileFilter(pngFilter);
		}
		//TODO: JPG does not seem to work (all black)
		//chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		//chooser.setMultiSelectionEnabled(false);
		final String name;
		if (treeView.getTree()!=null && treeView.getTree().getName()!=null) {
			name = treeView.getTree().getName();
		} else {
			name = "tree";
		}
		chooser.setSelectedFile(new File(chooser.getCurrentDirectory(), name+".png"));
		if (chooser.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
			final String formatName = ((ImageFormatFileFilter)chooser.getFileFilter()).getFormatName();
			try {
				File file = chooser.getSelectedFile();
				if (!file.getName().contains(".")) {
					file = new File(file.getPath()+"."+formatName);
				}
				ImageIO.write(image, formatName, file);
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
			setVisible(false);
		}
	}
	
}
