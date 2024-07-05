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

package de.dfki.mlt.wozgui;

import static de.dfki.mlt.wozgui.Constants.*;
import static de.dfki.mlt.wozgui.Utils.getResource;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.MatteBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.mlt.wozgui.xml.Activities;
import de.dfki.mlt.wozgui.xml.Activities.Activity;;

public class WizardGui extends JFrame {
  private static final long serialVersionUID = 1L;

  private static final Logger logger = LoggerFactory.getLogger("WizardGUI");

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


  @SuppressWarnings("serial")
  public class TtsButtonAction extends AbstractAction {
    private String _text;
    private String _motion;

    public TtsButtonAction(String text, String motion) {
      super(text);
      _text = text;
      _motion = motion;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      //Usermodel _activeUser = Usermodels.getUsermodels().getActiveModel();
      try {
        //String adaptText = Utils.adaptText(_text, _activeUser.getGivenName(),
        //    _activeUser.getFavoriteColor(), _activeUser.getHobby());
        String adaptText = _text;

        _robotListeners.receive(FREE_RESPONSE_TAG, _motion + "|" + adaptText);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

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

  private boolean motionButtonsVisible = true;

  /** Panels for the different Activities (see Activity enum) */
  private JComponent[][] activityPanels;

  /** Panel to activate when smalltalk is active */
  private SmalltalkPanel smallTalkInput;

  // To send commands as if they came from the robot, to inject tts speech
  // or motion, or other speech acts directly, like the robot "brain"
  private Receiver<String> _robotListeners;

  /** The directory where the top-level context is */
  public File configDir;

  String iconDir;
  String panelDir;
  String smalltalkDir;

  //public static final int Activity_NONE = 0;

  /** The list of activities */
  public List<Activity> activities;

  /** Which activity is currently active */
  public int currentActivity = -1;

  /** Which language is currently active */
  protected String _lang;

  /** Still in, the button is currently not shown */
  // private Listener<String> asrOnFileListener;

  UserQuestionDialog uq;
  private Receiver<String> _userListeners;
  private Receiver<String> _sysListeners;

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


  private BufferedImage imageResize(String image, int size) {
    if (image == null)
      return null;
    BufferedImage myPicture = null;
    try {
      myPicture = ImageIO.read(getResource(configDir, iconDir, image));
      //ClassLoader.getSystemResourceAsStream(iconDir + "/" + image)
    } catch (IOException e) {
      logger.error("ImageResize() can't find file: {}/{}", iconDir, image);
      e.printStackTrace();
      System.exit(1);
    }
    BufferedImage scaled = scale(myPicture, size, size);

    return scaled;
  }

  public ImageIcon loadIcon(String image, int size, String text) {
    if (image == null)
      return null;
    BufferedImage img = imageResize(image, size);
    if (img == null)
      return null;
    if (text != null) {
      Graphics2D g = img.createGraphics();
      g.setFont(ICON_FONT);
      g.setColor(Color.black);
      g.drawString(text, size / 2 - size / 3, size - size / 4);
      g.dispose();
    }
    return img == null ? null : new ImageIcon(img);
  }

  public ImageIcon loadIcon(String image, int size) {
    return loadIcon(image, size, null);
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

  protected void readActivities(String configFileName) throws JAXBException {
    File confFile = new File(configFileName);
    configDir = confFile.getParentFile();
    Activities a = Activities.readActivities(confFile);
    setActivities(a);
  }

  protected void setActivities(Activities a) {
    activities = a.getActivities();
    for (int i = 0; i < activities.size(); ++i) {
      activities.get(i).ordinal = i;
    }
    int acts = activities.size();
    activityPanels = new JComponent[acts][];
    //_sysListeners = new Listener[acts];
    //_userListeners = new Listener[acts];
    //_robotListeners = new Listener[acts];
    currentActivity = -1;

    iconDir = a.icons;
    panelDir = a.panels;
    smalltalkDir = a.smalltalk;
  }


  /**
   * constructor: The config file currently only contains the activities
   * @throws JAXBException
   */
  public WizardGui() {
    super(GUI_TITLE);
  }

  private JComponent newVerticalButtonPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(GridButtonPanel.WITH_SCROLLBARS ? new BoxLayout(panel,
        BoxLayout.Y_AXIS) : new FlowLayout(FlowLayout.LEFT));
    // new BoxLayout(result, BoxLayout.Y_AXIS));
    panel.setAutoscrolls(false);
    return panel;
  }

  /*
  private JComponent newVerticalButtonPanel(JComponent compo) {
     /@
     JPanel panel = new JPanel(new GridBagLayout());
     GridBagConstraints gc = new GridBagConstraints();
     gc.gridx = 0;
     gc.gridy = 0;
     gc.anchor = GridBagConstraints.NORTHWEST;
     panel.add(compo, gc);
      @/
     //panel.setLayout(new B.
     // new BoxLayout(result, BoxLayout.Y_AXIS));
     JPanel panel = new JPanel();
     panel.setLayout(GridButtonPanel.WITH_SCROLLBARS
         ? new BoxLayout(panel, BoxLayout.Y_AXIS)
             : new FlowLayout(FlowLayout.LEFT));
     panel.add(compo);
     panel.setAutoscrolls(false);
     return panel;
   }
   */

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
  public void createGui(Receiver<String> system, Receiver<String> user,
  // StringEventListener asrOnFileListener,
      Receiver<String> robot) {
    Thread currentThread = Thread.currentThread();
    ClassLoader old = currentThread.getContextClassLoader();
    try {
      currentThread.setContextClassLoader(ClassLoader.getSystemClassLoader());
      this._userListeners = user;
      this._robotListeners = robot;
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
      Receiver<String>... listeners) throws IOException {
    int ordinal = activity.ordinal;
    String actName = activity.name.toLowerCase();
    activityPanels[ordinal] = new JComponent[2];
    File[] panelDefinitions = {
        getResource(configDir, panelDir, actName + "UserActions.xml"),
        getResource(configDir, panelDir, actName + "SysActions.xml")
    };
    int i = 0;
    for (File f : panelDefinitions) {
      activityPanels[ordinal][i] =
          new GridButtonPanel(this, listeners[i], f);
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
  public void createGui2(final Receiver<String> sysListener,
      Receiver<String> userListener) throws IOException {
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
    //activityPanels[Activity_NONE] = new JComponent[1];
    //activityPanels[Activity_NONE][0] = newVerticalButtonPanel();

    // a panel for smalltalk buttons
    smallTalkInput = new SmalltalkPanel(this, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        _robotListeners.receive(FREE_RESPONSE_TAG, e.getActionCommand());
      }
    });

    // TODO: PUT INTO TABBED PANE?
    for (Activity activity : activities) {
      //if (activity != activities.get(Activity_NONE))
      createActivityPanel(activity, userListener, _robotListeners);
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


    // motion buttons on the right
    File moFile = getResource(configDir, panelDir, "motionActions.xml");
    GridButtonPanel motionButtons = new GridButtonPanel(this, _robotListeners, moFile);
    //motionButtons.setBorder(new MatteBorder(5,0,0,0,Color.gray));

    // Ouput messages on the left
    JScrollPane outputPane = new JScrollPane();
    outputPane.setViewportView(textPane);
    outputPane.setBorder(new MatteBorder(0,0,0,5,Color.gray));
    outputPane.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent ev) {
        int width = outputPane.getWidth();
        if (ev.getX() > width - 5) {
          motionButtonsVisible = ! motionButtonsVisible;
          motionButtons.setVisible(motionButtonsVisible);
        }
      }
    });

    // dialoguePane contains the (scrollable) text output window and the motion
    // buttons
    JPanel dialoguePane = new JPanel();
    dialoguePane.setLayout(new BorderLayout());
    dialoguePane.add(outputPane, BorderLayout.CENTER);
    // motion buttons on the right
    dialoguePane.add(motionButtons, BorderLayout.EAST);


    // Create the status bar
    statusBar = new JLabel("dialogue state");
    statusBar.setFont(new Font(GUI_FONT_STATUSBAR, Font.PLAIN,
        GUI_FONTSIZE_STATUSBAR));

    JPanel inner = new JPanel(new BorderLayout());
    // Add random buttons panel of smalltalk
    inner.add(smallTalkInput.getRandomPanel(), BorderLayout.NORTH);
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
        new SystemToolBar(this, JToolBar.HORIZONTAL, new Receiver<String>() {
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
    currentActivity = activity.ordinal;
    updateActivityPane(currentActivity);
    smallTalkInput.selectSmalltalkTheme(activity.toString());

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
          _userListeners.receive("", userAnswer);
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
    smallTalkInput.addSmalltalkThemes(_lang);
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

  public void quitProgram() {
    System.exit(0);
  }

}
