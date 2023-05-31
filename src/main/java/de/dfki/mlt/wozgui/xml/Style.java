package de.dfki.mlt.wozgui.xml;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.SwingConstants;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class Style {
  private static class ColorAdapter extends XmlAdapter<String, Color> {
    @Override
    public Color unmarshal(String v) throws Exception {
      return new Color(Integer.parseInt(v, 16));
    }

    @Override
    public String marshal(Color v) throws Exception {
      return v == null ? null :Integer.toHexString(v.getRGB());
    }
  }

  @XmlAttribute
  String font = null;

  @XmlAttribute
  String align = null;

  @XmlJavaTypeAdapter(ColorAdapter.class)
  @XmlAttribute
  Color fgcolor = null;

  @XmlJavaTypeAdapter(ColorAdapter.class)
  @XmlAttribute
  Color bgcolor = null;

  @XmlAttribute
  Integer fontstyle = null;

  @XmlAttribute
  Integer fontsize = null;

  @XmlAttribute
  Integer buttonwidth = null;

  @XmlAttribute
  Integer buttonheight = null;

  @XmlAttribute
  Integer span = null;

  @XmlAttribute
  Integer inset = null;

  void inherit(Style parent) {
    if (font == null) font = parent.font;
    if (align == null) align = parent.align;
    if (fgcolor == null) fgcolor = parent.fgcolor;
    if (bgcolor == null) bgcolor = parent.bgcolor;

    if (fontstyle == null) fontstyle = parent.fontstyle;
    if (fontsize == null) fontsize = parent.fontsize;
    if (buttonwidth == null) buttonwidth = parent.buttonwidth;
    if (buttonheight == null) buttonheight = parent.buttonheight;
    if (span == null) span = parent.span;
    if (inset == null) inset = parent.inset;
  }

  public Font getFont() {
    String name = font == null ? "SansSerif" : font;
    int style = fontstyle == null ? Font.PLAIN : fontstyle;
    int size = fontsize == null ? 12 : fontsize;
    return new Font(name, style, size);
  }

  public Dimension getDimension() {
    int span = getSpan();
    int inset = getInset();
    int width = buttonwidth == null ? 300 : buttonwidth;
    int height = buttonheight == null ? 30 : buttonheight;
    width = width * span + (inset * (span - 1));
    return new Dimension(width, height);
  }

  public int getInset() { return inset == null ? 2 : inset; }

  public int getSpan() { return span == null ? 1 : span; }

  public Color getFgColor() { return fgcolor; }

  public Color getBgColor() { return bgcolor; }

  public int getAlignment() {
    if (align == null) return SwingConstants.CENTER;
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