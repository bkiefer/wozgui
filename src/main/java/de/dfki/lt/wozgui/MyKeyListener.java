package de.dfki.lt.wozgui;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;

public abstract class MyKeyListener implements KeyListener {
  protected StringBuilder buffer = new StringBuilder();
  private String prevKeyPressed = "";

  private JTextField component;

  public MyKeyListener(JTextField tf) {
    component = tf;
  }

  protected abstract void enterPressed(String message);

  public void keyTyped(KeyEvent e) {
    char character = e.getKeyChar();
    boolean validCharacter = (Character.isLetterOrDigit(character)
        || (character >= 32 && character < 127));
    if (validCharacter)
      buffer.append(character);
  }


  public void keyReleased(KeyEvent e) {
    String keyPressed = KeyEvent.getKeyText(e.getKeyCode());
    if (keyPressed.equals("Enter")) {
      String message = buffer.toString().trim();
      if (! message.isEmpty()) {
        enterPressed(message);
      }
      buffer.delete(0, buffer.length());
      component.setText("");
    }
  }

  public void keyPressed(KeyEvent e) {
    String keyPressed = KeyEvent.getKeyText(e.getKeyCode());
    int messageLength = buffer.length();

    try {
      if (keyPressed.equals("Backspace") && messageLength > 0) {
        buffer.delete(messageLength - 1, messageLength);
      } else if (prevKeyPressed.equals("Ctrl")) {
        if (keyPressed.equals("V")) {
          Transferable trans = Toolkit.getDefaultToolkit()
              .getSystemClipboard().getContents(null);
          buffer.append(
              (String)trans.getTransferData(DataFlavor.stringFlavor));
        } else if (keyPressed.equals("K")) {
          buffer.delete(0, buffer.length());
          component.setText("");
        }
      }
      component.getRootPane().setDefaultButton(null);
      prevKeyPressed = keyPressed;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
