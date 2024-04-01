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
        String incomingMessage = "";
        Server server = new Server(Constants.DEFAULT_SERVER_ADDRESS);
        Thread thread = new Thread(server);
        thread.start();

    }

    @Override
    public void run() {
        String body = "";
        do{
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String incomingMessage = new String(packet.getData());

                String name = getNameFromMessage(incomingMessage);
                body = getBodyFromMessage(incomingMessage);
                InetAddress address = getAddressFromMessage(incomingMessage);
                byte[] outgoingMessage = this.formatMessage(name, body).getBytes();
                DatagramPacket outgoingPacket = new DatagramPacket(
                        outgoingMessage,
                        outgoingMessage.length,
                        address,
                        4321);
                this.socket.send(outgoingPacket);
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
        System.out.println(message);
        String delimiter = "&!%";
        String[] parts = message.trim().split(delimiter);
        if (parts.length > 2) {
            String ip = parts[2].trim();
            return InetAddress.getByName(ip);
        }
        else {
            throw new UnknownHostException("Invalid message format: " + message);
        }
    }

}
