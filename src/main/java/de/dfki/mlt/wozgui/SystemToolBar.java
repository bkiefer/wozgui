/**
 * @authors Bernd Kiefer, Bert Bierman
 */
package de.dfki.mlt.wozgui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Document;

import de.dfki.mlt.wozgui.xml.Activities.Activity;

@SuppressWarnings("serial")
public class SystemToolBar extends JToolBar {

  private Receiver<String> _buttonListener;

  private static ButtonDefs lastButton = ButtonDefs.VOICE;

  private static String histName = null;

  /** Display orientation of the tablet */
  private JLabel showOrientation;

  /** The list of buttons in this tool bar */
  private List<JButton> buttons = new ArrayList<JButton>();

  // private HashMap<String,String> userInfo = new HashMap<String,String>();

  /** The owning frame of this tool bar */
  public WizardGui _parent;

  class ButtonAction extends AbstractAction {

    private Icon[] states;
    private String[] actions;

    private int currentState;

    public ButtonAction(// HashMap<String,String> userInfo,
        String[] actions, String text, Icon[] states,
        boolean autoswitch) {
      super(actions[0], states[0]);
      this.states = states;
      this.actions = actions;
      this.currentState = autoswitch ? 0 : -1;
      // this.UserInfo = userInfo;
      this.putValue(SHORT_DESCRIPTION, text);
    }

    public void actionPerformed(ActionEvent e) {
      try {
        if (currentState >= 0 ) changeState(this.currentState + 1);
        String command = (String) getValue(NAME);
        if (_buttonListener != null) {
          // clear text field when starting or switching activities
          if (command.startsWith("NEW_") || command.startsWith("OPEN")
              // || command.startsWith("DO_")  // maybe not
              ) {
            Document doc = _parent.textPane.getDocument();
            doc.remove(0, doc.getLength());
          } else if (command.startsWith("PLAYBACK_START")) {
            String histFile = getPlaybackFile();
            if (histFile == null) return;
            command += "=" + histFile;
          }
          _buttonListener.receive(command,
              (String)this.getValue(SHORT_DESCRIPTION));
        }

        if (command.startsWith("EXIT")){
        	Thread.sleep(2000);
        	_parent.quitProgram();
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    public void activate() {
      changeState(1);
    }

    /** The state of a multi-state button changed, so change the icon to reflect
     *  the state.
     *
     * @param newState
     */
    public void changeState(int newState) {
      int state = newState % states.length;
      if (states.length > 1) {
        setIconAndAction(states[state], actions[state]);
      }
      if (currentState >= 0)
        currentState = state;
    }

    public void setIconAndAction(Icon icon, String action) {
      putValue(LARGE_ICON_KEY, icon);
      putValue(NAME, action);
    }
  }


  private enum ButtonDefs {
    PLAY ( "play.png" , "START_SESSION", "Starts the session." ),
    //PAUSE ( "pause.png" , "PAUSE", "Pauses the whole system." ),
    ABORTSYS (  "abort.png" , "STOP_SESSION", "Ends the session." ),
    EXITSYS (  "quit.png" , "EXIT", "Exits the program." ),
    //( "open.png",
    //  "OPEN", "Re-replays an stored dialogue in XML format (under `data_collection/interaction')." ),
    // ("info.png", null,
    // "INFO", "Opens a dialogue box to introduce user information (currently this info is obtained from the user model)."
    // ),
    //( "language_it.png", "language_en.png", // "language_de.png",
    //  "LANG", "Switches the spoken language to use." ),
    // AUTONOMOUS ( "autonomous_de.png", "autonomous.png",
    //              "AUTONOMOUS=off", "AUTONOMOUS=on",
    //              "switch wizarded / autonomous behaviour." ),
    TTS ( "tts_de.png", "tts.png",
        "TTS=off", "TTS=on", "Enables or disables speech synthesis." ),
    VOICE ( "voice_de.png", "voice.png",
        "ASR=off", "ASR=on", "Process speech recognition outputs."),
    PLAYBACK ( "gtk-media-play-ltr.png", "gtk-media-stop.png" ,
        "PLAYBACK_STOP", "PLAYBACK_START",
        "Starts playback of a recorded session." ),
    STEP ( "gtk-media-next-ltr.png" , "PLAYBACK_NEXT",
        "If playback is paused, execute the next step." ),
    PAUSE ( "gtk-media-pause.png" , "PLAYBACK_PAUSE", "Pause playback." ),

    //( "gesture.png", "gesture_de.png",
    //  "GRU", "Enables or disables gesture recognition (currently not working)." ),
    ;

    private final String[] iconName;
    private final String[] action;
    private final String toolTip;

    ButtonDefs(String... tt) {
      int states = (tt.length - 1)/2;
      iconName = Arrays.copyOfRange(tt, 0, states);
      action = Arrays.copyOfRange(tt, states, states + states);
      toolTip = tt[tt.length - 1];
    }

  };


  @Override
  public JButton add(Action a) {
    JButton b = super.add(a);
    buttons.add(b);
    return b;
  }

  public void setTabletOrientation(String newOrientation){
	  if (newOrientation.equalsIgnoreCase("Landscape") == true){
		  showOrientation.setText("Tablet facing child");
	  } else {
		  if (newOrientation.equalsIgnoreCase("LandscapeReversed") == true){
			  showOrientation.setText("Tablet facing NAO");
		  } else {
			  showOrientation.setText("Tablet orientation unknown");
		  }
	  }
  }

  private String getPlaybackFile() {
    histName = System.getProperty("userlogs.dir");

    if (histName == null)
      histName="/home/kiefer/src/tr/alize/data_collection/debug/interaction/";

    if (! histName.endsWith("/")) histName += "/";
    File hist = new File(histName);
    JFileChooser fc = new JFileChooser(hist);
    fc.setFileFilter(new FileFilter(){
      @Override
      public boolean accept(File pathname) {
        return pathname.isDirectory()
            || pathname.getName().endsWith("quiz.rec");
      }

      @Override
      public String getDescription() {
        return "Return only quiz recording files";
      }});
    int returnVal = 0;
    do {
      hist = null;
      returnVal = fc.showOpenDialog(_parent);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        hist = fc.getSelectedFile();
      }
    }
    while ((hist == null || hist.isDirectory() || ! hist.exists())
        && returnVal != JFileChooser.CANCEL_OPTION);
    if (hist == null) return null;
    // save last location
    histName = hist.getParentFile().getAbsolutePath();
    return hist.getAbsolutePath();
  }


  public SystemToolBar(final WizardGui frame, int orientation,
      Receiver<String> buttonListener, int buttonSize) {
    super(orientation);
    _parent = frame;
    _buttonListener = buttonListener;

    for (ButtonDefs b : ButtonDefs.values()) {	//
      Icon[] icons = new Icon[b.iconName.length];
      for (int j = 0; j < icons.length; ++j) {
        icons[j] = _parent.loadIcon(b.iconName[j], buttonSize);
      }
      add(new ButtonAction(b.action, b.toolTip, icons, true));
      if (lastButton != null && lastButton == b)
        break;
    }

    //String[] states = { "", "-pause", "-play" }; // Idle, Running, Paused
    //String[] commands = { "DO_", "PAUSE_", "RESUME_" };
    String[] states = { "", "-pause" };
    String[] commands = { "ON_", "OFF_" };  // has only effect on GUI

    for (Activity a : _parent.activities) {
      String name = a.toString().toLowerCase();
      Icon[] icons = new Icon[states.length];
      String[] cmds = new String[states.length];
      for(int i = 0; i < states.length; ++i) {
        icons[i] = _parent.loadIcon("action" + states[i] + ".png", buttonSize, name);;
        cmds[i] = commands[i] + a.toString();
      }
      add(new ButtonAction(cmds, "Start/Stop " + name, icons, false));
    }

    // move label to the left
    add(new JPanel());
    showOrientation = new JLabel("");
    add(showOrientation);

    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(3, 3, 3, 3), getBorder()));
    setFloatable(false);

