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

    private StringBuffer send;
    private StringBuffer encodedOptions;
    // for the questions page
    private Form questions;
    private int questionIndex;
    private ChoiceGroup answerChoices;
    private char answer;
    private String rationale;
    private Command back, next;

    public Aquarc() {
        choices = new ChoiceGroup[5];

        questionIndex = 0;

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
           // design the HTTP query
           send = new StringBuffer("http://j2me.aquarc.org/legacy/sat-q/");

           encodedOptions = new StringBuffer();
           int realIndex = 0;
           for (int i = 0; i < choices.length; i++) {
               boolean[] flags = new boolean[choices[i].size()];
               choices[i].getSelectedFlags(flags);
               for (int j = 0; j < flags.length; j++) {
                   if (flags[j]) {
                       encodedOptions.append((char) ('a' + realIndex));
                   }
                   realIndex++;
               }
           } 

           // actually query
           downloadQuestions(0);
           loadQuestion();
       } else if (c == next) {
           // Prevent incrementing beyond the last question
           if (questions_data.size() == 0) {
                questionIndex = 0;
                downloadQuestions(0);
                loadQuestion();
           } else if (questionIndex % questions_data.size() < questions_data.size() - 1) {
               questionIndex++;
               loadQuestion();
           } else {
               questionIndex++;
               downloadQuestions(questionIndex + 1);
               loadQuestion();
           }
        } else if (c == back) {
           if (questionIndex % questions_data.size() > 0) {
               questionIndex--;
               loadQuestion();
           } else if (questionIndex % questions_data.size() == 0 && questionIndex > 0) {
               questionIndex--;
               downloadQuestions(questionIndex - 9);
               loadQuestion();
           } else if (questionIndex <= 0) {
               display.setCurrent(form);
           } 
        } 
    }

    public void itemStateChanged(Item item) {
        // page 1
        if (item == answerChoices) {
            if (answerChoices.getSelectedIndex() + 'A' == answer) {
                // alert
                alert = new Alert("Correct", rationale, null, AlertType.INFO);
                display.setCurrent(alert, questions);
            } else {
                alert = new Alert("Incorrect", rationale, null, AlertType.INFO);
                display.setCurrent(alert, questions);
            }
        }
    }

    public void loadQuestion() {
           // Validate questionIndex range
           if (questionIndex < 0) {
               alert = new Alert("Error", "Invalid question index.", null, AlertType.ERROR);
               display.setCurrent(alert, form); // Return to main form
               return;
           }

           if (questions_data.size() == 0) {
               alert = new Alert("Error", "No questions found. If you try again, you will start again at the first question.", null, AlertType.ERROR);
               display.setCurrent(alert, questions); // Return to previous question
               questionIndex--;
               return;
           }

           questions = new Form("Question " + (questionIndex % questions_data.size() + 1) + " of " + questions_data.size());

           // put details first then the question
           StringItem details = new StringItem(null, questions_data.getObject(questionIndex % questions_data.size()).getString("details"));
           questions.append(details);
           // add question
           StringItem question = new StringItem(null, questions_data.getObject(questionIndex % questions_data.size()).getString("question"));
           questions.append(question);

           answerChoices = new ChoiceGroup(null, Choice.EXCLUSIVE);
           // Inside loadQuestion()
           JSONArray answerChoicesData = questions_data.getObject(questionIndex % questions_data.size()).getArray("answerChoices");
           if (answerChoicesData.size() == 0) {
               // Handle empty answer choices (e.g., show an error)
               alert = new Alert("Error", "No answer choices provided.", null, AlertType.ERROR);
               display.setCurrent(alert, questions);
               return; // Skip further processing
           }

            // Populate answerChoices only if data exists
           answerChoices = new ChoiceGroup(null, Choice.EXCLUSIVE);
           for (int i = 0; i < answerChoicesData.size(); i++) {
               answerChoices.append(answerChoicesData.getString(i), null);
           }
           questions.append(answerChoices);

           // add answer and rationale
           answer = questions_data.getObject(questionIndex % questions_data.size()).getString("answer").charAt(0);
           rationale = questions_data.getObject(questionIndex % questions_data.size()).getString("rationale");

           // add difficulty , skill, and domain
           StringItem difficulty = new StringItem(null, questions_data.getObject(questionIndex % questions_data.size()).getString("difficulty"));
           questions.append(difficulty);
           StringItem domain = new StringItem(null, questions_data.getObject(questionIndex % questions_data.size()).getString("domain"));
           questions.append(domain);
           StringItem skill = new StringItem(null, questions_data.getObject(questionIndex % questions_data.size()).getString("skill"));
           questions.append(skill);

           // add commands to go back and to the next question

           back = new Command("Back", Command.BACK, 1);
           next = new Command("Next", Command.OK, 1);
           questions.addCommand(back);
           questions.addCommand(next);
           questions.setCommandListener(this);
           questions.setItemStateListener(this);

           display.setCurrent(questions);
    } 

    public void downloadQuestions(int offset) {
        HttpConnection connection = null;
        InputStream in = null;
        ByteArrayOutputStream byteOut = null; // To store raw bytes

        StringBuffer urlBuffer = new StringBuffer(send.toString());
        if (offset != 0) {
            urlBuffer.append(offset);
        }
        urlBuffer.append(encodedOptions.toString());

        try {
            connection = (HttpConnection) Connector.open(urlBuffer.toString());
            in = connection.openInputStream();

            // Read all bytes into a byte array
            byteOut = new ByteArrayOutputStream();
            int bytesRead;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) != -1) {
                byteOut.write(buffer, 0, bytesRead);
            }

            // Convert bytes to UTF-8 string
            String jsonResponse = byteOut.toString("UTF-8");
            questions_data = JSON.getArray(jsonResponse);

        } catch (IOException e) {
            alert = new Alert("Error", "Connection failed: " + e.getMessage(), null, AlertType.ERROR);
            alert.setTimeout(Alert.FOREVER);
            display.setCurrent(alert, form);
        } finally {
            try {
                if (byteOut != null) byteOut.close();
                if (in != null) in.close();
                if (connection != null) connection.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}
