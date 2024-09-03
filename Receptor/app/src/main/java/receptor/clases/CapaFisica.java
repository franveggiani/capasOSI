package receptor.clases;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class CapaFisica implements Runnable {

    Monitor monitor;
    Socket transmisor;
    String trama;
    BufferedReader entrada;
    PrintWriter salida;
    boolean estadoTransmision = true;   // Cambiar esto para que finalice en algún momento
    String valorACK;

    @Override
    public void run() {

        boolean tramaRecibida = false;
        int intento = 0;

        while (monitor.estadoTransmision) {
            
            if (intento == 6) {
                monitor.terminarTransmision();
                System.out.println("XD");
            }
            
            while (!tramaRecibida) {               
                try {
                    recibirTrama();                 // Hilo espera a recibir la trama del receptor
                    monitor.enviarTrama(trama);     // Envia la trama a la capa física
                    
                    tramaRecibida = true;
                    
                } catch (SocketTimeoutException se) {
                    System.out.println("Tiempo de espera excedido: " + se.getMessage());
                    intento++;
                } catch (IOException e) {
                    System.out.println("Error: " + e.getMessage());
                    monitor.terminarTransmision();
                    break;
                }
            }

            monitor.esperarConfirmacion();          // Hilo espera al ACK de la capa de enlace
            
            // Enviar ACK
            valorACK = monitor.resultadoAck;        // Obtengo el ACK de la capa de enlace
            enviarConfirmacionATransmisor();        // Envio el ACK al transmisor como respuesta

            tramaRecibida = false;

            // System.out.println("Trama recibida: " + trama); Para probar si recibe la trama

            // Seteo un pequeño retardo para observar mejor el proceso. Escucha cada n milisegundos
            try {

                if (!estadoTransmision) {
                    break;
                }

                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        System.out.println("Fin de transmisión");

    }

    public CapaFisica(Socket transmisor, Monitor monitor, BufferedReader entrada, PrintWriter salida) {
        this.monitor = monitor;
        this.transmisor = transmisor;
        this.entrada = entrada;
        this.salida = salida;
    }

    public String recibirTrama() throws SocketTimeoutException, IOException {

        transmisor.setSoTimeout(5000);
        trama = entrada.readLine();

        return trama;

    }

    public void enviarConfirmacionATransmisor(){
        salida.println(valorACK);
    }

}
