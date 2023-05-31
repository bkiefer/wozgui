package de.dfki.mlt.wozgui;

import javax.xml.bind.JAXBException;

import de.dfki.mlt.wozgui.WizardGui.Conversant;

public class TestGui {

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

  /** For test purposes only
   * @throws JAXBException
   */
  public static void main(String[] args) throws JAXBException {
    // Read top-level spec file
    final WizardGui g = new WizardGui();
    g.readActivities(args[0]);
    // this is for test purposes only
    Listener<String> usr = makeStringListener(g, Conversant.User);
    Listener<String> sys = new SysListener(g);
    Listener<String> robot = makeStringListener(g, Conversant.Robot);
    g.createGui(sys, usr, robot);
    g.setLanguage("eng");
  }
}
