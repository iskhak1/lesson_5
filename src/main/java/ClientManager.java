import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientManager implements Runnable {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter ;
    private String name;
    public static ArrayList<ClientManager> clients = new ArrayList<>();
    public ClientManager(Socket socket) {
        try{
            this.socket = socket;
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            name = bufferedReader.readLine();
            clients.add(this);
            broadcastMessage("Server: " +name+" connected to chat");

        } catch (IOException e) {
            closeEverything(socket,bufferedWriter,bufferedReader);
        }
    }

    private void closeEverything(Socket socket, BufferedWriter bufferedWriter, BufferedReader bufferedReader) {
      removeClient();
      try{
          if(bufferedReader != null){
              bufferedReader.close();
          }
          if(bufferedWriter != null){
              bufferedReader.close();
          }
          if(socket != null){
              socket.close();
          }
      } catch (IOException e) {
          e.printStackTrace();
      }
    }

    private void removeClient() {
        clients.remove(this);
        broadcastMessage("Server: " +name+" leave chat");
    }

    private void broadcastMessage(String message) {

        for(ClientManager client: clients){
            try{

                //group chat
                    if(!client.name.equals(name) ){
                    client.bufferedWriter.write(message);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();

                }
            } catch (IOException e) {
                closeEverything(socket,bufferedWriter,bufferedReader);
            }
        }
    }

    private void privateMessage(String message) {
        String[] messages = message.split(" ");
        StringBuilder messageToSend = new StringBuilder();
        StringBuilder nick = new StringBuilder();
        for (int i = 1; i < messages[1].split("").length; i++) {
            nick.append(messages[1].split("")[i]);
        }
        for (int i = 2; i < messages.length; i++) {
            messageToSend.append(messages[i] + " ");
        }

        for(ClientManager client: clients){
            try{
                //private chat
                    if(!client.name.equals(name) && messages[1].split("")[0].startsWith("@") && client.name.equals(nick.toString())) {
                        client.bufferedWriter.write(messages[0] + " " + messageToSend.toString());
                        client.bufferedWriter.newLine();
                        client.bufferedWriter.flush();
                    }
            } catch (IOException e) {
                closeEverything(socket,bufferedWriter,bufferedReader);
            }
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while(socket.isConnected()){
            try{
                messageFromClient = bufferedReader.readLine();
                if(messageFromClient.split(" ")[1].split("")[0].startsWith("@")){
                    System.out.println("Private Chat "+messageFromClient.split(" ")[1].split("")[0]);
                    privateMessage(messageFromClient);
                }else {
                    System.out.println("Message for all " + messageFromClient.split(" ")[1].split("")[0]);
                    broadcastMessage(messageFromClient);
                }
            } catch (IOException e) {
                closeEverything(socket,bufferedWriter,bufferedReader);
                break;
            }
        }
    }
}