    ButtonDefs[] toActivate = {
        ButtonDefs.TTS, // ButtonDefs.LANG
    };
    for (ButtonDefs id : toActivate) {
      ((ButtonAction) buttons.get(id.ordinal()).getAction()).activate();
    }

    setStateOfControlButtons(false);
  }

  private int fixedButtons() {
    return lastButton == null
        ? ButtonDefs.values().length
        : lastButton.ordinal() + 1;
  }

  /** State of control buttons reflects if the session is started or not
   */
  public void setStateOfControlButtons(boolean inSession) {
    // TODO: This must be changed appropriately
    ((ButtonAction)buttons.get(ButtonDefs.PLAY.ordinal())
        .getAction()).setEnabled(! inSession);
    ((ButtonAction)buttons.get(ButtonDefs.ABORTSYS.ordinal())
        .getAction()).setEnabled(inSession);

    int firstActivity = fixedButtons();
    int i = 0;
    for (Activity a: _parent.activities) {
      ButtonAction action =
          (ButtonAction)buttons.get(i + firstActivity).getAction();
      action.changeState(0);
      action.setEnabled(inSession);
      ++i;
    }
  }

  /** Signal that the given activity is running */
  public void activityState(int what, int state) {
    ButtonAction action =
        (ButtonAction) buttons.get(what + fixedButtons()).getAction();
    action.changeState(state);
  }
}
