package transmisor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import transmisor.clases.CapaDeEnlace;
import transmisor.clases.CapaFisica;
import transmisor.clases.Monitor;

public class Transmisor {
    public static void main(String[] args) throws Exception {
        // CapaDeEnlace capaDeEnlace = new CapaDeEnlace();
        // capaDeEnlace.entramado("Hola");

        Monitor monitor = new Monitor();
        String input = "HOLA";
        Socket receptorSocket;

        try {

            receptorSocket = new Socket("localhost", 4009);

            BufferedReader entrada = new BufferedReader(new InputStreamReader(receptorSocket.getInputStream()));
            PrintWriter salida = new PrintWriter(receptorSocket.getOutputStream(), true);
            
            new Thread(new CapaDeEnlace(input, monitor)).start();
            new Thread(new CapaFisica(monitor, salida, entrada, receptorSocket)).start();
        
        } catch(IOException e){
            System.out.println(e.getMessage());
        }

    }
}
