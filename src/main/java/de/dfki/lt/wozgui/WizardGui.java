// =================================================================
// Copyright (C) 2010-2014 DFKI
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation; either version 2.1 of
// the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNUf
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
// 02111-1307, USA.
// =================================================================

package de.dfki.lt.wozgui;

import static de.dfki.lt.wozgui.PanelDefinitions.FREE_RESPONSE_ACTION;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.log4j.Logger;
import org.jdom2.JDOMException;
import org.xml.sax.InputSource;

public class WizardGui extends JFrame {

  private static final long serialVersionUID = 1L;
  public static final String GUI_TITLE = "PAL WIZARD OF OZ INTERFACE";
  public static final String GUI_FONT = "SansSerif";
  public static final String GUI_FONT_STATUSBAR = "Arial";
  public static final int GUI_MESSAGE_FONTSIZE = 15;
  public static final int GUI_FONTSIZE_TEXT = 24;
  public static final int GUI_FONTSIZE_BUTTONS = 12;
  public static final int GUI_FONTSIZE_STATUSBAR = 12;
  // private int guiTextHeight = 150;
  public static final int GUI_BUTTON_MAX_LENGTH = 33;
  public static final int SMALLTALK_BUTTONWIDTH = 150;
  public static final int SMALLTALK_THEME_BUTTONWIDTH = 200;

  public static int MAX_MESSAGE_LENGTH = 4000;

  private static final Font MONITOR_FONT = new Font(GUI_FONT, Font.PLAIN,
      GUI_FONTSIZE_TEXT);

  static Dimension randomButtonSmallTalkSize = new Dimension(110, 30);
  static Dimension buttonSmallTalkThemeSize = new Dimension(
      SMALLTALK_THEME_BUTTONWIDTH, 30);

  private static final String MOD_PATH = "alize-gui/";

  private static final String IMAGE_FILE_PATH = "icons/";
  private static final String BUTTON_RESOURCE_DIR = "buttonPanels/";
  private static final String SMALLTALK_RESOURCE_DIR = "smalltalk/";

  private static final Logger logger = Logger.getLogger("WizardGUI");


  private static File getResource(String moduleDir, String filePath, String image) {
    return new File(new File(moduleDir, filePath), image);
  }

  public enum Conversant {
    Robot, User, Debug, Info, Asr, System, Misc;

    private final Color color;

    Conversant() {
      final Color conversantColor[] =
          { Color.blue, Color.red, Color.gray, Color.magenta,
              new Color(0x1d, 0xd2, 0x0c), Color.green, Color.orange };
      color = conversantColor[ordinal()];
    }
  };

  public enum Activity {
    NONE, INTRODUCTION, SWITCH, QUIZ, // DANCE,
    SORTING, // , CLOSING
    INTEGRATED
  };

  /*
   * private Listener<String>[] _sysListeners = new
   * Listener<String>[Activity.values().length]; private Listener<String>[]
   * _userListeners = new Listener<String>[Activity.values().length];
   */

  /**
   * Components of the main window: text field, toolbar, game panel, status bar
   */
  JTextPane textPane;
  private SystemToolBar systemToolBar = null;
  // Container content ;
  private JTabbedPane activityPane;
  private JLabel statusBar;

  /** Panels for the different Activities (see Activity enum) */
  private JComponent[][] activityPanels =
      new JComponent[Activity.values().length][];

  /** Panel to activate when smalltalk is active */
  private JPanel smallTalkInput;
  // Sub-panels of the small talk conversation templates
  private JComponent smalltalkButtonsPanel;
  private JComponent smalltalkThemesPanel;
  private JComponent smalltalkSubThemesPanel;
  private JComponent smalltalkRandomPanel;

  private JTextField freeSmalltalk;

  // To send commands as if they came from the robot, to inject tts speech
  // or motion, or other speech acts directly, like the robot "brain"
  private Listener<String> _robotListener;

  /** Which activity is currently active */
  public int currentActivity = -1;

  /** Which language is currently active */
  private String _lang;

  /** Still in, the button is currently not shown */
  // private Listener<String> asrOnFileListener;

  UserQuestionDialog uq;
  private Listener<String> _userListener;

  // Rescale a image into JLabel
  private static BufferedImage scale(BufferedImage src, int w, int h) {
    int type = BufferedImage.TYPE_INT_ARGB;
    BufferedImage dst = new BufferedImage(w, h, type);
    Graphics2D g2 = dst.createGraphics();
    // Fill background for scale to fit.
    // g2.setBackground(UIManager.getColor("Panel.background"));
    g2.setBackground(new Color(0, 0, 0, 0));
    g2.clearRect(0, 0, w, h);
    double xScale = (double) w / src.getWidth();
    double yScale = (double) h / src.getHeight();
    // Scaling options:
    // Scale to fit - image just fits in label.
    double scale = Math.min(xScale, yScale);
    // Scale to fill - image just fills label.
    // double scale = Math.max(xScale, yScale);
    int width = (int) (scale * src.getWidth());
    int height = (int) (scale * src.getHeight());
    int x = (w - width) / 2;
    int y = (h - height) / 2;
    g2.drawImage(src, x, y, width, height, null);
    g2.dispose();
    return dst;
  }

