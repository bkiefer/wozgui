package de.dfki.mlt.wozgui;

import static de.dfki.mlt.wozgui.Constants.*;

import javax.xml.bind.JAXBException;

import de.dfki.mlt.wozgui.WizardGui.Conversant;

public class TestGui {
  /** This method demonstrates what special treatment of the free response
   *  field could look like
   */
  public static String freeResponseAction(String cmd, String text) {
    if (!cmd.contains(FREE_RESPONSE_TAG)) return null;
    if (text.startsWith("@"))
      return text.substring(1);
    String motion = null;
    int firstBar = text.indexOf('|');
    if (firstBar >= 0) {
      motion = text.substring(firstBar + 1);
      text = text.substring(0, firstBar);
    }
    StringBuilder sb = new StringBuilder();
    sb.append("provide(freeresponse");
    if (motion != null)
      sb.append(", motion=\"").append(motion).append("\"");
    if (text != null)
      sb.append(", string=\"").append(text).append("\"");
    sb.append(")");
    return sb.toString();
  }


  /** For test purposes only */
  private static Receiver<String> makeStringListener(final WizardGui g,
      final Conversant c) {
    return new Receiver<String>() {
      @Override
      public void receive(String event, String message) {
        String free = freeResponseAction(event, message);
        if (free != null) {
          message = free;
        }
        g.setMessage(c, "[" + event + "," + message + "]");
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
    Receiver<String> usr = makeStringListener(g, Conversant.User);
    Receiver<String> sys = new SysListener(g);
    Receiver<String> robot = makeStringListener(g, Conversant.Robot);
    g.createGui(sys, usr, robot);
    g.setLanguage("eng");
  }
}
