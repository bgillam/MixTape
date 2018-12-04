
/**
 * Write a description of class MixTape here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.JOptionPane;
import java.awt.event.*;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.FileDialog;
import java.io.FilenameFilter;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;


import javafx.scene.media.*;
import javafx.stage.Stage;
import javafx.embed.swing.JFXPanel;
import javafx.util.Duration;
import java.time.temporal.*;
import java.awt.Dimension;


public class MixTape
{
    final JFXPanel fxPanel = new JFXPanel(); //needed for player to not throw thread error
    final String playIcon = "jpegs/play.jpg"; 
    final String editIcon = "jpegs/editbutton.jpg";
    final String pauseIcon = "jpegs/pause.jpg"; 
    final String newIcon = "jpegs/cassette.jpg";
    final String menuIcon ="jpegs/menubutton.jpg";
    final String songsIcon = "jpegs/songlistButton.jpg";
    final String backIcon = "jpegs/back.jpg";
    final String forwardIcon = "jpegs/forward.jpg";
    
    
    
    //state variables that need to be saved when a copy is made
    private ArrayList<Song> songs; //list of songs
    private int currentSong = 0;    //currentSong
    private boolean paused = true;  //is the tape paused?
    private boolean newSong = true;  //is this a newly loaded song? Used for play/pause time update
    private boolean userChanged = true; //did the user change the slider?
    private boolean tapeChanged = false; //has the tape been changed?
    
    //Frames for UI
    private JFrame tapeFrame; //frame for main program.
    private JFrame errorFrame = new JFrame("Error"); //Frame for error messages
    
    //UI components
    private JLabel songLabel = new JLabel("Song Information", SwingConstants.CENTER); //displays song name
    private JLabel mixTitle =  new JLabel("mix title", SwingConstants.CENTER); //displays tape name
    private JButton timeLabel = new JButton("0:00");   //shows time. Used as label, but button used to keep consistant look
    private JButton forwardButton = new JButton();     //navigation buttons 
    private JButton backButton = new JButton();
    private JButton playButton = new JButton();
    private JButton menuButton = new JButton(); //displays "save" "new" "load" menu
    private JButton editButton = new JButton(); //edit playlist
    private JButton newButton = new JButton();  //new tape
    private JButton songsButton = new JButton(); //displays songs
    private PlaylistEditor playlistEditor;       //Dialog for editing playlists
    private JSlider slider = new JSlider();      //slider for song progress
    private Timer timer;                         //timer for song progress displays
    private MediaPlayer player;     //song being played
    
    /* Public methods
     * getters:
    public String getMixTitle()   
    public String getSongTitle(int songNum){
    public String getSongArtist(int songNum){
    public String getSongYear(int songNum){
    public String getSongAlbum(int songNum){
    public String getSongLocation(int songNum){
    public int getCurrentSongIndex(){
    public int getNumSongs
    public ArrayList<Song> getSongs()
    public MediaPlayer getPlayer(){
    * setters    
    public void setSongs(ArrayList<Song> s){
    public void setPaused(boolean p){
    public void setCurrentSongIndex(int inum){
    public void setSongLabel(String s){
    public void setMixTitle(String s){
    public void setPlayer(MediaPlayer plyr){
    public void setNewSong(boolean nSong){
    public void setTapeChanged(boolean f){
    * other
    public void deleteSong(int i)  //deletes song number i from the tape
    public MixTape copy()   //copies the tape
    public void hide(){     //hides the tape
    public void toString(){  //title,artist,a;bum,year
    */  
    
    /*load a tape*/
     public static void main(String args[]){
        MixTape thisTape;
        if (args != null && args.length > 0) {
            thisTape = new MixTape(args[0]); //load tape in parameter
        }else thisTape = new MixTape();  // load empty tape
        
    }
    
    
    /**
     * Constructor for objects of class MixTape
     */
    public MixTape(){
        songs = new ArrayList<Song>();
        currentSong = 0;
        addButtonIcons();
        makeTapeFrame();
        addButtonListeners();
        playlistEditor = new PlaylistEditor(this);
        userChanged = false;
        slider.setValue(0);
        userChanged = true;
        tapeChanged = false;    
        
    }
    
    public MixTape(String playlist){
        this();
        try{
           File playlistFile = new File(playlist);
           loadPlaylist(playlistFile);
        }catch (Exception e){JOptionPane.showMessageDialog(errorFrame,"Could not load file "+playlist,"Error",JOptionPane.ERROR_MESSAGE);}
        
    }

   
    //Timer classes and methods
    
    class updateTime extends TimerTask {
       public void run() {
           timeLabel.setText(minutesSeconds()[0]+":"+String.format("%02d", minutesSeconds()[1]));
           userChanged = false;
           slider.setValue((int)(player.getCurrentTime().toMillis()/player.getStopTime().toMillis()*100 )  );
           userChanged = true;
       }
   }
     
   private int[] minutesSeconds(){
       int[] minSec = new int[2];
       minSec[0] = (int)player.getCurrentTime().toMinutes();
       minSec[1] = ((int)player.getCurrentTime().toSeconds())%60;       
       return minSec;
   }  
   
   
   //file access methods
   private File selectFile(String title, String btnMessage){//try{ 
           FileDialog fileDialog = new FileDialog(tapeFrame,title);
           fileDialog.setMode(fileDialog.LOAD);
           fileDialog.setFile("*.mxtp");
           fileDialog.setVisible(true);
           if (fileDialog.getFile() == null) return null;
           return new File(fileDialog.getDirectory()+fileDialog.getFile());
   }
    
   
   //tape creation methods 
    private void loadPlaylist(File plFile){ //loads song locations from playlist file
        try{
            if (plFile == null) return;
            BufferedReader reader = new BufferedReader(new FileReader(plFile));
            String location = "";
            String missingList = "";
            mixTitle.setText(plFile.getName());
            boolean fnfFlag = false;
            while(location != null){ 
                 location = reader.readLine();
                 if (location == null) break;
                  if (location.equals("")) continue; 
                 //f = new File(location);
                 if (!(new File(location).exists())) {
                    fnfFlag = true;
                    System.out.println("*"+location+"*");
                    missingList = missingList + " "+location+";";
                }
                else songs.add(new Song(location)); 
            }
            if (fnfFlag) JOptionPane.showMessageDialog(errorFrame,"One or more files were not found: "+missingList+". Omitted from playlist.","File not found",JOptionPane.ERROR_MESSAGE);
            songLabel.setText(songs.get(0).getTitle()+" - "+songs.get(currentSong).getArtist());
            currentSong = 0;
            playlistEditor.update();
            player = new MediaPlayer(new Media(new File(getSongLocation(0)).toURI().toString()));
            newSong = true;
        }catch (IOException e){
             System.out.println(e.toString());
             JOptionPane.showMessageDialog(errorFrame,"problem loading file","Error",JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
     private void newTape(){//load blank tape
       if (tapeChanged){
           Object[] options = {"Yes", "No","Cancel"};
           int n = JOptionPane.showOptionDialog(tapeFrame, "Save old tape?","Load blank tape",
                    JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
           if (n == 0) saveTape();
           if (n == 2) return;
       } else{
           Object[] options = {"Yes", "Cancel"};
           int n = JOptionPane.showOptionDialog(tapeFrame, "Close old tape and load blank tape?","Load blank tape",
                    JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
           if (n == 1) return;
       }
       newTape();
   }
    
    public MixTape copy(){//make a copy of the curent tape. used for saving original when editing in case changes are cncelled.
        MixTape copy = new MixTape();
        ArrayList<Song> newSongList = new ArrayList<Song>();
        for (Song s: songs) newSongList.add(s);
        copy.setSongs(newSongList);
        copy.setPaused(paused);
        copy.setCurrentSongIndex(currentSong);
        copy.setSongLabel(songLabel.getText());
        copy.setMixTitle(mixTitle.getText());
        copy.setPlayer(player);
        copy.setNewSong(newSong);
        copy.setTapeChanged(tapeChanged);
        return copy;
              
               
    }
             
    private void saveTape(){
       int ans = 0;; 
       if (songs.size() == 0) ans = JOptionPane.showConfirmDialog (null, "Empty tape. Save anyway?","WARNING",JOptionPane.YES_NO_OPTION);
       if (ans == JOptionPane.YES_OPTION) {
               FileDialog fileDialog = new FileDialog(tapeFrame,"Save Tape");
               fileDialog.setMode(fileDialog.SAVE);
               fileDialog.setFile("*.mxtp");
               fileDialog.setVisible(true);
               try{
                   String mixName = fileDialog.getFile();
                   String fullpath = fileDialog.getDirectory()+mixName+".mxtp";
                   FileWriter fw = new FileWriter(fullpath);
                   mixTitle.setText(mixName);
                   for (Song s: songs) fw.write(s.getLocation()+System.lineSeparator()); 
                   fw.close();
                   tapeChanged = false;
                   //songLabel.setText(songs.get(currentSong).getTitle());
                }catch (Exception e){System.out.println("bad filewriter");}
       }
       
    }
   
    
    //navigation methods
    private void play(){
       //String song = getSongLocation(currentSong);
       try{
             setPlayButtonPlay(false);
             if (player == null) {
                     setPlayer(new MediaPlayer(new Media(new File(getSongLocation(0)).toURI().toString())));
                     setNewSong(true);
                     setSongLabel(getSongTitle(0));
                
             }
             player.play();
             if (newSong) {
                   timer = new Timer();
                   timer.schedule(new updateTime(),0, 100);
                   userChanged = false;
                   slider.setValue(0);
                   userChanged = true;
             }
             paused = false;
             newSong = false;
             player.setOnEndOfMedia(new Runnable() {
                   public void run() {
                       next();
                    }
             });
       }catch(Exception exc){
               System.out.println(exc.toString());
               next();
       }
   }
    
    private void pause(){
            player.pause();
            paused = true;
            try{
               setPlayButtonPlay(true);
            }catch (Exception E){System.out.println("file not found");}
    }
    
    private void playPause(){
       if (paused) { setButtonIcon(playButton, playIcon); play(); setButtonIcon(playButton, playIcon); } 
       else { setButtonIcon(playButton, pauseIcon); pause(); }
    }
       
    private void previous(){
        if (currentSong == 0) currentSong = songs.size()-1;
        else currentSong --;
        stop();
        try{
            player = new MediaPlayer(new Media(new File(getSongLocation(currentSong)).toURI().toString()));
            songLabel.setText(songs.get(currentSong).getTitle()+" - "+songs.get(currentSong).getArtist());
            newSong = true;
            playPause();
        }catch (Exception e){//no songs - do nothing
        }
    }
    
    
    private void next(){
        if (currentSong < songs.size()-1) {
            currentSong++;
        }else {
            currentSong = 0;
        }
        stop();
        try {
            songLabel.setText(songs.get(currentSong).getTitle()+" - "+songs.get(currentSong).getArtist());
            player = new MediaPlayer(new Media(new File(getSongLocation(currentSong)).toURI().toString()));
            newSong = true;
            playPause();
        }catch (Exception e){//no songs do nothing}
        }
    }
       
     
    private void stop(){
        if (player == null) return;
        player.pause();
        paused = true;
        player.stop();
    }
    
       
    //Button methods
    //Button action methods
    private void addButtonIcons(){
        editButton = setButtonIcon(editButton, editIcon); //"jpegs/editbutton.jpg");
        newButton = setButtonIcon(newButton, newIcon);
        menuButton = setButtonIcon(menuButton, menuIcon); //"jpegs/menubutton.jpg");
        songsButton = setButtonIcon(songsButton, songsIcon); //"jpegs/songlistButton.jpg");
        backButton = setButtonIcon(backButton, backIcon);  //"jpegs/back.jpg");
        playButton =  setButtonIcon(playButton, playIcon);
        forwardButton = setButtonIcon(forwardButton, forwardIcon); //"jpegs/forward.jpg");
    }
    
    private ImageIcon scaleBufferedImageToIcon(BufferedImage myPicture, int w, int h){
        ImageIcon myImageIcon = new ImageIcon(myPicture); 
        Image myImage = myImageIcon.getImage(); 
        Image newImage = myImage.getScaledInstance(w, h,  java.awt.Image.SCALE_SMOOTH); 
        return new ImageIcon(newImage);  
    }
    
    //Listeners
    private void addButtonListeners(){
              
       playButton.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e){
            if (thereAreSongs())playPause();
        }
       });
       
    
       forwardButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
            if (thereAreSongs()) next();
            
        }
       });
       
       backButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
            if (thereAreSongs()) previous();
        }
       });
       
       newButton.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               newTape();
            }
        });
    
       editButton.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               playlistEditor.editPlaylist();
            }
        });
        
       songsButton.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               playlistEditor.showPlaylist();
            }
        });
        
         
       menuButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
            stop();
        
            Object[] options = {"Save Playlist","Load Playlist", "New Playlist", "Exit Program", "Cancel", "About"};
            int n = JOptionPane.showOptionDialog(tapeFrame, "Choose an option: Save Tape, load tape, create a new tape, exit program, or retun to current mixtape", "Close MixTape",
                    JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
           if (n ==0)  saveTape();
           if (n == 1) {loadPlaylist(selectFile("Load Playlist", "Choose Playlist"));
                       }
           if (n == 2) newTape();
           if (n == 3) safeExit();
           if (n == 4) setPlayButtonPlay(true);  
           if (n == 5) JOptionPane.showMessageDialog(errorFrame,"Bill Gillam\nVersion 1.0\n 9/28/2017\nwbgillamjr@gmail.com","Old School Mixtape",JOptionPane.INFORMATION_MESSAGE);
        }
       });
       
       slider.addChangeListener(new ChangeListener(){
           public void stateChanged(ChangeEvent e) {
                double percentage = slider.getValue()/100.00;
                if (userChanged) player.seek(player.getTotalDuration().multiply(percentage));
           }
          
       });
       
       
    }
    
    // Setup the Frame/////////////////////////////////////
    private void makeTapeFrame(){
        tapeFrame = new JFrame("Old School Mix Tape");
        tapeFrame.getContentPane().setLayout(new BorderLayout());
        tapeFrame.getContentPane().add(setupTopPanel(), BorderLayout.NORTH);
        tapeFrame.getContentPane().add(setupMidLeftPanel(), BorderLayout.WEST);
        tapeFrame.getContentPane().add(setupReelPanel(), BorderLayout.CENTER);
        tapeFrame.getContentPane().add(setupMidRightPanel(), BorderLayout.EAST);
        tapeFrame.getContentPane().add(setupBottomPanel(), BorderLayout.SOUTH);
        tapeFrame.setResizable(false);
        tapeFrame.pack();
        tapeFrame.setVisible(true);
        tapeFrame.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e)
            {
                safeExit();
            }
          });
          
          
       tapeFrame.addComponentListener(new ComponentAdapter(){
           
              public void componentResized(ComponentEvent e){ 
               tapeFrame.pack();
            }
        });
    }    
    
      
     private JPanel setupTopPanel(){
        JPanel topPanel = new JPanel(); //Panel for tape images
        topPanel.setLayout(new BorderLayout());
        JLabel topLabel = new JLabel(); //JLabel to hold a picture
        JLabel leftLabel = new JLabel();
        JLabel rightLabel = new JLabel();
        topPanel.add(setLabelIcon(topLabel, "jpegs/cassette very top.jpg"),  BorderLayout.NORTH);
        topPanel.add(setLabelIcon(leftLabel,"jpegs/leftPiece2.jpg"), BorderLayout.WEST);
        topPanel.add(mixTitle, BorderLayout.CENTER);
        rightLabel = setLabelIcon(rightLabel, "jpegs/rightPiece.jpg");
        rightLabel.setBorder( new EmptyBorder( 0, 0, 0, 2 ) ); // had to adjust picture didn't match up
        topPanel.add(rightLabel, BorderLayout.EAST);
        return topPanel;
    }
    
    private JPanel setupMidLeftPanel(){
       JPanel midLeftPanel = new JPanel();
       midLeftPanel.setLayout(new BorderLayout());
       midLeftPanel.add(editButton, BorderLayout.NORTH);  
       midLeftPanel.add(newButton ,  BorderLayout.SOUTH);
       return midLeftPanel;
    }
    
    private JPanel setupMidRightPanel(){
       JPanel midRightPanel = new JPanel();
       midRightPanel.setLayout(new BorderLayout());
       JPanel songsPanel = new JPanel();
       midRightPanel.add(backButton, BorderLayout.NORTH);
       midRightPanel.add(playButton, BorderLayout.SOUTH);
       return midRightPanel;
    }
       
    private JPanel setupReelPanel(){ 
         JLabel midLabel = new JLabel(); //JLabel to hold a picture
         JPanel reelPanel = new JPanel();
         reelPanel.add(setScaledLabelIcon(midLabel,"jpegs/cassette middle reels.jpg" , 300, 60));
        return reelPanel;
    }
    
     private JPanel setupBottomPanel(){
        BufferedImage myPicture;
        JLabel picLabel = new JLabel(); //JLabel to hold a picture
        JLabel leftLabel;
        JLabel rightLabel;
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        JPanel controlStrip = new JPanel();
        controlStrip.setLayout(new BorderLayout());
        JPanel leftControlStrip = new JPanel();
        leftControlStrip.setLayout(new BorderLayout());
        JPanel rightControlStrip = new JPanel();
        rightControlStrip.setLayout(new BorderLayout());
        //add left cassette image, pause and scan left to leftControlstrip and play, forward and scan right to rightcontrol strip here
         leftControlStrip.add(menuButton , BorderLayout.WEST);
        leftControlStrip.add(timeLabel, BorderLayout.EAST);
        rightControlStrip.add(songsButton, BorderLayout.WEST);
        rightControlStrip.add(forwardButton, BorderLayout.EAST);
        controlStrip.add(leftControlStrip, BorderLayout.WEST);
        controlStrip.add(songLabel, BorderLayout.CENTER);
        controlStrip.add(rightControlStrip, BorderLayout.EAST);
        bottomPanel.add(controlStrip, BorderLayout.NORTH);
        bottomPanel.add(slider, BorderLayout.CENTER);
        bottomPanel.add(setLabelIcon(picLabel, "jpegs/cassette bottom2.jpg"), BorderLayout.SOUTH);
        return bottomPanel;
        
    }
    
    
    //Gettere
    
    public String getMixTitle(){
        try{
            return mixTitle.getText();
        }catch (Exception e){ return "";}
    }
      
    public String getSongTitle(int songNum){
        try{
            return songs.get(songNum).getTitle();
        }catch (Exception e){ return "";}
            
        
    }
    
    public String getSongArtist(int songNum){
        try{
            return songs.get(songNum).getArtist();
        }catch (Exception e){ return ""; }
    }
    
    public String getSongLocation(int songNum){
        try{
            return songs.get(songNum).getLocation();
        }catch (Exception e){ return ""; }
    }
    
    public String getSongYear(int songNum){
        try{
            return songs.get(songNum).getYear();
        }catch (Exception e){ return ""; }
    }
    
    public String getSongAlbum(int songNum){
         try{
            return songs.get(songNum).getAlbum();
        }catch (Exception e){ return ""; }
    }
    
    public int getNumSongs(){
    return songs.size();
    }
    
    public MediaPlayer getPlayer(){
           return player;
    }
    
    public ArrayList<Song> getSongs(){
        return songs;
    }
    
    
    
    //Setters
    
    private void setPlayButtonPlay(boolean f){
           try{
             if (f == true) playButton.setIcon(scaleBufferedImageToIcon(ImageIO.read(new File(playIcon)), 25 , 25));  
             else playButton.setIcon(scaleBufferedImageToIcon(ImageIO.read(new File(pauseIcon)), 25 , 25));   
            }catch (Exception e){ }
       }
    
     public void setTapeChanged(boolean f){
            tapeChanged = f;
    }
    
    public void setSongs(ArrayList<Song> s){
        songs = s;
    }
    
    public void setPaused(boolean p){
        paused = p;
    }
    
    public void setCurrentSongIndex(int inum){
        currentSong = inum;
    }
     public int getCurrentSongIndex(){
         return currentSong;
     }
    
    public void setSongLabel(String s){
        songLabel.setText(s);
    } 
    
    public void setMixTitle(String s){
        mixTitle.setText(s);
    }
    
    public void setPlayer(MediaPlayer plyr){
        player = plyr;
    }
    
    public void setNewSong(boolean nSong){
        newSong = nSong;
    }
    
    private JButton setButtonIcon(JButton jb, String ps){
        try{
            jb = new JButton(scaleBufferedImageToIcon(ImageIO.read(new File(ps)), 25 , 25));
        }catch (Exception e) { jb = new JButton(ps);
        }
        return jb;
    }
    
    private JLabel setLabelIcon(JLabel jl, String ps){
         try{
             jl = new JLabel(new ImageIcon(ImageIO.read(new File(ps))));
        }catch (Exception e){ jl = new JLabel(ps);
        }
        return jl;
    }
    
    private JLabel setScaledLabelIcon(JLabel jl, String ps, int w, int h){
    try{
             jl = new JLabel(scaleBufferedImageToIcon(ImageIO.read(new File(ps)), w , h));
        }catch (Exception e){
             jl = new JLabel(ps);
        }
        return jl;
    }
    
    //misc private methods  
      private boolean thereAreSongs(){
          try{
              songs.get(0);
              return true;
          }catch (Exception e){}
          return false;
      }
      
      
      private void safeExit(){
          if (tapeChanged){
              Object[] options = {"Yes", "No","Cancel"};
              int n = JOptionPane.showOptionDialog(tapeFrame, "Save tape before exit?","Save Tape?",
                    JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                    if (n == 0) saveTape();
                    if (n==3) return; 
          }
          if (timer != null) timer.cancel();
          System.exit(0); 
      }
    
    //other public methods
    public void deleteSong(int i){
           songs.remove(i);
    }
    
     public void hide(){
        tapeFrame.setVisible(false);
    }
    
    public String toString(){
        String songString = "";
        for (Song s: songs)
            songString = songString+s.getTitle()+" "+s.getArtist()+" "+s.getAlbum()+" "+s.getYear()+"\n";
        return songString;
            
    }
    
}
