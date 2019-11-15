package de.dfki.lt.wozgui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.InputSource;

public class ThemesReader {
  public static class Theme {
    public String name;
    public List<Pair<String, String>> utterances;
    public boolean isRandom;

    Theme(String s) {
      name = s;
      utterances = new ArrayList<Pair<String, String>>();
      subthemes = new ArrayList<Theme>();
      isRandom = false;
    }

    public List<Theme> subthemes;
  }

  public static void readUtterances(Element theme, Theme current) {
    List<Element> utterances = theme.getChildren("utterance");
    for (Element utt : utterances) {
      String motion = utt.getAttributeValue("motion");
      current.utterances.add(
          new Pair<String, String>(utt.getTextNormalize(), motion));
    }
  }

  public static List<Theme> readThemes(InputSource in) throws JDOMException, IOException {
    SAXBuilder builder = new SAXBuilder();
    Document doc = builder.build(in);

    Element root = doc.getRootElement();
    List<Theme> result = new ArrayList<Theme>();
    List<Element> themes = root.getChildren("theme");
    for (Element theme : themes) {
      Theme current = new Theme(theme.getAttributeValue("name"));
      current.isRandom = (theme.getAttributeValue("randomtext") != null);

      readUtterances(theme, current);
      List<Element> subthemes = theme.getChildren("subtheme");
      for (Element subtheme : subthemes) {
    	  Theme currentSubTheme = new Theme(subtheme.getAttributeValue("name"));
    	  readUtterances(subtheme, currentSubTheme);
    	  current.subthemes.add(currentSubTheme);
      }
      result.add(current);
    }
    return result;
  }

//
//  public static void main(String[] args) throws JDOMException, IOException {
//    System.out.println(readThemes(new File(args[0])));
//  }

}
