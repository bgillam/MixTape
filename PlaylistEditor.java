
/**
 * Write a description of class PlayListEditor here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
import javax.swing.JDialog;
import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;
import java.awt.FileDialog;
import java.io.*;
import javafx.scene.media.*;
//import java.util.*;


public class PlaylistEditor extends JDialog
{
    private MixTape tape; //current tape
    private MixTape oldTape; //old tape
    private JTable songlist = new JTable(); //list of songs in current playlist
    //buttons
    private JButton editButton = new JButton("Edit");
    private JButton hideButton = new JButton("Hide");
    private JButton saveButton = new JButton("Save Changes");
    private JButton cancelButton = new JButton("Discard Changes");
    private JButton deleteButton = new JButton("Delete");
    private JButton addButton = new JButton("Add");
    //panels
    private JPanel buttonPanel = new JPanel();  
    private JPanel editPanel = new JPanel();
    //file access
    private FileDialog fileDialog;
    
    //public methods
    //public void showPlaylist(){
    //public void editPlaylist(){ 
    //public void update(){
    
       
    /**
     * Constructor for objects of class PlayListEditor
     */
    public PlaylistEditor(MixTape thisTape)
    {
       tape = thisTape;
       fileDialog = new FileDialog(this,"title");
       fileDialog.setVisible(false);
      
       setBounds(100, 100, 900, 300);
       getContentPane().setLayout(new BorderLayout());
       getContentPane().add(songlist, BorderLayout.CENTER);
       editPanel.setLayout(new BorderLayout());
       JPanel lilPanel = new JPanel();
       lilPanel.setLayout(new BorderLayout());
       lilPanel.add(deleteButton, BorderLayout.CENTER);
       lilPanel.add(addButton, BorderLayout.NORTH);
       editPanel.add(lilPanel, BorderLayout.NORTH);
       
       getContentPane().add(editPanel, BorderLayout.EAST);
       buttonPanel.add(editButton);
       buttonPanel.add(hideButton);
       buttonPanel.add(saveButton);
       buttonPanel.add(cancelButton);
       getContentPane().add(buttonPanel, BorderLayout.SOUTH);
       addButtonListeners();
       
       
    }
    
    //public methods
    public void showPlaylist(){
        update();
        setVisible(true);
        //setModal(false);
        showListButtons();
    }
    
   public void editPlaylist(){ 
       oldTape = tape.copy();
       oldTape.hide();
       update();
       setVisible(true);
       //setModal(true);
       showEditButtons();
       songlist.setRowSelectionAllowed(true);
    }
       
   public void update(){
        setTitle(tape.getMixTitle());
        int numSongs = tape.getNumSongs();
        songlist.setModel(new DefaultTableModel(tape.getNumSongs()+1, 5));
        songlist.setValueAt("num",0,0);
        songlist.setValueAt("title",0,1);
        songlist.setValueAt("artist",0,2);
        songlist.setValueAt("album",0,3);
        songlist.setValueAt("year",0,4);
        int i = 1;
        for (Song s: tape.getSongs()){
            songlist.setValueAt(i,i,0);
            songlist.setValueAt(s.getTitle(),i,1);
            songlist.setValueAt(s.getArtist(),i,2);
            songlist.setValueAt(s.getAlbum(),i,3);
            songlist.setValueAt(s.getYear(),i,4);
            i++;
        }
          //System.out.println("in p{}ocess update");
    }
    
    //button mamipulation and listening   
    private void showListButtons(){
        editButton.setVisible(true);
        hideButton.setVisible(true);
        saveButton.setVisible(false);
        cancelButton.setVisible(false);
        deleteButton.setVisible(false);
        addButton.setVisible(false);
    }
  
    private void showEditButtons(){
        editButton.setVisible(false);
        hideButton.setVisible(false);
        saveButton.setVisible(true);
        cancelButton.setVisible(true);
        deleteButton.setVisible(true);
        addButton.setVisible(true);
    }
  
    private void addButtonListeners(){
        
        editButton.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               //System.out.println("Edit");
               editPlaylist();
            }
        });
       
        hideButton.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               //System.out.println("Hide");
               setVisible(false);
            }
        });
        
        saveButton.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
             if (tape.getCurrentSongIndex() >= tape.getSongs().size())
                    tape.setCurrentSongIndex(tape.getSongs().size()-1);
             tape.setTapeChanged(true);
             showPlaylist();
             if (tape.getPlayer() == null){
                
                tape.setPlayer(new MediaPlayer(new Media(new File(tape.getSongLocation(0)).toURI().toString())));
                tape.setNewSong(true);
                tape.setSongLabel(tape.getSongTitle(0));
             }
             //System.out.println("Save"); 
            }
        });
        
        cancelButton.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               tape = oldTape;
               showPlaylist();
            }
        });
        
        deleteButton.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               int[] selected = songlist.getSelectedRows(); 
               for (int i = 0; i < selected.length; i++) tape.getSongs().remove(selected[0]-1);
               update();
            }
        });
        
        addButton.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               String fileDir = "";
               String filePath = "";
               fileDialog.setFile("*.mp3");
               fileDialog.setMultipleMode(true);
               fileDialog.setMode(FileDialog.LOAD);
               fileDialog.setVisible(true);
               File[] files = fileDialog.getFiles(); //returning 0 length
               for (File file : files) { 
                   fileDir = fileDialog.getDirectory();
                   fileDir = fileDir.replaceAll("\\\\","/");
                   if (!(fileDir.substring(fileDir.length() - 1).equals("/"))) fileDir = fileDir+"/";
                   filePath = fileDir+file.getName();
                   tape.getSongs().add(new Song(filePath)); 
                }
               update();
               fileDialog.setVisible(false);
            }
        });
        
        
    }
    
    
   
}
