import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.io.*;
import javax.microedition.io.*;
import java.util.*;

public class Aquarc extends MIDlet implements CommandListener, ItemStateListener {

    private Display display;
    private Form form;
    private StringItem title;
    private ChoiceGroup[] choices;
    private Command exit;
    private Command search;
    private Alert alert;

    public Aquarc() {
        choices = new ChoiceGroup[5];

        display = Display.getDisplay(this);
        form = new Form("aquarc Eng SAT Qs");

        choices[0] = new ChoiceGroup("Information and Ideas", Choice.MULTIPLE);
        choices[0].setSelectedIndex(
            choices[0].append("Central Ideas and Details", null),
        true);
        choices[0].setSelectedIndex(
            choices[0].append("Inferences", null),
        true);
        choices[0].setSelectedIndex(
            choices[0].append("Command of Evidence", null),
        true);
        form.append(choices[0]);

        choices[1] = new ChoiceGroup("Craft and Structure", Choice.MULTIPLE);
        choices[1].setSelectedIndex(
            choices[1].append("Words in Context", null),
        true);
        choices[1].setSelectedIndex(
            choices[1].append("Text Structure and Purpose", null),
        true);
        choices[1].setSelectedIndex(
            choices[1].append("Cross-Text Connections", null),
        true);
        form.append(choices[1]);

        choices[2] = new ChoiceGroup("Expression of Ideas", Choice.MULTIPLE);
        choices[2].setSelectedIndex(
            choices[2].append("Rhetorical Synthesis", null),
        true);
        choices[2].setSelectedIndex(
            choices[2].append("Transitions", null),
        true);
        form.append(choices[2]);

        choices[3] = new ChoiceGroup("Standard English Conventions", Choice.MULTIPLE);
        choices[3].setSelectedIndex(
            choices[3].append("Boundaries", null),
        true);
        choices[3].setSelectedIndex(
            choices[3].append("Form, Structure, and Sense", null),
        true);
        form.append(choices[3]);

        choices[4] = new ChoiceGroup("Difficulty", Choice.MULTIPLE);
        choices[4].setSelectedIndex(
            choices[4].append("Hard", null),
        true);
        choices[4].setSelectedIndex(
            choices[4].append("Medium", null),
        true);
        choices[4].setSelectedIndex(
            choices[4].append("Easy", null),
        true);
        form.append(choices[4]);

        exit = new Command("Exit", Command.EXIT, 1);
        search = new Command("Search", Command.OK, 1);

        form.addCommand(exit);
        form.addCommand(search);
        form.setCommandListener(this);
        form.setItemStateListener(this);
    }

    public void startApp() {
        display.setCurrent(form);
//
//        HttpConnection connection = null;
//        InputStream in = null;
//        StringBuffer buffer = new StringBuffer();
//
//        try {
//            
//            connection = (HttpConnection) Connector.open("http://localhost:8080/legacy/sat-q/10hi");
//
//        } catch (IOException e) {
//            System.out.println("Connection f");
//        }
//            System.out.println("Connection opened");
//            try {
//            in = connection.openInputStream();
//            } catch (IOException e) {
//                System.out.println("Connection f");
//            }
//            int ch;
//            try {
//                
//            while ((ch = in.read()) != -1) {
//                buffer.append((char)ch);
//            } 
//            } catch (IOException e) {
//                System.out.println("Connection g");
//            }
//            String line = new String (buffer.toString());
//            stringItem.setText(line);


    }

    public void pauseApp() {}

    public void destroyApp(boolean unconditional) {}

    public void commandAction(Command c, Displayable d) {
        if (c == exit) {
            destroyApp(false);
            notifyDestroyed();
        }
        if (c == search) {
            StringBuffer send = new StringBuffer("http://localhost:8080/legacy/sat-q/");
            int realIndex = 0;
            for (int i = 0; i < choices.length; i++) {
                boolean[] flags = new boolean[choices[i].size()];
                choices[i].getSelectedFlags(flags);
                for (int j = 0; j < flags.length; j++) {
                    if (flags[j]) {
                        send.append((char) ('a' + realIndex));
                    }
                    realIndex++;

                }

            } 

            // print send
            StringItem stringItem = new StringItem(null, send.toString());
            form.append(stringItem);


            HttpConnection connection = null;
            InputStream in = null;
            StringBuffer buffer = new StringBuffer();

            try {
                
                connection = (HttpConnection) Connector.open(send.toString());

            } catch (IOException e) {
                // ALERT the user
                alert = new Alert("Failure", "Unable to open socket with Aquarc's servers", null, AlertType.ERROR);
                alert.setTimeout(Alert.FOREVER);
                display.setCurrent(alert, null);
            }

            try {
                in = connection.openInputStream();
            } catch (IOException e) {
                 //ALERT the user
                alert = new Alert("Failure", "Unable to open input stream with Aquarc's servers", null, AlertType.ERROR);
                alert.setTimeout(Alert.FOREVER);
                display.setCurrent(alert, null);
            }
            int ch;
            try {
                while ((ch = in.read()) != -1) {
                    buffer.append((char)ch);
                } 
            } catch (IOException e) {
                //ALERT the user
                alert = new Alert("Failure", "Data reading error", null, AlertType.ERROR);
                alert.setTimeout(Alert.FOREVER);
                display.setCurrent(alert, null);
            }
            String line = new String (buffer.toString());
            stringItem = new StringItem(null, line);
            stringItem.setText(line);
            form.append(stringItem);

        }
    }

    public void itemStateChanged(Item item) {
    }
}
