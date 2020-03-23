import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ConsumerHandler extends Thread{
    Message answer;
    Boolean check;


    Consumer consumer;
    public ConsumerHandler(Consumer consumer){
        this.consumer=consumer;
    }
    void connect(int port) {
        Socket requestSocket=null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {

            requestSocket = new Socket("127.0.0.1", consumer.port);
            out = new ObjectOutputStream(requestSocket.getOutputStream());

            System.out.println("Message created.");
            out.writeObject(consumer.request);
            in = new ObjectInputStream(requestSocket.getInputStream());
            try {
                answer=(Message)in.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
             check=answer.check;

            if(check==false){
                System.out.println("I change server");

                Consumer NEW =new Consumer(answer.artist,answer.port);
                new ConsumerHandler(NEW).start();
                try {
                    System.out.println("Server Changed");
                    this.join();
                    System.out.println("stopped");

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }else{
                System.out.println(answer.song+" of "+this.consumer.artist);
            }
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
    public void run(){
        connect(consumer.port);
    }
    public static void main(String args[]) {
        Consumer a=new Consumer("Kevin MacLeod");
        Consumer b=new Consumer("Alexander Narakada");
       // Consumer c=new Consumer("Alexander Narakada");

        new ConsumerHandler(a).start();
       //new ConsumerHandler(b).start();
      //  new ConsumerHandler(c).start();

    }
}
