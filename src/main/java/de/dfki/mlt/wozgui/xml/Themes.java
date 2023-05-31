package de.dfki.mlt.wozgui.xml;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name="themes")
public class Themes {
  public static class Utterance {
    @XmlValue
    public String utterance;

    @XmlAttribute(name="motion")
    public String motion;
  }

  public static class Subtheme {
    @XmlElement(name="utterance")
    public List<Utterance> utterances;

    @XmlAttribute(name="randomtext")
    public boolean isRandom = false;

    @XmlAttribute
    public String name;
  }

  public static class Theme {
    @XmlElement(name="subtheme")
    public List<Subtheme> subthemes;

    @XmlAttribute
    public String name;

    @XmlAttribute(name="randomtext")
    public boolean isRandom = false;
  }

  @XmlElement(name="theme")
  List<Theme> themes;

  public static List<Theme> readThemes(File in) throws JAXBException {
    JAXBContext context = JAXBContext.newInstance(Themes.class);
    Unmarshaller um = context.createUnmarshaller();
    Themes themes = (Themes) um.unmarshal(in);
    return themes.themes;
  }
}
