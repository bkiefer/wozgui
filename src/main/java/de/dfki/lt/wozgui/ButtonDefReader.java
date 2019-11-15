package de.dfki.lt.wozgui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.SwingConstants;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.InputSource;

public class ButtonDefReader {

  public static class Style {
    private static enum StyleParameter {
      font, align,
      fgcolor, bgcolor,
      fontstyle, fontsize,
      buttonwidth, buttonheight,
      span, inset;

      Object[] defaults = {
          "SansSerif", "center",
          null, null,
          Font.PLAIN, 12,
          300, 30,
          1, 2, new int[0] };

      public Object getDefault() {
        return defaults[ordinal()];
      }
    }

    Object[] val = new Object[StyleParameter.values().length];

    private Object get(StyleParameter param) {
      return val[param.ordinal()];
    }

    private Color getColor(StyleParameter param) {
      return val[param.ordinal()] == null || val[param.ordinal()].equals("")
          ? null
          : new Color(Integer.parseInt((String)val[param.ordinal()], 16));
    }

    public Style(Element elt) {
      this(elt, null);
    }

    public Style(Element elt, Style parent) {
      for (StyleParameter param : StyleParameter.values()) {
        Attribute attribute = elt.getAttribute(param.name());
        Object result = null;
        if (attribute != null) {
          if (param.ordinal() >= StyleParameter.fontstyle.ordinal()) {
            try {
              result = attribute.getIntValue();
            }
            catch (DataConversionException ex) {
              result = null;
            }
          } else {
	              result = attribute.getValue();
	      }
        }
        if (result == null && parent != null) {
          result = parent.val[param.ordinal()];
        }
        if (result == null) {
          result = param.getDefault();
        }
        val[param.ordinal()] = result;
      }
    }

    public Font getFont() {
      return new Font((String)get(StyleParameter.font),
          (Integer)get(StyleParameter.fontstyle),
          (Integer)get(StyleParameter.fontsize));
    }

    public Dimension getDimension() {
      int span = ((Integer)get(StyleParameter.span));
      int width = ((Integer)get(StyleParameter.buttonwidth)) * span
          + ((Integer)get(StyleParameter.inset) * (span - 1));
      return new Dimension(width, (Integer)get(StyleParameter.buttonheight));
    }

    public int getInset() { return (Integer)get(StyleParameter.inset); }

    public int getSpan() { return (Integer)get(StyleParameter.span); }

    public Color getFgColor() { return getColor(StyleParameter.fgcolor); }

    public Color getBgColor() { return getColor(StyleParameter.bgcolor); }

    public int getAlignment() {
      String align = (String) get(StyleParameter.align);
      int res = 0;
      switch (align.charAt(0)) {
      case 't' : res = SwingConstants.TOP; break;
      case 'l' : res = SwingConstants.LEFT; break;
      case 'b' : res = SwingConstants.BOTTOM; break;
      case 'r' : res = SwingConstants.RIGHT; break;
      case 'c' : res = SwingConstants.CENTER; break;
      }
      return res;
    }
  }

  private static JButton styleButton(JButton result, int maxLength, Font font) {
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

  /** return width of the button in columns (span) */
  public static void readButton(GridButtonPanel panel,
      Element buttonElt, Style style, int row, int col) {
    String text = buttonElt.getChildTextNormalize("text");
    // No text: spacer
    if (text == null) return;

    // No command: label, no action
    String command = buttonElt.getChildTextNormalize("command");
    // Icon is optional
    String iconName = buttonElt.getChildText("icon");

    panel.addCell(text, command, iconName, style, row, col);
  }

  public static void readGrid(InputSource in, GridButtonPanel panel)
      throws JDOMException, IOException {
    SAXBuilder builder = new SAXBuilder();
    Document doc = builder.build(in);

    Element root = doc.getRootElement();
    assert(root.getName().equals("grid"));
    Style gridStyle = new Style(root);

    List<Element> rows = root.getChildren("row");
    for (int i = 0; i < rows.size(); ++i) {
      Element row = rows.get(i);
      List<Element> buttons = row.getChildren("button");
      int col = 0;
      for (int j = 0; j < buttons.size(); ++j) {
        Element button = buttons.get(j);
        Style buttonStyle = new Style(button, gridStyle);
        readButton(panel, button, buttonStyle, i, col);
        col += buttonStyle.getSpan();
      }
    }
  }


  /*
  public static void main(String[] args) throws JDOMException, IOException {
    System.out.println(readThemes(new File(args[0])));
  }
  */
}
