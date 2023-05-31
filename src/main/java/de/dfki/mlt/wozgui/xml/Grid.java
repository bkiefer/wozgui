package de.dfki.mlt.wozgui.xml;

import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement(name = "grid")
public class Grid extends Style {
  private static final Logger logger = LoggerFactory.getLogger(Grid.class);

  public static class Row extends Style {
    @XmlElement(name="button")
    public
    List<Button> buttons;

    void inherit(Style parent) {
      super.inherit(parent);
      if (buttons != null) {
        for (Button b : buttons) {
          b.inherit(this);
        }
      } else {
        buttons = Collections.emptyList();
      }
    }
  }

  public static class Button extends Style {
    @XmlElement(name="text")
    public
    String text = null;
    @XmlElement(name="command")
    public
    String command = null;
    @XmlElement(name="icon")
    public
    String iconName = null;
  }

  @XmlElement(name = "row")
  public
  List<Row> rows;

  void inherit() {
    for (Row r : rows) {
      r.inherit(this);
    }
  }

  public static Grid readGrid(File in) {
    try {
      JAXBContext context = JAXBContext.newInstance(Grid.class);
      Unmarshaller um = context.createUnmarshaller();
      Grid g = (Grid) um.unmarshal(in);
      g.inherit();
      return g;
    } catch (JAXBException ex) {
      logger.error("Reading grid file failed: {}", ex);
    }
    return null;
  }
}
