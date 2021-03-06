import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BrokerHandler extends Thread implements Serializable {


    ObjectInputStream in;
    ObjectOutputStream out;
    String f;
    BigInteger theirKeys;
    Broker broker;
    Object e;
    Message request;
    Socket Stopcon=null;
    String Song;


    public BrokerHandler(Broker broker) throws NullPointerException{
        Stopcon=broker.getConnection();
        try {

            in = new ObjectInputStream(Stopcon.getInputStream());
            out =new ObjectOutputStream(Stopcon.getOutputStream());
            out.flush();
            try {
                this.request=(Message)in.readObject();
                this.f=request.artist;
                this.e =request.entity;
                this.broker=broker;
                this.Song=request.song;

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void run(){
        if(this.e instanceof Consumer){
            calculateMessageKeys(this.request);
            checkBroker(this.broker,(Consumer) e);


        }
        if(this.e instanceof Publisher){
            checkPublisher((Publisher) e);

        }
        else if (this.e instanceof Publisher){
            System.out.println("This is a publisher");
        }

    }
    public void pull(){
        Publisher correctPublisher = null;
        if (broker.GetPublishers().size()!=0) {
            for (Publisher publisher : broker.GetPublishers()) {
                if (publisher.getArtists().contains(e)) {
                    correctPublisher = publisher;
                }
                Socket requestSocket = null;
                ObjectOutputStream out = null;
                ObjectInputStream in = null;

                try {
                    requestSocket = new Socket("127.0.0.1", correctPublisher.port);
                    out = new ObjectOutputStream(requestSocket.getOutputStream());
                    in = new ObjectInputStream(requestSocket.getInputStream());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                 Message songRequest=new Message(this.f,this.Song);
                try {
                    System.out.println("Fetching song");
                    out.writeObject(songRequest);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }


            }
        }
    }
    public  void disconnectClient(Socket connection){
        try {
            in.close();
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }finally {
            try {
                connection.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    public void checkPublisher(Publisher publisher){
        boolean add=false;
           if(!this.broker.GetPublishers().contains(publisher)){
               for(String artist:publisher.getArtists()){
                   if(calculateArtistKeys(artist)<=broker.myKeys.intValue()&&calculateArtistKeys(artist)>=broker.myKeys.intValue()-11){
                       add=true;
                   }
               }
               if(add){
                   broker.GetPublishers().add(publisher);
               }
           }
    }



    public  void checkBroker(Broker broker,Consumer consumer) {


        int intMyKeys = broker.myKeys.intValue();
        int intTheirKeys = theirKeys.intValue();
        if (intTheirKeys > 23) {
            intTheirKeys = intTheirKeys % 23;
        }
        if (intTheirKeys <= intMyKeys && intTheirKeys >= intMyKeys - 11) {
            consumer.Register(broker, f);
            System.out.println(broker.Name + "Client Connected and Registered");
            Message answer=(new Message("what song would u like to listen to?"));
            try {
                out.writeObject(answer);
            } catch (IOException ex) {
                ex.printStackTrace();
           }

        } else {
            int thePort = 0;
            System.out.println(broker.Name + "Client changing server");
            for (Broker broker1 : Node.getBrokers()) {
                int KEYS = broker1.myKeys.intValue();
                if (intTheirKeys <= KEYS && intTheirKeys >= KEYS - 11) {
                    thePort = broker1.port;
                    System.out.println(thePort);
                }
            }
            Message answer=new Message(this.f,thePort,false);
            try {
                out.writeObject(answer);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            disconnectClient(Stopcon);


        }
    }
    public int calculateArtistKeys(String Artist){
        int artistKeys;
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        m.reset();
        m.update(Artist.getBytes());
        byte[] digest = m.digest();
         BigInteger Keys = new BigInteger(1,digest);
        BigInteger mod=new BigInteger("25");
        Keys=Keys.mod(mod);
        artistKeys=Keys.intValue();
        if(artistKeys>23){
            artistKeys=artistKeys%23;
        }
        return artistKeys;

    }
    public  void calculateMessageKeys(Message request)  {
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        m.reset();
        m.update(f.getBytes());
        byte[] digest = m.digest();
        theirKeys = new BigInteger(1,digest);
        BigInteger mod=new BigInteger("25");
        theirKeys=theirKeys.mod(mod);
        System.out.println(theirKeys);
    }
}
