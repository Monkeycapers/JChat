import java.io.*;
import java.net.*;

/**
 * Created by Evan on 8/21/2016.
 */
public class JChat {

    Settings settings;
    Gui gui;


    public JChat() {

        settings = new Settings();
        settings.load();

        gui = new Gui(settings);
        gui.load();
        //for (int i = 0; i <= 100; i ++) {
            //gui.addText("<Monkeycapers>: Test " + i);
        //}

    }




}
