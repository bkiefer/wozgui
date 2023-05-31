package de.dfki.mlt.wozgui.xml;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import de.dfki.mlt.wozgui.xml.Themes.Utterance;

public class TestReadWriteXml {

  @Test
  public void testSerialization()
      throws JAXBException, FileNotFoundException, UnsupportedEncodingException {
    Grid g = new Grid();
    g.buttonheight=200; g.buttonwidth=240;
    Grid.Row r = new Grid.Row();
    g.rows = new ArrayList<>();
    g.rows.add(r);
    Grid.Button b = new Grid.Button();
    b.text = "text";
    //b.initDefault();
    b.command = "command";
    r.buttons = new ArrayList<>();
    r.buttons.add(b);


    // create JAXB context and instantiate marshaller
    JAXBContext context = JAXBContext.newInstance(Grid.class);
    Marshaller m = context.createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    // Write to System.out
    m.marshal(g, out);
    String s = new String(out.toByteArray());

    // Write to File
    //m.marshal(g, new File("grid.xml"));

    // get variables from our xml file, created before
    //System.out.println();
    System.out.println("Output from our XML File:\n" + s);
    Unmarshaller um = context.createUnmarshaller();
    Grid g2 = (Grid) um.unmarshal(new ByteArrayInputStream(s.getBytes("UTF8")));
  }

  @Test
  public void testDeserialization()
      throws JAXBException, FileNotFoundException, UnsupportedEncodingException {
    JAXBContext context = JAXBContext.newInstance(Grid.class);
    Unmarshaller um = context.createUnmarshaller();
    Grid g = (Grid) um.unmarshal(
        new File("src/test/resources/buttonPanels/danceSysActions.xml"));
    g.inherit();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    // Write to System.out
    Marshaller m = context.createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

    // Write to out
    m.marshal(g, out);
    String s = new String(out.toByteArray());
    //System.out.println(s)
  }


  @Test
  public void testThemesDeserialization()
      throws JAXBException, FileNotFoundException, UnsupportedEncodingException {
    JAXBContext context = JAXBContext.newInstance(Themes.class);
    Unmarshaller um = context.createUnmarshaller();
    Themes g = (Themes) um.unmarshal(
        new File("src/test/resources/smalltalk/woz-smalltalk-eng.xml"));

    Utterance u = g.themes.get(0).subthemes.get(0).utterances.get(0);
    assertEquals("may I ask you a question that I have in mind?", u.utterance);
    assertEquals("cockhead", u.motion);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    // Write to System.out
    Marshaller m = context.createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

    // Write to out
    m.marshal(g, out);
    String s = new String(out.toByteArray());
    // System.out(s);
  }

  @Test
  public void testConfigRead() throws JAXBException {
    JAXBContext context = JAXBContext.newInstance(Activities.class);
    Unmarshaller um = context.createUnmarshaller();
    Activities a = (Activities) um.unmarshal(
        new File("src/test/resources/gui.xml"));
    assertEquals(5, a.activities.size());
    System.out.println(a.activities.get(0).name);
  }
}
