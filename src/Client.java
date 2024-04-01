import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Client implements Runnable {
    public final InetAddress defaultAddress = InetAddress.getByName(Constants.DEFAULT_SERVER_ADDRESS);
    public String name;
    public String targetAddress;

    private Client(String serverIp, String name) throws IOException {
        this.name = name;
        this.targetAddress = serverIp;
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        String outgoingMessage = "";
        int topicClass = 0;
        int topic = 0;
        String name = null;


        System.out.print("Insira seu nome: ");
        name = sc.nextLine();

        do{
            try{
                System.out.print("\n1. Serviço\n2. Pedidos\n" +
                        "Digite o número do tipo do tópico que deseja entrar: ");
                topicClass = Integer.parseInt(sc.nextLine());
            } catch(Exception e){
                continue;
            }
        }while(topicClass != 1 && topicClass != 2);

        if (topicClass == 1) {
            do{
                try{
                    System.out.print("\n1. Bar\n2. Cozinha\n" +
                            "Digite o número do serviço do produto que deseja oferecer: ");
                    topic = Integer.parseInt(sc.nextLine());
                } catch(Exception e){
                    continue;
                }
            }while(topic != 1 && topic != 2);

            serviceHandler(topic, name);

        } else {
            do{
                try{
                    System.out.print("\n1. Entradas\n2. Pratos Principais\n3. Sobremesas\n" +
                            "4. Bebidas\n5. Bebidas Alcoólicas\n" +
                            "Digite o número do tipo do produto que deseja pedir: ");
                    topic = Integer.parseInt(sc.nextLine());
                } catch(Exception e){
                    continue;
                }
            }while(topic != 1
                    && topic != 2
                    && topic != 3
                    && topic != 4
                    && topic != 5);

            orderHandler(topic, name);
        }
    }

    private static void serviceHandler(int topic, String name) throws IOException {
        Client client;
        Thread thread;
        switch(topic){
            case 1:
                client = new Client(Constants.BAR_ADDRESS, name);
                thread = new Thread(client);
                thread.start();
                break;
            case 2:
                client = new Client(Constants.KITCHEN_ADDRESS, name);
                thread = new Thread(client);
                thread.start();
                break;
        }
    }
    private static void orderHandler(int topic, String name) throws IOException {
        Client client;
        Thread thread;
        switch(topic){
            case 1: case 2: case 3:
                client = new Client(Constants.KITCHEN_ADDRESS, name);
                thread = new Thread(client);
                thread.start();
                break;
            case 4: case 5:
                client = new Client(Constants.BAR_ADDRESS, name);
                thread = new Thread(client);
                thread.start();
                break;
        }
    }

    @Override
    public void run() {
        Thread receiverThread = null;
        Thread senderThread = null;
        try {
            receiverThread = new Thread(new Receiver(this));
            senderThread = new Thread(new Sender(this));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        receiverThread.start();
        senderThread.start();
        try {
            receiverThread.join();
            senderThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class Receiver implements Runnable {
        private final MulticastSocket socket = new MulticastSocket(Constants.PORT);
        public Receiver(Client client) throws IOException {
            InetAddress address = InetAddress.getByName(client.targetAddress);
            InetSocketAddress socketAddress = new InetSocketAddress(address, Constants.PORT);
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(address);
            this.socket.joinGroup(socketAddress, networkInterface);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String incomingMessage = new String(packet.getData());
                    System.out.println("\n" + incomingMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Sender implements Runnable {
        private final MulticastSocket socket = new MulticastSocket(Constants.PORT);
        Client client;

        public Sender(Client client) throws IOException {
            this.client = client;
            InetAddress defaultAddress = InetAddress.getByName(Constants.DEFAULT_SERVER_ADDRESS);
            InetSocketAddress socketAddress = new InetSocketAddress(client.defaultAddress, Constants.PORT);
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(client.defaultAddress);
            this.socket.joinGroup(socketAddress, networkInterface);
        }

        @Override
        public void run() {
            try {
                joinServer(client);
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    System.out.print("\nDigite sua mensagem: ");
                    String message = scanner.nextLine();
                    if(message.equals("sair")){
                        message =  client.name + "&!%" + "leave-server" + "&!%" + client.targetAddress;;
                        sendMessage(message, client);
                        break;
                    } else {
                        message = client.name + "&!%" + message + "&!%" + client.targetAddress;
                        sendMessage(message, client);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void joinServer(Client client) throws IOException {
            String message = client.name + "&!%" + "join-server" + "&!%" + client.targetAddress;
            byte[] messageBytes = message.getBytes();
            DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, client.defaultAddress, Constants.PORT);
            socket.send(packet);
        }

        private void sendMessage(String message, Client client) throws IOException {
            byte[] messageBytes = message.getBytes();
            DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, client.defaultAddress, Constants.PORT);
            socket.send(packet);
        }
    }
}

// TODO
// fazer thread de leitura fechar quando o cliente sair (De escrita já fecha)
// 
// Opção de sair -> fecha o socket e acaba o programa ou escolhe um novo tópico? escolhe um novo topico

