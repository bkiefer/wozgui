package de.dfki.mlt.wozgui.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement(name="activities")
public class Activities {
  private static final Logger logger = LoggerFactory.getLogger("WizardGUI");

  public static class Activity {
    @XmlAttribute
    public String name;

    public int ordinal;

    @Override
    public String toString() {
      return name;
    }
  }

  @XmlAttribute(name="icons", required=true)
  public String icons;

  @XmlAttribute(name="panels", required=true)
  public String panels;

  @XmlAttribute(name="smalltalk", required=true)
  public String smalltalk;

  @XmlElement(name="activity")
  List<Activity> activities;

  public List<Activity> getActivities() {
    return activities;
  }

  public void addActivity(Activity a) {
    if (activities == null) {
      activities = new ArrayList<>();
    }
    a.ordinal = activities.size();
    activities.add(a);
  }

  public static Activities readActivities(File f) throws JAXBException {
    Activities a = null;
    try {
      JAXBContext context = JAXBContext.newInstance(Activities.class);
      Unmarshaller um = context.createUnmarshaller();
      a = (Activities)um.unmarshal(f);
    } catch (JAXBException ex) {
      logger.error("Error reading config: " + ex);
      throw(ex);
    }
    return a;
  }
}
