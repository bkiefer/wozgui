package de.dfki.mlt.wozgui;

import static de.dfki.mlt.wozgui.Constants.*;
import static de.dfki.mlt.wozgui.Utils.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import de.dfki.mlt.wozgui.xml.Grid;
import de.dfki.mlt.wozgui.xml.Style;

public class GridButtonPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  public static final boolean WITH_SCROLLBARS = true;

  private static final Color NotSelectedBorder = Color.black;
  private static final Color NextActionBorder = Color.green;
  private static final int NextActionBorderWidth = 3;

  /** The owner of this panel */
  private WizardGui _parent;

  /** The listener to send actions and messages to */
  private Receiver<String> _receiver;

  /** To be able to add a scroll bar to the button panel */
  private JPanel _contentPanel;

  /** Just a constraints object for this panel*/
  private GridBagConstraints c;

  private List<JButton> allButtons = new ArrayList<JButton>();

  private HashMap<String, Set<JButton>> patternCache =
      new HashMap<String, Set<JButton>>();

  @SuppressWarnings("serial")
  public class GridButtonAction extends AbstractAction {
    private String _action;
    private String _label;
    private int _id;
    private int[] _nextAction;

    public GridButtonAction(String label, String iconName, String action) {
      _label = label;
      _action = action;

      ImageIcon icon = _parent.loadIcon(iconName, 48);
      if (icon != null) {
        putValue(Action.LARGE_ICON_KEY, icon);
        //putValue(Action.SHORT_DESCRIPTION, label);
      } else {
        putValue(Action.NAME, label == null ? "" : label);
      }
      putValue(Action.SHORT_DESCRIPTION, action);
    }

    public GridButtonAction(int span) { _action = null; }

    @Override
    public void actionPerformed(ActionEvent e) {
      _receiver.receive(_label, getAction());
    }

    @Override
    public boolean isEnabled() { return true; }

    public int getId() { return _id; }

    public int[] getNextAction() { return _nextAction; }

    public String getAction() { return _action; }
  }


  public GridButtonPanel(WizardGui p, Receiver<String> l, File f)
      throws IOException {
    super(WITH_SCROLLBARS ? new BorderLayout() : new GridBagLayout());
    if (WITH_SCROLLBARS) {
      _contentPanel = new JPanel(new GridBagLayout());
      JScrollPane sp = new JScrollPane(_contentPanel);
      this.add(sp, BorderLayout.WEST);
    } else {
      _contentPanel = this;
    }
    c = new GridBagConstraints();
    _receiver = l;
    this._parent = p;
    Grid g = Grid.readGrid(f);
    int row = 0;
    for (Grid.Row r : g.rows) {
      int col = 0;
      for (Grid.Button b : r.buttons) {
        addCell(b.text, b.command, b.iconName, b, row, col);
        col += b.getSpan();
      }
      ++row;
    }
  }

  public void colorButtons(String regex) {
    Set<JButton> buttons = patternCache.get(regex);
    if (buttons == null) {
      buttons = new HashSet<JButton>();
      patternCache.put(regex, buttons);
      Pattern p = Pattern.compile(regex);
      for (JButton temp: allButtons) {
        GridButtonAction  a = (GridButtonAction) temp.getAction();
        if (a != null && p.matcher(a.getAction()).find()) {
          buttons.add(temp);
        }
      }
    }
    for (JButton b : allButtons) {
      b.setBorder(buttons.contains(b)
          ? BorderFactory.createLineBorder(NextActionBorder,
              NextActionBorderWidth)
          : BorderFactory.createLineBorder(NotSelectedBorder));
    }
  }

  /*
  private JComponent getFreeQuestionField(final String text, final String action) {
    JButton button = new JButton(text);
    button.setActionCommand(text);
    ActionListener al = new ActionListener() {
      public void actionPerformed(ActionEvent as) {
        JFrame fr = (JFrame)GridButtonPanel.this.getRootPane().getParent();
        UserQuestionDialog uq = null;
        if (fr instanceof WizardGui) {
          uq = ((WizardGui)fr).uq;
        }
        if (uq != null) {
          uq.openDialog(); uq.pack();
          String buttonAction = as.getActionCommand();
          String question = uq.getText();
          if (question == null)
            question = "";
          else
            question = PROVIDE_QUESTION + ", string=\"" + question +
            (uq.getAction().equals("With")
                ? "\", answersProvided=true)" : "\")");
          _listener.receive(buttonAction, question);
        }
      }
    };
    button.setToolTipText(text);
    button.addActionListener(al);
    return button;
  }
  */

  /** Create a JTextField to be able to enter arbitrary text
   *
   * The receiver needs to take specific means for the free text field if it
   * intends to do so. The text is guaranteed to contain the FREE_RESPONSE_TAG,
   * so it can be identified by that.
   *
   * TODO: add a history and a drop-down menu showing it that is attached to the
   * text field
   */
  private JComponent getFreeResponseField(final String text, final String action,
      Dimension d) {
    JTextField _freeResponse;
    _freeResponse = new JTextField();
    _freeResponse.setText("");
    _freeResponse.setToolTipText(action);
    _freeResponse.setMinimumSize(d);
    _freeResponse.setMaximumSize(d);
    setListeners(_freeResponse, new ActionListener(){
      @Override
      public void actionPerformed(ActionEvent e) {
        // The receiver needs to take specific means for the free text field if
        // it intends to do so. The text is guaranteed to contain the
        // FREE_RESPONSE_TAG, so it can be identified by that.
        _receiver.receive(text, e.getActionCommand());
      }
    });
    return _freeResponse;
  }


  private void addCell(String text, String command, String iconName, Style style,
      int row, int col) {
    JComponent component;
    c.gridx = col;
    c.gridy = row;
    c.gridwidth = style.getSpan();
    int inset = style.getInset();
    c.insets = new Insets(inset, inset, inset, inset);
    if (command == null) {
      // a separator
      component = new JLabel(text, style.getAlignment());
      c.anchor = GridBagConstraints.SOUTHWEST;
    } else {
      if (text.toLowerCase().contains(FREE_RESPONSE_TAG)) {
        component = getFreeResponseField(text, command, style.getDimension());
      } /* else if (command.equals(FREE_QUESTION_ACTION)) {
        component = getFreeQuestionField(text, FREE_QUESTION_ACTION);
      } */
      else {
        // a button
        component = new JButton(new GridButtonPanel.GridButtonAction(text,
            iconName, command));
        allButtons.add((JButton) component);
        c.anchor = GridBagConstraints.CENTER;
      }
    }
    if (style.getFgColor() != null) {
      component.setForeground(style.getFgColor());
    }
    if (style.getBgColor() != null) {
      component.setBackground(style.getBgColor());
    }
    Dimension thisButtonSize = style.getDimension();
    component.setPreferredSize(thisButtonSize);
    component.setMinimumSize(thisButtonSize);
    component.setMaximumSize(thisButtonSize);

    component.setFont(style.getFont());

    component.setAlignmentX(Component.CENTER_ALIGNMENT);
    component.setAlignmentY(Component.CENTER_ALIGNMENT);
    _contentPanel.add(component, c);
  }
}
