package de.dfki.mlt.wozgui;

import java.awt.Dimension;
import java.awt.Font;

public interface Constants {
  String GUI_TITLE = "PAL WIZARD OF OZ INTERFACE";
  String GUI_FONT = "Sans";
  String GUI_FONT_STATUSBAR = "Arial";
  int GUI_MESSAGE_FONTSIZE = 15;
  int GUI_FONTSIZE_TEXT = 24;
  int GUI_FONTSIZE_BUTTONS = 12;
  int GUI_FONTSIZE_STATUSBAR = 12;
  // private int guiTextHeight = 150;
  int GUI_BUTTON_MAX_LENGTH = 33;
  int SMALLTALK_BUTTONWIDTH = 150;
  int SMALLTALK_THEME_BUTTONWIDTH = 200;

  int MAX_MESSAGE_LENGTH = 4000;

  Font MONITOR_FONT = new Font(GUI_FONT, Font.PLAIN, GUI_FONTSIZE_TEXT);

  Font ICON_FONT = new Font(GUI_FONT, Font.BOLD, 9);

  Dimension randomButtonSmallTalkSize = new Dimension(110, 30);
  Dimension buttonSmallTalkThemeSize =
      new Dimension(SMALLTALK_THEME_BUTTONWIDTH, 30);

  public static final String FREE_RESPONSE_TAG = "free response";

}
