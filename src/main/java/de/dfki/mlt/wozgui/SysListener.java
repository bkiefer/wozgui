package de.dfki.mlt.wozgui;

import de.dfki.mlt.wozgui.WizardGui.Conversant;

public class SysListener implements Receiver<String> {
  WizardGui g;
  public SysListener(WizardGui gui) {
    g = gui;
  }

  Conversant c = Conversant.Debug;

  private void react(String event) {
    switch (event) {
    case "START_SESSION":
      g.setStateOfSession(true);
      break;
    case "STOP_SESSION":
      g.setStateOfSession(false);
      break;
    }
    if (event.startsWith("ON_")) {
      String act = event.substring(3);
      g.activities.forEach((a) -> {
        if (a.name.equalsIgnoreCase(act)) {
          g.switchToActivity(a);
          g.setLanguage("eng");
        }
      });
    }
  }

  @Override
  public void receive(String event, String message) {
    react(event);
    g.setMessage(c, "[" + event + "," + message + "]");
  }
}