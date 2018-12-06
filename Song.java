
/**
 * Write a description of class Song here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
import java.io.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
public class Song
{
    // instance variables - replace the example below with your own
    //private File songFile;
    private String fileLocation;
    private String title;
    //private int time;
    private String artist;
    private String genre;
    private String album;
    private String year;
    private String comment;
    private String track;
    private String zeroByte;
    private JFrame errorFrame = new JFrame("Error");
       
    public static void main(String args[]){
        String testSongFilePath = "C:/Users/Bill/workspace/MixTape/Songs/04 - Robert Earl Keen - Ride .mp3";
        Song testSong = new Song(testSongFilePath);
        System.out.println("Artist: "+testSong.getArtist());
        System.out.println("Album: "+testSong.getAlbum());
        System.out.println("Year: "+testSong.getYear());
        System.out.println("Comment: "+testSong.getComment());
        System.out.println("Track: "+testSong.getTrack());
        System.out.println("Genre: "+testSong.getGenre());
        
        testSongFilePath = "C:/Users/Bill/workspace/MixTape/Songs/06 - Old Crow Medicine Show - Wagon Wheel .mp3";
        testSong = new Song(testSongFilePath);
        System.out.println("Artist: "+testSong.getArtist());
        System.out.println("Album: "+testSong.getAlbum());
        System.out.println("Year: "+testSong.getYear());
        System.out.println("Comment: "+testSong.getComment());
        System.out.println("Track: "+testSong.getTrack());
        System.out.println("Genre: "+testSong.getGenre());
        
    }
    
    //public methods
    //public String getLocation(){
    //public String getTitle(){
    //public String getArtist(){
    //public String getAlbum(){
    //public String getYear(){
    //public String getComment(){
    //public String getGenre(){
    //public String getTrack(){
    
        
        
    /**
     * Constructor for objects of class Song
     */
    public Song()
    {
        
    }
    
    public Song(String pfileLocation){
        if (pfileLocation == "" && pfileLocation == null) return;
        fileLocation = pfileLocation;
        readTags();
    }
   
    //get info from mp3 files
    private void readTags(){
        try {
            File song = new File(fileLocation);
            FileInputStream file = new FileInputStream(song);
            int size = (int)song.length();
            file.skip(size - 128);
            byte[] last128 = new byte[128];
            file.read(last128);
            String id3 = new String(last128);
            String tag = id3.substring(0, 3);
            if (tag.equals("TAG")) {
                title = id3.substring(3, 32);
                artist = id3.substring(33, 62);
                album = id3.substring(63, 92);
                year = id3.substring(93, 97);
                comment = id3.substring(98,126);
                zeroByte = id3.substring(126,126);
                track = id3.substring(127,127);
                genre = id3.substring(128,128);
            } else
                JOptionPane.showMessageDialog(errorFrame,fileLocation + " does not contain ID3 information.","Error",JOptionPane.ERROR_MESSAGE);
            file.close();
        } catch (Exception e) {
            System.out.println("Error - " + e.toString());
        }
    }
    
    
    //getters
    public String getLocation(){
        return fileLocation;
    }    
    
    public String getTitle(){
    return title;
    }
    
    public String getArtist(){
        return artist;
    }
    
    public String getAlbum(){
        return album;
    }
    
    public String getYear(){
        return year;
    }
    
    public String getComment(){
        return comment;
    }
    
    public String getGenre(){
        return genre;
    }
    
    public String getTrack(){
        return track;
    }
    
    
    }
    

