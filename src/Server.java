import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;

public class Server implements Runnable {
    private final MulticastSocket socket = new MulticastSocket(Constants.PORT);;
    private InetAddress address;
    private InetSocketAddress socketAddress;

    private Server(String serverIp) throws IOException{
        this.address = InetAddress.getByName(serverIp);
        this.socketAddress = new InetSocketAddress(this.address, Constants.PORT);
    }
    public static void main(String[] args) throws IOException{
        System.out.print("Serviços:\n1. Bar\n2. Restaurante\n" +
                "[Servidor] Digite o número do serviço que deseja fornecer: ");
        Scanner sc = new Scanner(System.in);

        String serviceStr = sc.nextLine();
        int service = Integer.parseInt(serviceStr);
        if(service == 1){
            Server server = new Server(Constants.BAR_ADDRESS);
            Thread thread = new Thread(server);
            thread.start();
        } else if(service == 2) {
            Server server = new Server(Constants.KITCHEN_ADDRESS);
            Thread thread = new Thread(server);
            thread.start();
        }
    }

    @Override
    public void run() {
        NetworkInterface networkInterface;
        try {
            networkInterface = NetworkInterface.getByInetAddress(this.address);
            this.socket.joinGroup(this.socketAddress, networkInterface);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String incomingMessage = null;
        while(!Objects.equals(incomingMessage, "FINALIZAR SERVIDOR")){
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
                this.socket.receive(incomingPacket);
                incomingMessage = new String(incomingPacket.getData());

                byte[] outgoingMessage = this.formatMessage(incomingMessage).getBytes();
                DatagramPacket outgoingPacket = new DatagramPacket(
                        outgoingMessage,
                        outgoingMessage.length,
                        this.address,
                        4321);
                this.socket.send(outgoingPacket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String formatMessage(String message){
        String moment = new SimpleDateFormat("dd/MM/yyyy - HH:mm").format(new Date());
        return "[" + moment + "] " + message;
    }
}