  public static ImageIcon loadIcon(String image, int size) {
    if (image == null)
      return null;
    BufferedImage img = imageResize(image, size);
    return img == null ? null : new ImageIcon(img);
  }

  public static BufferedImage imageResize(String image, int size) {
    if (image == null)
      return null;
    BufferedImage myPicture = null;
    try {
      myPicture = ImageIO.read( ClassLoader.getSystemResourceAsStream(
          IMAGE_FILE_PATH + "/" + image));
    } catch (IOException e) {
      logger.error("ImageResize() can't find file: " + IMAGE_FILE_PATH + image);
      e.printStackTrace();
      System.exit(1);
    }
    BufferedImage scaled = scale(myPicture, size, size);

    return scaled;
  }

  // private List<Pair<String, File>> audioChunks = new ArrayList<Pair<String,
  // File>>();
  //
  // private File _currentDir = new File(".");

  public void errorDialog(String string) {
    JOptionPane.showMessageDialog(this, string, "Error",
        JOptionPane.ERROR_MESSAGE);
  }

  public void warningDialog(String string) {
    JOptionPane.showMessageDialog(this, string, "Warning",
        JOptionPane.WARNING_MESSAGE);
  }

  public void infoDialog(String string) {
    JOptionPane.showMessageDialog(this, string, "Information",
        JOptionPane.INFORMATION_MESSAGE);
  }

  protected void reportProblem(String msg) {
    warningDialog(msg);
  }

  /** For test purposes only */
  private static Listener<String> makeStringListener(final WizardGui g,
      final Conversant c) {
    return new Listener<String>() {
      @Override
      public void receive(String event, String message) {
        g.setMessage(c, "[" + event + "," + message + "]");
      }

      @Override
      public void receive(String event) {
        g.setMessage(c, "[" + event + "]");
      }
    };
  }

  /**
   * constructor
   */
  public WizardGui() {
    super(GUI_TITLE);
  }

