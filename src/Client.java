import java.io.IOException;
import java.net.*;
import java.util.Objects;
import java.util.Scanner;

public class Client implements Runnable {
    private final MulticastSocket socket = new MulticastSocket(4321);
    private InetAddress address;
    private InetSocketAddress socketAddress;
    private NetworkInterface networkInterface;

    private Client(String serverIp) throws IOException {
        this.address = InetAddress.getByName(serverIp);
        this.socketAddress = new InetSocketAddress(this.address, Constants.PORT);
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        String outgoingMessage = null;
        int topic = 0;
        String name = null;

        System.out.print("Insira seu nome: ");
        name = sc.nextLine();
        while(!outgoingMessage.equals("SAIR")) {
            System.out.print("Tópicos:\n1. Entradas\n2. Pratos Principais\n3. Sobremesas\n" +
                    "4. Bebidas\n5. Bebidas Alcoólicas\n" +
                    "Digite o número do tipo do produto que deseja pedir: ");
            topic = Integer.parseInt(sc.nextLine());
            if (topic == 1){
                Client client = new Client(Constants.BAR_ADDRESS);
                Thread thread = new Thread(client);
                thread.start();
            }
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
                System.out.println(incomingMessage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
