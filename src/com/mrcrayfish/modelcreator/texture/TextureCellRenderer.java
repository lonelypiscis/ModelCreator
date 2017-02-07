package com.mrcrayfish.modelcreator.texture;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

public class TextureCellRenderer extends DefaultListCellRenderer
{
	private static final long serialVersionUID = 1L;

	private JLabel lbl = new JLabel();

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{
		int icon_scale = TextureManager.icon_scale_current;
		String icon_name = (String) list.getModel().getElementAt(index);
		lbl.setIcon(TextureManager.getIcon(icon_name, icon_scale));
		lbl.setText(icon_name);
		lbl.revalidate();
		if (isSelected)
		{
			lbl.setBackground(Color.LIGHT_GRAY);
			lbl.setOpaque(true);
			lbl.setEnabled(false);
		}
		else
		{
			lbl.setOpaque(false);
			lbl.setEnabled(true);
		}
		return lbl;
	}
}
