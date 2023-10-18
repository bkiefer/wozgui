package de.dfki.mlt.wozgui;

import static de.dfki.mlt.wozgui.Constants.*;
import static de.dfki.mlt.wozgui.Utils.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.mlt.wozgui.xml.Themes;
import de.dfki.mlt.wozgui.xml.Themes.Subtheme;
import de.dfki.mlt.wozgui.xml.Themes.Theme;
import de.dfki.mlt.wozgui.xml.Themes.Utterance;

@SuppressWarnings("serial")
public class SmalltalkPanel extends JPanel {

  private static final Color NotSelectedBorder = Color.black;
  private static final Color NextActionBorder = Color.green;
  private static final int NextActionBorderWidth = 3;

  private void fillPanelWithButtons(JComponent owner, List<Action> utterances) {
    owner.removeAll();
    parent.getContentPane().validate();
    owner.repaint();
    // owner.setLayout(new GridLayout(utterances.size(),1,0,3));

    // Put button is for all utterance
    for (Action s : utterances) {
      JButton b = styleButton(new JButton(s), 200, DEFAULT_FONT);
      owner.add(b);
    }
    owner.setPreferredSize(null);
    owner.setMaximumSize(null);
    owner.setAutoscrolls(false);
    owner.setVisible(true);
    parent.getContentPane().validate();

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
    parent.getContentPane().validate();

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
    parent.getContentPane().validate();

  }

  private abstract class AbstractThemeAction extends AbstractAction {
    AbstractThemeAction(String text) {
      super(text);
    }

    public void colorButton(ActionEvent e, JComponent panel) {
      JButton clickedButton = (JButton) e.getSource();
      String name = clickedButton.getParent().getName();
      logger.info(name);
      // fillSmallTalkPanel(_utterances);

      // indicate which section is active by changing border of button
      /*if (name.compareTo(smalltalkThemesPanel.getName()) == 0) {
        for (Component c : smalltalkThemesPanel.getComponents()) {
          assert (c instanceof JButton);
          ((JButton) c).setBorder(BorderFactory
              .createLineBorder(NotSelectedBorder));
        }
      }*/
      if (name.compareTo(panel.getName()) == 0) {
        for (Component c : panel.getComponents()) {
          assert (c instanceof JButton);
          ((JButton) c).setBorder(BorderFactory
              .createLineBorder(NotSelectedBorder));
        }
      }
      clickedButton.setBorder(BorderFactory.createLineBorder(NextActionBorder,
          NextActionBorderWidth));
    }
  }


  private class SubthemeAction extends AbstractThemeAction {
    protected List<Action> _utterances;

    protected SubthemeAction(String text, List<Utterance> utts) {
      super(text);
      initUtterances(utts);
    }

    protected void initUtterances(List<Utterance> utts) {
      _utterances = new ArrayList<Action>(utts.size());
      for (Utterance s : utts) {
        _utterances.add(parent.new TtsButtonAction(s.utterance, s.motion));
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      colorButton(e, smalltalkSubThemesPanel);
      fillPanelWithButtons(smalltalkButtonsPanel, _utterances);
    }
  }

  private class ThemeAction extends AbstractThemeAction {
    private List<Subtheme> _subthemes;

    public ThemeAction(String text, List<Subtheme> subthemes) {
      super(text);
      _subthemes = subthemes;
    }

    /*
    public ThemeAction(String text, List<Utterance> utts) {
      super(text);
      initUtterances(utts);
      _issubtheme = true;
    }\
    */

    @Override
    public void actionPerformed(ActionEvent e) {
      colorButton(e, smalltalkThemesPanel);
      updateSubThemes();
    }

    private void updateSubThemes() {
      smalltalkSubThemesPanel.removeAll();
      smalltalkSubThemesPanel.repaint();
      for (Subtheme subtheme : _subthemes) {
        JButton bt =
            new JButton(new SubthemeAction(subtheme.name, subtheme.utterances));
        bt =
            finalizeButton(bt, GUI_BUTTON_MAX_LENGTH, buttonSmallTalkThemeSize);
        smalltalkSubThemesPanel.add(bt);
      }
      smalltalkSubThemesPanel.repaint();
      smalltalkSubThemesPanel.validate();
    }
  }

