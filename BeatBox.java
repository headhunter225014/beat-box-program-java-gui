import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.util.*;
import java.io.*;

public class BeatBox
{
    JPanel mainPanel;
    ArrayList<JCheckBox> checkBoxList; // store flags in arraylist
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame frame;

    String[] instrumentNames = {"Bass Drum", "Closed Hit-Hat", "Open Hit-Hat", "Acoustic snare", "Crash Cymbal", "Hand Clap", "High Tom",
    "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi Conga"}; // names of sounds in array

    int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63}; //Number of instrument
    public static void main(String[] args) {
        new BeatBox().buildGUI();
    }

    public void buildGUI()
    {
        frame = new JFrame("Cyber BeatBox");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout border = new BorderLayout();
        JPanel background = new JPanel(border);
        background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        checkBoxList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Start");     // building buttons
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        JButton save = new JButton("Save");
        save.addActionListener(new MySendListener());
        buttonBox.add(save);

        JButton restore = new JButton("Restore");
        restore.addActionListener(new MyReadListener());
        buttonBox.add(restore);

        Box nameBox  = new Box(BoxLayout.Y_AXIS);
        for(int i = 0; i < 16; i++)
        {
            nameBox.add(new Label(instrumentNames[i]));
        }

        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);

        frame.getContentPane().add(background);


        GridLayout grid = new GridLayout(16,16) ;
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel) ;

        for(int i = 0; i < 256; i++)  //creating flags, giving them bool = false, adding to array and panel
        {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkBoxList.add(c);
            mainPanel.add(c);
        }

        setUpMidi();
        frame.setBounds(50,50,300,300);
        frame.pack();
        frame.setVisible(true);
    }

    public void setUpMidi()
    {
        try
        {
            sequencer = MidiSystem.getSequencer(); // Code for track and synthesizer
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ,4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void buildTrackAndStart()
    {
        int[] trackList = null;

        sequence.deleteTrack(track);    // delete old track and add new one
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++)  // repeat for each of 16 rows
        {
            trackList = new int[16];

            int key = instruments[i]; // create the button that represents instrument

            for(int j = 0; j < 16; j++) //for each bit
            {
                JCheckBox jc = (JCheckBox) checkBoxList.get(j + (16*i));

                if (jc.isSelected())  //
                {
                    trackList[j] = key;
                }
                else //
                {
                    trackList[j] = 0;
                }
            }
            makeTracks(trackList);
            track.add(makeEvent(176, 1, 127, 0, 16));
        }

        track.add(makeEvent(192,9,1,0,15));
        try
        {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);

        }
        catch (Exception e) {e.printStackTrace();}
    }

    public class MyStartListener implements ActionListener // listener for start button
    {
        public void actionPerformed(ActionEvent a)
        {
            buildTrackAndStart();
        }
    }

    public class MyStopListener implements ActionListener
    {
        public void actionPerformed(ActionEvent a)
        {
            sequencer.stop();
        }
    }

    public class MyUpTempoListener implements ActionListener
    {
        public void actionPerformed(ActionEvent a)
        {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor ((float) (tempoFactor * 1.03));
        }
    }

    public class MyDownTempoListener implements ActionListener
    {
        public void actionPerformed(ActionEvent a)
        {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 0.97));
        }
    }

    public void makeTracks(int[] list)
    {
        for(int i = 0; i < 16; i++)
        {
            int key = list[i];

            if(key != 0)
            {
                track.add(makeEvent(144, 9, key, 100, i));
                track.add(makeEvent(128, 9, key, 100, i+1));
            }
        }
    }

    public MidiEvent makeEvent (int comd, int chan, int one, int two, int tick)
    {
        MidiEvent event = null;
        try
        {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            event = new MidiEvent(a, tick);
        }
        catch (Exception e){e.printStackTrace();}
        return event;
    }

    public class MySendListener implements ActionListener
    {
        public void actionPerformed(ActionEvent ex)
        {
            boolean[] checkBoxState = new boolean[256];

            for(int i = 0; i < 256; i++)
            {
                JCheckBox check = (JCheckBox) checkBoxList.get (i);
                if(check.isSelected())
                {
                    checkBoxState[i] = true;
                }
            }

            try
            {
                FileOutputStream fo = new FileOutputStream(new File("checkBox.ser"));
                ObjectOutputStream ob = new ObjectOutputStream(fo);
                ob.writeObject(checkBoxState);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public class MyReadListener implements ActionListener
    {
        public void actionPerformed(ActionEvent av)
        {
            boolean[] checkboxState = null;
            try
            {
                FileInputStream in = new FileInputStream(new File("checkBox.ser"));
                ObjectInputStream is = new ObjectInputStream(in);
                checkboxState = (boolean[]) is.readObject();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            for(int i = 0; i < 256; i++)
            {
                JCheckBox check = (JCheckBox) checkBoxList.get(i);

                if (checkboxState[i])
                {
                    check.setSelected(true);
                }
                else
                {
                    check.setSelected(false);
                }
            }

            sequencer.stop();
            buildTrackAndStart();
        }
    }
    

}
