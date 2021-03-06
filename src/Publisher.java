import com.mpatric.mp3agic.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class Publisher implements Serializable{
    private ArrayList<String> artists;
    private static final String DIR="dataset";
    private Message msg;
    public int port;



    public Publisher(String filepath,int port) {

        artists=getListofArtist(filepath);
        System.out.println("\nArtists Loaded");
        this.port=port;
        this.msg=new Message(this);
        System.out.println(Node.getBrokers().size());

    }


    public void push() {
    }





    public ArrayList<String> getListofArtist(String filepath){
        ArrayList<String> tempList = new ArrayList<>();
        File dir=new File(filepath);
        System.out.println("Loading Artists");
        int i=0;
        for (File temp: dir.listFiles()){
            printProgressBar(++i);
            GlobalFunctions gf=new GlobalFunctions();
            String artist=gf.getMp3Metadata(temp).get("xmpDM:artist");
            if(!tempList.contains(artist) && artist!=null)tempList.add(artist);
        }

        return tempList;
    }


    public void printProgressBar(int i) {
        if (i % 5 == 0) System.out.print("#");
    }

    public ArrayList<String> getArtists() {
        return artists;
    }

    public Message getMsg() {
        return msg;
    }
}
