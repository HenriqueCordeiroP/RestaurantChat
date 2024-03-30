import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Server implements Runnable {
    private MulticastSocket socket;
    private InetAddress address;

    private Server(String serverIp) throws IOException{
        this.socket = new MulticastSocket(Constants.PORT);
        this.address = InetAddress.getByName(serverIp);
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
        InetSocketAddress socketAddress = new InetSocketAddress(this.address , Constants.PORT);
        NetworkInterface networkInterface;
        try {
            networkInterface = NetworkInterface.getByInetAddress(this.address);
            this.socket.joinGroup(socketAddress, networkInterface);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}

