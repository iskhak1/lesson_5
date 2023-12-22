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

    private void broadcastMessage(String mesageToSend) {
        for(ClientManager client: clients){
            try{
                if(!client.name.equals(name)){
                    client.bufferedWriter.write(mesageToSend);
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
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket,bufferedWriter,bufferedReader);
                break;
            }
        }
    }
}
