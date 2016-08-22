/**
 * Created by Evan on 8/21/2016.
 */
import java.io.*;
import java.util.Scanner;
public class Settings {

    //Defaults
    boolean LOAD_SETTINGS = false;
    boolean FULL_SCREEN = false;

    int GUI_WIDTH = 10;
    int GUI_HEIGHT = 50;
    String GUI_TITLE = "JChat 0.03";

    String hostName = "localhost";
    int portNumber = 16000;


    public Settings () {

    }

    public void load () {

        if (!LOAD_SETTINGS) return;

    }

}
