import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;


public class Server implements Runnable {
    private final MulticastSocket socket = new MulticastSocket(Constants.PORT);;
    private InetAddress address;
    private InetSocketAddress socketAddress;
    private NetworkInterface networkInterface;

    private Server(String serverIp) throws IOException{
        this.address = InetAddress.getByName(serverIp);
        this.socketAddress = new InetSocketAddress(this.address, Constants.PORT);
        this.networkInterface = NetworkInterface.getByInetAddress(this.address);
        this.socket.joinGroup(this.socketAddress, this.networkInterface);
    }
    public static void main(String[] args) throws IOException{
        Server server = new Server(Constants.DEFAULT_SERVER_ADDRESS);
        Thread thread = new Thread(server);
        thread.start();

    }

    @Override
    public void run() {
        String name = "";
        String body = "";
        InetAddress address = null;
        do{
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String incomingMessage = new String(packet.getData());
                try {
                    name = getNameFromMessage(incomingMessage);
                    body = getBodyFromMessage(incomingMessage);
                    address = getAddressFromMessage(incomingMessage);
                } catch(ArrayIndexOutOfBoundsException ae){
                    System.out.println("Invalid Message: " + incomingMessage);
                    System.out.println("Skipping...");
                }
                byte[] outgoingMessage = null;
                if(body.equals("join-server")){
                    outgoingMessage = this.formatMessage(name, "conectou ao servidor").getBytes();
                    System.out.println("[CONNECT] " + name + " joined " + getAddressString(incomingMessage));
                } else if(body.equals("leave-server")){
                    outgoingMessage = this.formatMessage(name, "desconectou do servidor").getBytes();
                    System.out.println("[DISCONNECT] " + name + " left " + getAddressString(incomingMessage));
                } else if(body.equals("FINALIZAR SERVIDOR")){
                    outgoingMessage = this.formatMessage(name, "finalizou o servidor").getBytes();
                    System.out.println("[END SERVER] " + name + " has finished the server");
                } else {
                    outgoingMessage = this.formatMessage(name, body).getBytes();
                    System.out.println("[MESSAGE] " + name + " sent a message to " + getAddressString(incomingMessage));
                }
                forwardMessage(outgoingMessage, address);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }while(!Objects.equals(body, "FINALIZAR SERVIDOR"));
    }

    private String formatMessage(String name, String message){
        String moment = new SimpleDateFormat("dd/MM/yyyy - HH:mm").format(new Date());
        return "[" + moment + "] " + name + ": " + message;
    }

    private String getNameFromMessage(String message){
        String delimiter = "&!%";
        String[] parts = message.split(delimiter);
        return parts[0];
    }

    private String getBodyFromMessage(String message){
        String delimiter = "&!%";
        String[] parts = message.split(delimiter);
        return parts[1];
    }

    private InetAddress getAddressFromMessage(String message) throws UnknownHostException {
        String ip = getAddressString(message).trim();
        return InetAddress.getByName(ip);
    }

    private String getAddressString(String message) throws UnknownHostException {
        String delimiter = "&!%";
        String[] parts = message.trim().split(delimiter);
        if (parts.length > 2) {
            return parts[2].trim();
        }
        else {
            throw new UnknownHostException("Invalid message format: " + message);
        }
    }
    private void forwardMessage(byte[] message, InetAddress address) throws IOException {
        DatagramPacket outgoingPacket = new DatagramPacket(
                message,
                message.length,
                address,
                Constants.PORT);
        this.socket.send(outgoingPacket);
    }
}