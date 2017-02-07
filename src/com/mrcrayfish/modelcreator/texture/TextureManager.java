package com.mrcrayfish.modelcreator.texture;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.BufferedImageUtil;

import com.mrcrayfish.modelcreator.ModelCreator;
import com.mrcrayfish.modelcreator.Settings;
import com.mrcrayfish.modelcreator.element.ElementManager;
import com.mrcrayfish.modelcreator.panels.SidebarPanel;
import javax.swing.SpringLayout;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.SwingConstants;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

public class TextureManager
{
	private static List<TextureEntry> textureCache = new ArrayList<TextureEntry>();

	public static Texture cobblestone;
	public static Texture dirt;

	public static File lastLocation = null;
	
	public static int icon_scale_min = 16;
	public static int icon_scale_max = 264;
	public static int icon_scale_current = 128;

	public static boolean loadExternalTexture(File image, File meta) throws IOException
	{
		TextureMeta textureMeta = TextureMeta.parse(meta);

		if (textureMeta != null)
		{
			if (textureMeta.getAnimation() != null)
			{
				BufferedImage bimage = ImageIO.read(image);

				int fWidth = textureMeta.getAnimation().getWidth();
				int fHeight = textureMeta.getAnimation().getHeight();

				ImageIcon icon = null;

				List<Texture> textures = new ArrayList<Texture>();

				int xpos = 0;
				while (xpos + fWidth <= bimage.getWidth())
				{
					int ypos = 0;
					while (ypos + fHeight <= bimage.getHeight())
					{
						BufferedImage subImage = bimage.getSubimage(xpos, ypos, fWidth, fHeight);
						if (icon == null)
						{
							icon = TextureManager.upscale(new ImageIcon(subImage), 256);
						}
						Texture texture = BufferedImageUtil.getTexture("", subImage);
						textures.add(texture);
						ypos += fHeight;
					}
					xpos += fWidth;
				}
				String imageName = image.getName();
				textureCache.add(new TextureEntry(image.getName().substring(0, imageName.indexOf(".png")), textures, icon, image.getAbsolutePath(), textureMeta, meta.getAbsolutePath()));
				return true;
			}
			return loadTexture(image, textureMeta, meta.getAbsolutePath());
		}
		return loadTexture(image, null, null);
	}

	private static boolean loadTexture(File image, TextureMeta meta, String location) throws IOException
	{
		FileInputStream is = new FileInputStream(image);
		Texture texture = TextureLoader.getTexture("PNG", is);
		is.close();

		if (texture.getImageHeight() % 16 != 0 | texture.getImageWidth() % 16 != 0)
		{
			texture.release();
			return false;
		}
		ImageIcon icon = upscale(new ImageIcon(image.getAbsolutePath()), 256);
		textureCache.add(new TextureEntry(image.getName().replace(".png", "").replaceAll("\\d*$", ""), texture, icon, image.getAbsolutePath(), meta, location));
		return true;
	}

	public static ImageIcon upscale(ImageIcon source, int length)
	{
		Image img = source.getImage();
		Image newimg = img.getScaledInstance(length, length, java.awt.Image.SCALE_FAST);
		return new ImageIcon(newimg);
	}

	public static TextureEntry getTextureEntry(String name)
	{
		for (TextureEntry entry : textureCache)
		{
			if (entry.getName().equalsIgnoreCase(name))
			{
				return entry;
			}
		}
		return null;
	}

	public static Texture getTexture(String name)
	{
		for (TextureEntry entry : textureCache)
		{
			if (entry.getName().equalsIgnoreCase(name))
			{
				return entry.getTexture();
			}
		}
		return null;
	}

	public static String getTextureLocation(String name)
	{
		for (TextureEntry entry : textureCache)
		{
			if (entry.getName().equalsIgnoreCase(name))
			{
				return entry.getTextureLocation();
			}
		}
		return null;
	}
	
	public static String getMetaLocation(String name)
	{
		for (TextureEntry entry : textureCache)
		{
			if (entry.getName().equalsIgnoreCase(name))
			{
				return entry.getMetaLocation();
			}
		}
		return null;
	}

	public static ImageIcon getIcon(String name)
	{
		for (TextureEntry entry : textureCache)
		{
			if (entry.getName().equalsIgnoreCase(name))
			{
				return entry.getImage();
			}
		}
		return null;
	}
	
	public static ImageIcon getIcon(String name, int scale) {
		ImageIcon imageIcon = getIcon(name);
		Image image = imageIcon.getImage();
		Image newimg = image.getScaledInstance(scale, scale, java.awt.Image.SCALE_FAST);
		imageIcon = new ImageIcon(newimg); 
		
		return imageIcon;
	}

	private static String texture = null;