  private JComponent newSmallTalkButtonPanel(int nrows) {
    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(nrows, 1, 0, 3));
    panel.setAutoscrolls(false);
    return panel;
  }

  private JComponent newVerticalButtonPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(GridButtonPanel.WITH_SCROLLBARS ? new BoxLayout(panel,
        BoxLayout.Y_AXIS) : new FlowLayout(FlowLayout.LEFT));
    // new BoxLayout(result, BoxLayout.Y_AXIS));
    panel.setAutoscrolls(false);
    return panel;
  }

  // private JComponent newVerticalButtonPanel(JComponent compo) {
  // /*
  // JPanel panel = new JPanel(new GridBagLayout());
  // GridBagConstraints gc = new GridBagConstraints();
  // gc.gridx = 0;
  // gc.gridy = 0;
  // gc.anchor = GridBagConstraints.NORTHWEST;
  // panel.add(compo, gc);
  // */
  // //panel.setLayout(new B.
  // // new BoxLayout(result, BoxLayout.Y_AXIS));
  // JPanel panel = new JPanel();
  // panel.setLayout(GridButtonPanel.WITH_SCROLLBARS
  // ? new BoxLayout(panel, BoxLayout.Y_AXIS)
  // : new FlowLayout(FlowLayout.LEFT));
  // panel.add(compo);
  // panel.setAutoscrolls(false);
  // return panel;
  // }

  private JPanel createSmallTalkPanel(final Listener<String> listener) {

    smalltalkButtonsPanel = newSmallTalkButtonPanel(20);
    smalltalkSubThemesPanel = newSmallTalkButtonPanel(20);
    smalltalkThemesPanel = newSmallTalkButtonPanel(20);
    smalltalkButtonsPanel.setName("smalltalkButtonsPanel");
    smalltalkSubThemesPanel.setName("smalltalkSubThemesPanel");
    smalltalkThemesPanel.setName("smalltalkThemesPanel");

    freeSmalltalk = new JTextField();

    ActionListener robotActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _robotListener.receive(freeResponseAction(e.getActionCommand()));
      }
    };
    freeSmalltalk.addActionListener(robotActionListener);

    JPanel smallTalkPanel = new JPanel(new BorderLayout());
    smalltalkRandomPanel = new JPanel(new FlowLayout());

    JPanel smallTalkSelectPanel = new JPanel(new BorderLayout());

    // first combine the themes and subThemes panel
    // then add the buttons next to that.
    JSplitPane smalltalkThemeSplitter =
        new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, smalltalkThemesPanel,
            smalltalkSubThemesPanel);
    JScrollPane themesscrolled = new JScrollPane(smalltalkThemeSplitter);

    smalltalkThemeSplitter.setDividerLocation(SMALLTALK_THEME_BUTTONWIDTH + 10);
    smalltalkThemeSplitter.setResizeWeight(1);
    smalltalkThemeSplitter.setDividerSize(6);
    smalltalkThemeSplitter.setBorder(null);
    smalltalkThemeSplitter.setAutoscrolls(false);

    smallTalkSelectPanel.add(themesscrolled);

    JScrollPane buttonsscrolled = new JScrollPane(smalltalkButtonsPanel);
    // buttonsscrolled.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    // buttonsscrolled.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

    JSplitPane smalltalkSplitter =
        new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, smallTalkSelectPanel,
            buttonsscrolled);

    smalltalkSplitter.setDividerLocation(2 * SMALLTALK_THEME_BUTTONWIDTH + 30);
    smalltalkSplitter.setResizeWeight(1);
    smalltalkSplitter.setDividerSize(6);
    smalltalkSplitter.setBorder(null);
    smalltalkSplitter.setAutoscrolls(false);

    smallTalkPanel.add(smalltalkSplitter, BorderLayout.CENTER);

    // addSmalltalkThemes("it"); // done by language preload!

    return smallTalkPanel;
  }

  /**
   *
   * @param system
   *          Listener for SystemToolbar buttons: pass system commands to the
   *          main loop like start/stop session/activity, tts on/off, etc.
   *
   * @param user
   *          To send commands as if they came from the user, what the wizard
   *          perceives and sends on to the system. In an autonomous system,
   *          that channel transports what the recognition has perceived.
   *
   * @param robot
   *          To send commands as if they came from the robot, to inject tts
   *          speech or motion, or other speech acts directly, like the robot
   *          "brain"
   */
  public void createGui(Listener<String> system, Listener<String> user,
  // StringEventListener asrOnFileListener,
      Listener<String> robot) {
    Thread currentThread = Thread.currentThread();
    ClassLoader old = currentThread.getContextClassLoader();
    try {
      currentThread.setContextClassLoader(ClassLoader.getSystemClassLoader());
      this._userListener = user;
      this._robotListener = robot;
      createGui2(system, user);
    } catch (Exception ex) {
      logger.error(ex.getMessage());
      ex.printStackTrace();
      System.exit(2);
    } finally {
      currentThread.setContextClassLoader(old);
    }
  }

  private void createActivityPanel(Activity activity,
      Listener<String>... listeners) throws JDOMException, IOException {
    int ordinal = activity.ordinal();
    String actName = activity.name().toLowerCase();
    activityPanels[ordinal] = new JComponent[2];
    File[] panelDefinitions =
        {
            getResource(MOD_PATH, BUTTON_RESOURCE_DIR, actName
                + "UserActions.xml"),
            getResource(MOD_PATH, BUTTON_RESOURCE_DIR, actName
                + "SysActions.xml") };
    int i = 0;
    for (File f : panelDefinitions) {
      InputSource is = null;
      try {
        is = new InputSource();
        is.setByteStream(new FileInputStream(f));
      } catch (FileNotFoundException ex) {
        is = null;
      }
      activityPanels[ordinal][i] =
          (is != null ? new GridButtonPanel(listeners[i], is) : null);
      ++i;
    }
  }

  /**
   * Create all the necessary user interface units for the wizard GUI, put them
   * into the main frame and display it. The listeners transport the signals to
   * the outside world
   *
   * @param bl
   *          signals from the tool bar
   * @param navListener
   *          signals from the "robot remote control panel"
   * @throws IOException
   * @throws JDOMException
   */
  public void createGui2(final Listener<String> sysListener,
      Listener<String> userListener) throws JDOMException, IOException {
    try {
      String lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
      // BB replaced 'getSystemLookAndFeelClassName'
      // so there are also colors in windows ;-)
      // BK: Java UI look like shit anyway, so it doesn't really matter
      UIManager.setLookAndFeel(lookAndFeel);
    } catch (ClassNotFoundException e) {
      // well, we're content with everything we get
    } catch (InstantiationException e) {
      // well, we're content with everything we get
    } catch (IllegalAccessException e) {
      // well, we're content with everything we get
    } catch (UnsupportedLookAndFeelException e) {
      // well, we're content with everything we get
    }

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // ////////////////////////////////////////////////////////////////////
    // CREATE ACTIVITY PANELS
    // ////////////////////////////////////////////////////////////////////

    // Empty panel for NONE
    activityPanels[Activity.NONE.ordinal()] = new JComponent[1];
    activityPanels[Activity.NONE.ordinal()][0] = newVerticalButtonPanel();

    // a panel for smalltalk buttons
    smallTalkInput = createSmallTalkPanel(_robotListener);

    // TODO: PUT INTO TABBED PANE?
    for (Activity activity : Activity.values()) {
      createActivityPanel(activity, userListener, _robotListener);
      // _userListeners[activity.ordinal()],
      // _sysListeners[activity.ordinal()]);
    }

    // this arranges that the panels stay on the same position (vertical)
    // despite of the amount of text in the textpane
    /*
     * for (JComponent activity : activityPanels) { if (activity != null) {
     * activity.setMinimumSize(new Dimension(this.getWidth(), 500));
     * activity.setMaximumSize(new Dimension(this.getWidth(), 500));
     * activity.setPreferredSize(new Dimension(this.getWidth(), 500)); } }
     */

    // ////////////////////////////////////////////////////////////////////
    // CREATE MAIN WINDOW COMPONENTS: TOOLBAR, TEXT WINDOW AND STATUS BAR
    // ////////////////////////////////////////////////////////////////////

    uq = new UserQuestionDialog(this, "Enter a Quiz question");
    uq.pack();

    // Create the Message Monitor window
    textPane = new JTextPane();
    textPane.setMinimumSize(new Dimension(0, 160));
    textPane.setFont(MONITOR_FONT);
    textPane.setEditable(false);

    JScrollPane outputPane = new JScrollPane();
    outputPane.setViewportView(textPane);

    // dialoguePane contains the (scrollable) text output window and the motion
    // buttons
    JPanel dialoguePane = new JPanel();
    dialoguePane.setLayout(new BorderLayout());
    dialoguePane.add(outputPane, BorderLayout.CENTER);

    // motion buttons on the right
    InputSource is = new InputSource();
    is.setByteStream(new FileInputStream(getResource(MOD_PATH,
        BUTTON_RESOURCE_DIR, "motionActions.xml")));
    dialoguePane
        .add(new GridButtonPanel(_robotListener, is), BorderLayout.EAST);

    // Create the status bar
    statusBar = new JLabel("dialogue state");
    statusBar.setFont(new Font(GUI_FONT_STATUSBAR, Font.PLAIN,
        GUI_FONTSIZE_STATUSBAR));

    JPanel inner = new JPanel(new BorderLayout());
    // Add random buttons panel of smalltalk
    smalltalkRandomPanel.setMaximumSize(new Dimension(this.getWidth(), 50));
    smalltalkRandomPanel.setMinimumSize(new Dimension(this.getWidth(), 50));
    smalltalkRandomPanel.setPreferredSize(new Dimension(this.getWidth(), 50));
    inner.add(smalltalkRandomPanel, BorderLayout.NORTH);
    /*
     * JSplitPane smalltalkadded = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
     * dialoguePane, smalltalkRandomPanel);
     *
     * smalltalkadded.setResizeWeight(1.0); smalltalkadded.setDividerSize(6);
     * smalltalkadded.setBorder(null);
     */

    activityPane = new JTabbedPane();

    inner.add(activityPane, BorderLayout.CENTER);

    // Create the outer split pane that contains the inner split pane and
    // the upper dialogue window on the upper side of the window
    JSplitPane outer =
        new JSplitPane(JSplitPane.VERTICAL_SPLIT, dialoguePane, inner);
    // outer.setDividerLocation(guiTextHeight);
    outer.setResizeWeight(0.0);
    outer.setDividerSize(6);
    outer.setBorder(null);
    outer.setAutoscrolls(false);

    // Configure the container
    Container content = getContentPane();
    content.setLayout(new BorderLayout());
    content.add(outer, BorderLayout.CENTER);
    content.add(statusBar, BorderLayout.SOUTH);

    systemToolBar =
        new SystemToolBar(this, JToolBar.HORIZONTAL, new Listener<String>() {
          @Override
          public void receive(String event) {
            receive(event, "");
          }

          @Override
          public void receive(String event, String message) {
            sysListener.receive(event, message);
          }
        }, 40);
    add(systemToolBar, BorderLayout.NORTH);

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    // try to infer if dual monitors are used, if so width is reduced
    if (screenSize.width > 2560) {
      screenSize.width = screenSize.width / 2;
    }
    if (screenSize.height > 1440) {
      screenSize.height = screenSize.height / 2;
    }

    screenSize.width *= 0.7;
    screenSize.height *= 0.8;
    content.setPreferredSize(screenSize);

    ((JComponent) content).setBorder(BorderFactory.createLineBorder(Color.blue,
        2));

    pack();
    // setLocationByPlatform(true);
    setVisible(true);
  }

  /**
   * Go through all the active user actions and highlight those whose emitted
   * message matches regex
   *
   * @param regex
   */
  public void activateUserActions(String regex) {
    if (activityPane.getComponentCount() > 0) {
      GridButtonPanel p = (GridButtonPanel) activityPane.getComponent(0);
      if (p != null)
        p.colorButtons(regex);
    }
  }

  private void updateActivityPane(int what) {
    String[] label = { "User", "System" };
    JComponent[] compo = activityPanels[what];
    activityPane.removeAll();
    int i = 0;
    for (JComponent c : compo) {
      if (c != null)
        activityPane.addTab(label[i], c);
      ++i;
    }
    activityPane.addTab("Smalltalk", null, smallTalkInput, "smalltalk buttons");
    // activitytab.setTopComponent(compo);
    // outer.setBottomComponent(compo);
    // pack();
    repaint();
  }

  /**
   * Set the icon state of the current activity from idle to running and switch
   * to the appropriate activity panel. All other activities icons are set to
   * idle.
   */
  public void switchToActivity(Activity activity) {
    // TODO: maybe play safe and set all to idle first.
    if (currentActivity >= 0) {
      systemToolBar.activityState(currentActivity, 0);
    }
    currentActivity = activity.ordinal();
    updateActivityPane(currentActivity);
    selectSmalltalkTheme(activity.toString());

    systemToolBar.activityState(currentActivity, 1);
  }

  /**
   * Set the icon state of the current activity from running to paused and
   * switch to the "none" activity panel
   */
  public void pauseCurrentActivity() {
    systemToolBar.activityState(currentActivity, 2);
  }

  /**
   * Set the icon state of the current activity from paused to running and
   * switch to the back to the activity panel
   */
  public void resumeCurrentActivity() {
    systemToolBar.activityState(currentActivity, 1);
  }

  boolean debugFlag = false;

  /*
   * Show the last conversant's utterance in the display
   */
  public void setMessage(Conversant user, String message) {
    int len = 0;

    if (message == null
        || (message.trim().isEmpty() || message.endsWith("(empty)")))
      return;
    try {
      StyledDocument doc = (StyledDocument) textPane.getDocument();
      Style style = doc.addStyle("StyleName", null);
      StyleConstants.setFontFamily(style, GUI_FONT);
      StyleConstants.setFontSize(style, GUI_MESSAGE_FONTSIZE);

      if (message.startsWith("'"))
        message = message.substring(1, message.length() - 1);

      if (message.startsWith("orientation|")) {
        message = message.substring(12, message.length());
        systemToolBar.setTabletOrientation(message);
        return;
      }

      if (message.startsWith("[answer|")) {
        message = message.substring(8, message.length() - 1);
        message = message.replace("Answer: ", "");
        String userAnswer = "provide(answer, number=1)";
        switch (message) {
          case "A":
            userAnswer = userAnswer.replace("=1", "=1");
            break;
          case "B":
            userAnswer = userAnswer.replace("=1", "=2");
            break;
          case "C":
            userAnswer = userAnswer.replace("=1", "=3");
            break;
          case "D":
            userAnswer = userAnswer.replace("=1", "=4");
            break;
          default:
            userAnswer = "";
            break;
        }
        if (userAnswer.length() > 0) {
          _userListener.receive(userAnswer);
        }
      }

      if (user.equals("cls")) {
        doc.remove(0, doc.getLength());
      } else {
        StyleConstants.setForeground(style, user.color);
        if (message.length() > MAX_MESSAGE_LENGTH) {
          message = message.substring(0, MAX_MESSAGE_LENGTH) + " ..........";
        }
        doc.insertString(doc.getLength(), user + ": " + message + "\n", style);
      }
      len = doc.getLength();
      textPane.setCaretPosition(len);

    } catch (BadLocationException e) {
      e.printStackTrace();
    }
  }

  /**
   * set a system utterance in the Gui
   *
   * A method that was bound to an URBI call in the Aliz-E system
   *
   * TODO: Maybe change when we know how to compute expectations
   */
  public void setSystemUtterance(String text) {
    if (text.equals("NEW"))
      return;

    for (Conversant c : Conversant.values()) {
      int prefixLen = c.toString().length() + 1;
      if (text.length() > prefixLen
          && text.charAt(prefixLen - 1) == ':'
          && (text.startsWith(c.toString()) || text.startsWith(c.toString()
              .toUpperCase()))) {
        setMessage(c, text.substring(prefixLen));
        return;
      }
    }
    setMessage(Conversant.Robot, text);
  }

  /**
   * Set the status of the control buttons according to the status we are in. If
   * the robot is idle, both should be false. If a game was loaded, playing must
   * be true, if it was started, both should be true
   *
   * @param playing
   *          true, if not idle
   * @param started
   *          true if the game was started
   */
  public void setStateOfSession(boolean active) {
    systemToolBar.setStateOfControlButtons(active);
  }

  /** Signal that the system language has changed. Take appropriate actions */
  public void setLanguage(String lang) {
    _lang = lang;
    addSmalltalkThemes(_lang);
  }

  private static JButton styleButton(JButton result, int maxLength) {
    String text = result.getText();
    String shortText =
        (text.length() > maxLength) ? text.substring(0, maxLength) + "..."
            : text;
    result.setText(shortText);
    result.setToolTipText(text);
    result.setFont(new Font(GUI_FONT, Font.PLAIN, GUI_FONTSIZE_BUTTONS));
    result.setEnabled(true);
    result.setAlignmentX(Component.LEFT_ALIGNMENT);
    return result;
  }

  public static JButton finalizeButton(JButton result, int maxLength,
      Dimension size) {
    result = styleButton(result, maxLength);
    result.setPreferredSize(size);
    result.setMinimumSize(size);
    result.setMaximumSize(size);
    result.setFont(new Font(GUI_FONT, Font.PLAIN, GUI_FONTSIZE_BUTTONS));
    // result.setMaximumSize(new Dimension(290, 40));
    return result;
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

  /**
   * Visualization of ASR and parser data * public void createNluViewer(String
   * relations) { nluViewer = new MainFrame(relations);
   * tabbedPane.add("NLU output", nluViewer); nluViewer.setVisible(true); }
   */

  /*
   * ###################################################################### Play
   * available audio chunks through the ASR
   * ###################################################################### //
   */
  //
  // private boolean readChunkMap(File toRead, File currentDir)
  // throws IOException {
  // audioChunks.clear();addSmalltalkThemes
  // BufferedReader in = new BufferedReader(new InputStreamReader(
  // new FileInputStream(toRead), "UTF-8"));
  // String line;
  // while ((line = in.readLine()) != null) {
  // String[] fields = line.split("\\t");
  // if (fields.length < 3) {
  // warningDialog("Wrong field entry in chunk file: " + line);
  // return false;
  // }
  // File chunkFile = new File(_currentDir, fields[0]);
  // if (!chunkFile.exists()) {
  // throw new FileNotFoundException(fields[0]);
  // }
  // audioChunks.add(new Pair<String, File>(fields[2], chunkFile));
  // }
  // return true;
  // }

  // private void fillAudioChunks() {
  // JFileChooser fc = new JFileChooser();
  // fc.setCurrentDirectory(_currentDir);
  // int returnVal = -1;
  // boolean success = false;
  // do {
  // returnVal = fc.showOpenDialog(this);
  // if (returnVal == JFileChooser.APPROVE_OPTION) {
  // // update current directory
  // _currentDir = fc.getSelectedFile().getParentFile();
  // // get the object read from this file
  // File toRead = fc.getSelectedFile();
  // if (!toRead.exists()) {
  // errorDialog("No such File: " + toRead);
  // success = false;
  // } else {
  // try {
  // success = readChunkMap(toRead, _currentDir);
  // } catch (IOException ex) {
  // errorDialog("File content of "
  // + toRead
  // + " could not be read:\n"
  // + ((ex.getCause() != null) ? ex.getCause()
  // .toString() : ex.toString()));
  // }
  // }
  // }
  // } while (!success && returnVal != JFileChooser.CANCEL_OPTION);
  // if (!success)
  // return;
  // // populate the panel with buttons, put it into the content area, and
  // // do a redraw
  // smalltalkButtonsPanel.removeAll();
  // for (Pair<String, File> chunk : audioChunks) {
  // JComponent component;
  // AsrFileAction action = new AsrFileAction(chunk.getFirst(), chunk
  // .getSecond().getAbsolutePath(), asrOnFileListener);
  // component = finalizeButton(new JButton(action));
  // smalltalkButtonsPanel.add(component);
  // }
  // Container component = this.getContentPane();
  // // component.add(audioChunksPanel, BorderLayout.EAST);
  // smalltalkButtonsPanel.setPreferredSize(new Dimension(300, this
  // .getHeight()));
  // smalltalkButtonsPanel.setMaximumSize(new Dimension(300, this
  // .getHeight()));
  // smalltalkButtonsPanel.setAutoscrolls(false);
  // smalltalkButtonsPanel.setVisible(true);
  // component.validate();
  // }
  //
  // private void removeAudioChunksPanel() {
  // smalltalkButtonsPanel
  // .setMaximumSize(new Dimension(0, this.getHeight()));
  // smalltalkButtonsPanel.setVisible(false);
  // Container component = this.getContentPane();
  // component.validate();
  // }

  // ////////////////////////////////////////////////////////////////////
  // Small Talk panel utility methods
  // ////////////////////////////////////////////////////////////////////

  static String freeResponseAction(String motion, String text) {
    if (text.startsWith("@"))
      return text.substring(1);
    StringBuilder sb = new StringBuilder();
    sb.append(FREE_RESPONSE_ACTION);
    if (motion != null)
      sb.append(", motion=\"").append(motion).append("\"");
    if (text != null)
      sb.append(", string=\"").append(text).append("\"");
    sb.append(")");
    return sb.toString();
  }

  static String freeResponseAction(String textAndMotion) {
    int firstBar = textAndMotion.indexOf('|');
    if (firstBar >= 0) {
      return freeResponseAction(textAndMotion.substring(firstBar + 1),
          textAndMotion.substring(0, firstBar));
    }
    return freeResponseAction(null, textAndMotion);
  }

  @SuppressWarnings("serial")
  private class TtsButtonAction extends AbstractAction {
    private String _text;
    private String _motion;

    public TtsButtonAction(String text, String motion) {
      super(text);
      _text = text;
      _motion = motion;
    }

    public void actionPerformed(ActionEvent e) {
      //Usermodel _activeUser = Usermodels.getUsermodels().getActiveModel();
      try {
        //String adaptText = Utils.adaptText(_text, _activeUser.getGivenName(),
        //    _activeUser.getFavoriteColor(), _activeUser.getHobby());
        String adaptText = _text;

        _robotListener.receive(freeResponseAction(_motion, adaptText));
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  private static final Color NotSelectedBorder = Color.black;
  private static final Color NextActionBorder = Color.green;
  private static final int NextActionBorderWidth = 3;

  @SuppressWarnings("serial")
  private abstract class AbstractThemeAction extends AbstractAction {
    protected List<Action> _utterances;

    protected AbstractThemeAction(String text) {
      super(text);
    }

    protected void initUtterances(List<Pair<String, String>> utts) {
      _utterances = new ArrayList<Action>(utts.size());
      for (Pair<String, String> s : utts) {
        _utterances.add(new TtsButtonAction(s.getFirst(), s.getSecond()));
      }
    }
  }

  @SuppressWarnings("serial")
  private class ThemeAction extends AbstractThemeAction {
    private List<ThemesReader.Theme> _subthemes;
    private boolean _issubtheme;

    public ThemeAction(String text, List<Pair<String, String>> utts,
        List<ThemesReader.Theme> subthemes) {
      this(text, utts);
      _issubtheme = false;
      _subthemes = subthemes;
    }

    public ThemeAction(String text, List<Pair<String, String>> utts) {
      super(text);
      initUtterances(utts);
      _issubtheme = true;
    }

    public void actionPerformed(ActionEvent e) {
      JButton clickedButton = (JButton) e.getSource();
      String name = clickedButton.getParent().getName();
      logger.info(name);
      // fillSmallTalkPanel(_utterances);

      if (name.compareTo(smalltalkThemesPanel.getName()) == 0) {
        for (Component c : smalltalkThemesPanel.getComponents()) {
          assert (c instanceof JButton);
          ((JButton) c).setBorder(BorderFactory
              .createLineBorder(NotSelectedBorder));
        }
      }
      if (name.compareTo(smalltalkSubThemesPanel.getName()) == 0) {
        for (Component c : smalltalkSubThemesPanel.getComponents()) {
          assert (c instanceof JButton);
          ((JButton) c).setBorder(BorderFactory
              .createLineBorder(NotSelectedBorder));
        }
      }
      clickedButton.setBorder(BorderFactory.createLineBorder(NextActionBorder,
          NextActionBorderWidth));

      if (!_issubtheme) {
        updateSubThemes();
      }
      fillPanelWithButtons(smalltalkButtonsPanel, _utterances);
    }

    private void updateSubThemes() {
      smalltalkSubThemesPanel.removeAll();
      smalltalkSubThemesPanel.repaint();
      for (ThemesReader.Theme subtheme : _subthemes) {
        JButton bt =
            new JButton(new ThemeAction(subtheme.name, subtheme.utterances));
        bt =
            finalizeButton(bt, GUI_BUTTON_MAX_LENGTH, buttonSmallTalkThemeSize);
        smalltalkSubThemesPanel.add(bt);
      }
      smalltalkSubThemesPanel.repaint();
      smalltalkSubThemesPanel.validate();
    }
  }

  @SuppressWarnings("serial")
  private class ThemeRandomAction extends AbstractThemeAction {
    private Random _random;
    private List<Pair<String, String>> sourceUtterances;

    public ThemeRandomAction(String text, List<Pair<String, String>> utts) {
      super(text);
      initUtterances(utts);
      sourceUtterances = utts;
      _random = new Random(new Date().getTime());
    }

    public void actionPerformed(ActionEvent e) {
      int i = _random.nextInt(_utterances.size());
      _utterances.get(i).actionPerformed(e);
      _utterances.remove(i);
      if (_utterances.size() == 0) {
        initUtterances(sourceUtterances);
      }
    }
  }

  public void addSmalltalkThemes(String lang) {

    List<ThemesReader.Theme> themes = null;

    smalltalkThemesPanel.removeAll();
    smalltalkThemesPanel.repaint();
    smalltalkSubThemesPanel.removeAll();
    smalltalkSubThemesPanel.repaint();
    smalltalkButtonsPanel.removeAll();
    smalltalkButtonsPanel.repaint();
    smalltalkRandomPanel.removeAll();
    smalltalkRandomPanel.repaint();

    JLabel text = new JLabel();
    text.setText("send random text :      ");
    smalltalkRandomPanel.add(text);

    try {
      int underScorePos = lang.indexOf('_');
      String suffix = (underScorePos < 0) ? "" : lang.substring(underScorePos);

      File st =
          getResource(MOD_PATH, SMALLTALK_RESOURCE_DIR, "woz-smalltalk-"
              + lang.toLowerCase().substring(0, 3) + suffix + ".xml");
      logger.info("Reading SmallTalk file " + st);
      InputSource is = new InputSource();
      is.setByteStream(new FileInputStream(st));
      themes = ThemesReader.readThemes(is);
      logger.info("Themes: " + themes.size());
    } catch (Exception ex) {
      errorDialog(ex.getMessage());
    }
    if (themes != null) {
      for (ThemesReader.Theme theme : themes) {
        if (theme.isRandom) {
          JButton b =
              new JButton(new ThemeRandomAction(theme.name, theme.utterances));
          b =
              finalizeButton(b, GUI_BUTTON_MAX_LENGTH,
                  randomButtonSmallTalkSize);
          smalltalkRandomPanel.add(b);
        } else {
          JButton b =
              new JButton(new ThemeAction(theme.name, theme.utterances,
                  theme.subthemes));
          b =
              finalizeButton(b, GUI_BUTTON_MAX_LENGTH, buttonSmallTalkThemeSize);
          smalltalkThemesPanel.add(b);
        }
      }
    }
    smalltalkThemesPanel.repaint();
    smalltalkThemesPanel.validate();
    smalltalkSubThemesPanel.repaint();
    smalltalkSubThemesPanel.validate();
    smalltalkRandomPanel.repaint();
    smalltalkRandomPanel.validate();

    freeSmalltalk.setBackground(Color.lightGray);
    freeSmalltalk.setText("");
    freeSmalltalk.setPreferredSize(new Dimension(SMALLTALK_BUTTONWIDTH, 30));
    freeSmalltalk.setMinimumSize(new Dimension(SMALLTALK_BUTTONWIDTH, 30));
    freeSmalltalk.setMaximumSize(new Dimension(SMALLTALK_BUTTONWIDTH, 30));
    smalltalkButtonsPanel.add(freeSmalltalk);

  }

  public void fillPanelWithButtons(JComponent owner, List<Action> utterances) {
    owner.removeAll();
    this.getContentPane().validate();
    owner.repaint();
    // owner.setLayout(new GridLayout(utterances.size(),1,0,3));

    // Put button is for all utterance
    for (Action s : utterances) {
      JButton b = styleButton(new JButton(s), 200);
      owner.add(b);
    }
    owner.setPreferredSize(null);
    owner.setMaximumSize(null);
    owner.setAutoscrolls(false);
    owner.setVisible(true);
    this.getContentPane().validate();

    // Put all buttons in an array and determine max width
    List<JButton> l = new ArrayList<JButton>(utterances.size());
    // int maxWidth = 0;
    for (Component c : owner.getComponents()) {
      assert (c instanceof JButton);
      JButton b = (JButton) c;
      // maxWidth = Math.max(b.getWidth(), maxWidth);
      l.add(b);
    }

    // Remove all buttons and put in with the same max width
    owner.removeAll();
    this.getContentPane().validate();

    // add text field of free response
    freeSmalltalk.setBackground(Color.lightGray);
    freeSmalltalk.setText("");
    freeSmalltalk.setPreferredSize(new Dimension(SMALLTALK_BUTTONWIDTH, 30));
    freeSmalltalk.setMinimumSize(new Dimension(SMALLTALK_BUTTONWIDTH, 30));
    freeSmalltalk.setMaximumSize(new Dimension(SMALLTALK_BUTTONWIDTH, 30));
    owner.add(freeSmalltalk);

    for (JButton b : l) {
      // b.setPreferredSize(new Dimension(SMALLTALK_BUTTONWIDTH,
      // 10));//b.getHeight()));
      // b.setMinimumSize(new Dimension(SMALLTALK_BUTTONWIDTH,
      // 10));//b.getHeight()));
      // b.setMaximumSize(new Dimension(SMALLTALK_BUTTONWIDTH,
      // 10));//b.getHeight()));
      owner.add(b);
    }
    // component.add(audioChunksPanel, BorderLayout.EAST);
    // owner.setPreferredSize(new Dimension(SMALLTALK_BUTTONWIDTH,
    // this.getHeight()));
    // owner.setMaximumSize(new Dimension(SMALLTALK_BUTTONWIDTH,
    // this.getHeight()));
    // owner.setAutoscrolls(false);
    owner.setVisible(true);
    this.getContentPane().validate();

  }

  /*
   * public void setListeners(Activity activity, Listener<String> sysListener,
   * Listener<String> usrListener) { _sysListeners[activity.ordinal()] =
   * sysListener; _userListeners[activity.ordinal()] = usrListener; }
   */

  private void selectSmalltalkTheme(String what) {
    for (Component c : smalltalkThemesPanel.getComponents()) {
      assert (c instanceof JButton);
      JButton b = (JButton) c;
      if (b.getText().compareToIgnoreCase(what) == 0) {
        b.doClick();
      }
    }
  }

  public void quitProgram() {
    System.exit(0);
  }

  /** For test purposes only */
  public static void main(String[] args) {
    final WizardGui g = new WizardGui();
    Listener<String> usr = makeStringListener(g, Conversant.User), sys =
        makeStringListener(g, Conversant.Debug), robot =
        makeStringListener(g, Conversant.Robot);
    g.createGui(sys, usr, robot);
  }
}
