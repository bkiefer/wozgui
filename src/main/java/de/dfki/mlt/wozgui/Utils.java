package de.dfki.mlt.wozgui;

import static de.dfki.mlt.wozgui.Constants.*;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JTextField;

public class Utils {

  public static Font DEFAULT_FONT =
      new Font(GUI_FONT, Font.PLAIN, GUI_FONTSIZE_BUTTONS);

  public static JButton styleButton(JButton result, int maxLength, Font font) {
    String text = result.getText();
    String shortText = (text.length() > maxLength)
        ? text.substring(0, maxLength) + "..."
        : text;
    result.setText(shortText);
    result.setToolTipText(text);
    result.setFont(font);
    result.setEnabled(true);
    result.setAlignmentX(Component.LEFT_ALIGNMENT);
    return result;
  }

  public static JButton finalizeButton(JButton result, int maxLength,
      Dimension buttonSize, Font font) {
    result = styleButton(result, maxLength, font);
    result.setPreferredSize(buttonSize);
    result.setMinimumSize(buttonSize);
    result.setMaximumSize(buttonSize);
    // result.setMaximumSize(new Dimension(290, 40));
    return result;
  }

  public static JButton finalizeButton(JButton result, int maxLength,
      Dimension buttonSize) {
    return finalizeButton(result, maxLength, buttonSize, DEFAULT_FONT);
  }

  /**
   * listener for text-based system actions
   */
  public static void setListeners(final JTextField component,
      ActionListener acceptPressed) {
    component.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        component.getRootPane().setDefaultButton(null);
        component.setFocusable(true);
      }
    });

    component.addActionListener(acceptPressed);
  }
  public static File getResource(File moduleDir, String filePath, String image) {
    return new File(new File(moduleDir, filePath), image);
  }
}