	/**
	 * @wbp.parser.entryPoint
	 */
	public static String display(ElementManager manager)
	{
		Font defaultFont = new Font("SansSerif", Font.BOLD, 18);

		DefaultListModel<String> model = generate();
		JList<String> list = new JList<String>();
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setCellRenderer(new TextureCellRenderer());
		list.setVisibleRowCount(-1);
		list.setModel(model);
		list.setFixedCellHeight(icon_scale_current);
		list.setFixedCellWidth(icon_scale_current + icon_scale_max);
		list.setBackground(new Color(221, 221, 228));
		JScrollPane scroll = new JScrollPane(list);
		scroll.getVerticalScrollBar().setVisible(false);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		JDialog dialog = new JDialog(((SidebarPanel) manager).getCreator(), "Texture Manager", false);
		dialog.getContentPane().setLayout(new BorderLayout());
		dialog.setResizable(false);
		dialog.setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
		dialog.setPreferredSize(new Dimension(540, 480));
		dialog.getContentPane().add(scroll, BorderLayout.CENTER);
		
		JPanel pnlFooter = new JPanel();
		dialog.getContentPane().add(pnlFooter, BorderLayout.SOUTH);
		pnlFooter.setLayout(new BorderLayout(0, 0));
		

		JPanel pnlMainControlls = new JPanel();
		pnlFooter.add(pnlMainControlls, BorderLayout.NORTH);
		pnlMainControlls.setPreferredSize(new Dimension(1000, 40));
				pnlMainControlls.setLayout(new BoxLayout(pnlMainControlls, BoxLayout.X_AXIS));
				
				JSlider sldImageScale = new JSlider(icon_scale_min, icon_scale_max, icon_scale_current);
				pnlMainControlls.add(sldImageScale);
				sldImageScale.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e)
					{
						icon_scale_current = sldImageScale.getValue();
						list.setFixedCellHeight(icon_scale_current);
						list.setFixedCellWidth(icon_scale_current + icon_scale_max);
						list.repaint();
					}
				});
				sldImageScale.setPaintTicks(true);
				sldImageScale.setMajorTickSpacing(16);
				
				Component horizontalStrut_1 = Box.createHorizontalStrut(100);
				pnlMainControlls.add(horizontalStrut_1);
				
						JButton btnImport = new JButton("Import");
						pnlMainControlls.add(btnImport);
						btnImport.setHorizontalAlignment(SwingConstants.RIGHT);
						btnImport.addActionListener(a ->
						{
							JFileChooser chooser = new JFileChooser();
							chooser.setDialogTitle("Input File");
							chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
							chooser.setApproveButtonText("Import");
							
							if (lastLocation == null) {
								String dir = Settings.getImageImportDir();

								if (dir != null)
									lastLocation = new File(dir);
							}
							
							if (lastLocation != null) {
								chooser.setCurrentDirectory(lastLocation);
							}
							else
							{
								try
								{
									chooser.setCurrentDirectory(new File(ModelCreator.texturePath));
								}
								catch (Exception e1) {
									e1.printStackTrace();
								}
							}


							FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Images", "png");
							chooser.setFileFilter(filter);
							int returnVal = chooser.showOpenDialog(null);
							if (returnVal == JFileChooser.APPROVE_OPTION)
							{
								lastLocation = chooser.getSelectedFile().getParentFile();
								Settings.setImageImportDir(lastLocation.toString());
								
								try
								{
									File meta = new File(chooser.getSelectedFile().getAbsolutePath() + ".mcmeta");
									manager.addPendingTexture(new PendingTexture(chooser.getSelectedFile(), meta, new TextureCallback()
									{
										@Override
										public void callback(boolean success, String texture)
										{
											if (success)
											{
												model.insertElementAt(texture.replace(".png", ""), 0);
											}
											else
											{
												JOptionPane error = new JOptionPane();
												error.setMessage("Width and height must be a multiple of 16.");
												JDialog dialog = error.createDialog(btnImport, "Texture Error");
												dialog.setLocationRelativeTo(null);
												dialog.setModal(false);
												dialog.setVisible(true);
											}
										}
									}));
								}
								catch (Exception e1)
								{
									e1.printStackTrace();
								}
							}
						});
						btnImport.setFont(defaultFont);
		
				JButton btnClose = new JButton("Close");
				pnlMainControlls.add(btnClose);
				btnClose.setAlignmentX(Component.RIGHT_ALIGNMENT);
				btnClose.addActionListener(a ->
				{
					texture = null;
					SwingUtilities.getWindowAncestor(btnClose).dispose();
				});
				btnClose.setFont(defaultFont);
				
				JButton btnSelect = new JButton("Apply");
				pnlMainControlls.add(btnSelect);
				btnSelect.setAlignmentX(Component.RIGHT_ALIGNMENT);
				btnSelect.addActionListener(a ->
				{
					if (list.getSelectedValue() != null)
					{
						texture = list.getSelectedValue();
						SwingUtilities.getWindowAncestor(btnSelect).dispose();
					}
				});
				btnSelect.setFont(defaultFont);
				
				Component horizontalStrut = Box.createHorizontalStrut(20);
				pnlMainControlls.add(horizontalStrut);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);

		return texture;
	}

	private static DefaultListModel<String> generate()
	{
		DefaultListModel<String> model = new DefaultListModel<String>();
		for (TextureEntry entry : textureCache)
		{
			model.addElement(entry.getName());
		}
		return model;
	}
}
