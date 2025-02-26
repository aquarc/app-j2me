import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.io.*;
import javax.microedition.io.*;
import java.util.*;

import cc.nnproject.json.*;

public class Aquarc extends MIDlet implements CommandListener, ItemStateListener {

    private Display display;
    private Form form;
    private StringItem title;
    private ChoiceGroup[] choices;
    private Command exit;
    private Command search;
    private Alert alert;
    private JSONArray questions_data;
    // for the questions page
    private Form questions;
    private int questionIndex;
    private ChoiceGroup answerChoices;
    private char answer;
    private String rationale;

    // what page are we on?
    private int page;

    public Aquarc() {
        choices = new ChoiceGroup[5];

        questionIndex = 0;
        page = 0;

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
    }

    public void pauseApp() {}

    public void destroyApp(boolean unconditional) {}

    public void commandAction(Command c, Displayable d) {
        if (c == exit) {
            destroyApp(false);
            notifyDestroyed();
        }
        if (c == search) {
            if (page == 0) {
                // design the HTTP query
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

                // actually query

                HttpConnection connection = null;
                InputStream in = null;
                StringBuffer buffer = new StringBuffer();

                try {
                    connection = (HttpConnection) Connector.open(send.toString());
                } catch (IOException e) {
                    // ALERT the user
                    alert = new Alert("Failure", "Unable to open socket with Aquarc's servers", null, AlertType.ERROR);
                    alert.setTimeout(2500);
                    display.setCurrent(alert, null);
                }

                try {
                    in = connection.openInputStream();
                } catch (IOException e) {
                     //ALERT the user
                    alert = new Alert("Failure", "Unable to open input stream with Aquarc's servers", null, AlertType.ERROR);
                    alert.setTimeout(2500);
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
                questions_data = JSON.getArray(line);


                System.out.println(questions_data.size());
                // now you want to parse the JSON and make an array of it.
                // time to make a new Form for the display

                page = 1;
                questionIndex = 0;
                questions = new Form("Question " + (questionIndex + 1) + " of " + questions_data.size());

                // put details first then the question
                StringItem details = new StringItem(null, questions_data.getObject(0).getString("details"));
                questions.append(details);
                // add question
                StringItem question = new StringItem(null, questions_data.getObject(0).getString("question"));
                questions.append(question);

                answerChoices = new ChoiceGroup(null, Choice.EXCLUSIVE);
                JSONArray answerChoicesData = questions_data.getObject(0).getArray("answerChoices");
                for (int i = 0; i < answerChoicesData.size(); i++) {
                    answerChoices.setSelectedIndex(
                        answerChoices.append(answerChoicesData.getString(i), null),
                    false);
                }
                questions.append(answerChoices);

                // add answer and rationale
                answer = questions_data.getObject(0).getString("answer").charAt(0);
                rationale = questions_data.getObject(0).getString("rationale");

                // add difficulty , skill, and domain
                StringItem difficulty = new StringItem(null, questions_data.getObject(0).getString("difficulty"));
                questions.append(difficulty);
                StringItem domain = new StringItem(null, questions_data.getObject(0).getString("domain"));
                questions.append(domain);
                StringItem skill = new StringItem(null, questions_data.getObject(0).getString("skill"));
                questions.append(skill);

                // add commands to go back and to the next question

                Command back = new Command("Back", Command.BACK, 1);
                Command next = new Command("Next", Command.OK, 1);
                questions.addCommand(back);
                questions.addCommand(next);
                questions.setCommandListener(this);
                questions.setItemStateListener(this);


                // TODO: answer and rationale in ALERT
                display.setCurrent(questions);
            } else if (page == 1) {
                // make a new form I guess
            }
        }
    }

    public void itemStateChanged(Item item) {
        // page 1
        if (item == answerChoices) {
            System.out.println("Answer: " + answerChoices.getSelectedIndex());
            if (answerChoices.getSelectedIndex() + 'A' == answer) {
                System.out.println("correct");
                // alert
                alert = new Alert("Correct", rationale, null, AlertType.INFO);
                display.setCurrent(alert, questions);
            } else {
                alert = new Alert("Incorrect", rationale, null, AlertType.INFO);
                display.setCurrent(alert, questions);
            }
        }
    }
}
