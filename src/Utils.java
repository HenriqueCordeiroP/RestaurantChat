import java.util.Scanner;

public class Utils {

    public static int pickTopicClass() {
        int topicClass = 0;
        Scanner sc = new Scanner(System.in);

        do {
            try {
                System.out.print("\n1. Serviço\n2. Pedidos\n" +
                        "Digite o número do tipo do tópico que deseja entrar: ");
                topicClass = Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                continue;
            }
        } while (topicClass != 1 && topicClass != 2);

        return topicClass;
    }

    public static int pickServiceTopic(){
        Scanner sc = new Scanner(System.in);
        int topic = 0;
        do{
            try{
                System.out.print("\n1. Bar\n2. Cozinha\n" +
                        "Digite o número do serviço do produto que deseja oferecer: ");
                topic = Integer.parseInt(sc.nextLine());
            } catch(Exception e){
                continue;
            }
        }while(topic != 1 && topic != 2);

        return topic;
    }

    public static int pickOrderTopic(){
        Scanner sc = new Scanner(System.in);
        int topic = 0;

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

        return topic;
    }
}
