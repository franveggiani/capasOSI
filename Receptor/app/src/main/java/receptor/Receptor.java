package receptor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import receptor.clases.CapaDeEnlace;
import receptor.clases.CapaFisica;
import receptor.clases.Monitor;

public class Receptor {
    public static void main(String[] args) {

        ServerSocket receptor;
        Monitor monitor = new Monitor();

        try {
            
            receptor = new ServerSocket(4009);

            System.out.println("Esperando al transmisor");
            Socket transmisor = receptor.accept();
            System.out.println("Transmisor conectado: " + transmisor.getInetAddress());
            
            BufferedReader entrada = new BufferedReader(new InputStreamReader(transmisor.getInputStream()));
            PrintWriter salida = new PrintWriter(transmisor.getOutputStream(), true);

            new Thread(new CapaFisica(transmisor, monitor, entrada, salida)).start();
            new Thread(new CapaDeEnlace(monitor)).start();

            receptor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