  private class ThemeRandomAction extends SubthemeAction {
    private Random _random;
    private List<Utterance> sourceUtterances;

    public ThemeRandomAction(String text, List<Utterance> utts) {
      super(text, utts);
      sourceUtterances = utts;
      _random = new Random(new Date().getTime());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int i = _random.nextInt(_utterances.size());
      _utterances.get(i).actionPerformed(e);
      _utterances.remove(i);
      if (_utterances.size() == 0) {
        initUtterances(sourceUtterances);
      }
    }
  }

  private static final Logger logger = LoggerFactory.getLogger("WizardGUI");

  private WizardGui parent;

  // To enter whatever you like
  private JTextField freeSmalltalk;

  // Sub-panels of the small talk conversation templates
  private JComponent smalltalkButtonsPanel;
  private JComponent smalltalkThemesPanel;
  private JComponent smalltalkSubThemesPanel;
  private JComponent smalltalkRandomPanel;

  private ActionListener robotActionListener;

  private JComponent newSmallTalkButtonPanel(int nrows) {
    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(nrows, 1, 0, 3));
    panel.setAutoscrolls(false);
    return panel;
  }

  public SmalltalkPanel(WizardGui root, ActionListener actionListener) {
    parent = root;
    this.setLayout(new BorderLayout());
    this.robotActionListener = actionListener;

    smalltalkButtonsPanel = newSmallTalkButtonPanel(20);
    smalltalkSubThemesPanel = newSmallTalkButtonPanel(20);
    smalltalkThemesPanel = newSmallTalkButtonPanel(20);
    smalltalkButtonsPanel.setName("smalltalkButtonsPanel");
    smalltalkSubThemesPanel.setName("smalltalkSubThemesPanel");
    smalltalkThemesPanel.setName("smalltalkThemesPanel");

    freeSmalltalk = new JTextField();
    freeSmalltalk.addActionListener(robotActionListener);

    smalltalkRandomPanel = new JPanel(new FlowLayout());
    // Add random buttons panel of smalltalk
    smalltalkRandomPanel.setMaximumSize(new Dimension(this.getWidth(), 50));
    smalltalkRandomPanel.setMinimumSize(new Dimension(this.getWidth(), 50));
    smalltalkRandomPanel.setPreferredSize(new Dimension(this.getWidth(), 50));

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

    this.add(smalltalkSplitter, BorderLayout.CENTER);

    // addSmalltalkThemes("it"); // done by language preload!
  }

  public void addSmalltalkThemes(String lang) {

    List<Theme> themes = null;

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

      File st = getResource(parent.configDir, parent.smalltalkDir,
          "woz-smalltalk-" + lang.toLowerCase() + suffix + ".xml");
      logger.info("Reading SmallTalk file " + st);
      themes = Themes.readThemes(st);
      logger.info("Themes: " + themes.size());
    } catch (Exception ex) {
      parent.errorDialog(ex.getMessage());
    }
    if (themes != null) {
      for (Theme theme : themes) {
        if (theme.isRandom) {
          for (Subtheme s : theme.subthemes) {
            JButton b =
                new JButton(new ThemeRandomAction(s.name, s.utterances));
            b = finalizeButton(b, GUI_BUTTON_MAX_LENGTH, randomButtonSmallTalkSize);
            smalltalkRandomPanel.add(b);
          }
        } else {
          // switches to new theme: changes subpanel
          JButton b = new JButton(new ThemeAction(theme.name, theme.subthemes));
          b = finalizeButton(b, GUI_BUTTON_MAX_LENGTH, buttonSmallTalkThemeSize);
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

  public void selectSmalltalkTheme(String what) {
    for (Component c : smalltalkThemesPanel.getComponents()) {
      assert (c instanceof JButton);
      JButton b = (JButton) c;
      if (b.getText().compareToIgnoreCase(what) == 0) {
        b.doClick();
      }
    }
  }

  public JComponent getRandomPanel() {
    return smalltalkRandomPanel;
  }
}
