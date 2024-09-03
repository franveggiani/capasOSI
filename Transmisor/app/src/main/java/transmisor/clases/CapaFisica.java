package transmisor.clases;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

import transmisor.interfaces.InterfazCapaFisica;

public class CapaFisica implements InterfazCapaFisica, Runnable {
    
    public CapaFisica(Monitor monitor, PrintWriter salida, BufferedReader entrada, Socket receptor) {
        this.monitor = monitor;
        this.salida = salida;
        this.entrada = entrada;
        this.receptor = receptor;
    }
    
    Monitor monitor;
    String trama;
    PrintWriter salida;
    BufferedReader entrada;
    String ack;
    Socket receptor;
    int numeroIntentos = 5;
    int segundosEspera = 5000;


    @Override
    public void run() {

        while (monitor.estadoTransmision) {

            // Hilo espera a que la capa de enlace le envíe la trama a enviar a
            // través del Monitor
            trama = monitor.esperarTrama();

            // Cuando obtiene el trama, lo envía al receptor
            if (trama != null) {
                System.out.println("Trama recibida en la capa física " + trama);
                enviarTrama(trama);
            } else {
                System.out.println("No se recibió ninguna trama");
            }

            // Recibir la confirmación (ACK).
            int intentos = 0;
            boolean confirmacionExitosa = false;

            // El while es para implementar el timeout y la cantidad de intentos
            while (intentos < numeroIntentos && !confirmacionExitosa) { 
                intentos++;
                // System.out.println(intentos);
                try {
                    // Hilo revisa si llega el ACK del receptor por una cantidad de segundos
                    recibirConfirmacionConTimeOut();                    

                    //Se confirma el paquete correctamente
                    if (ack != null && ack.equals("1")) {
                        // System.out.println("Confirmación exitosa en el intento " + intentos);
                        monitor.confirmacionRecibida();
                        ack = null;
                        confirmacionExitosa = true;
                        intentos = 0;
                    } else {                                            
                        // El ACK es distinto de 1, entonces hay error en el paquete
                        // Programar reenvío
                        System.out.println("Error con la trama: Reenviar");
                        monitor.solicitarReenvioPaquete();  // Setea flag "reenvioNecesario = true".
                                                            // Después, la capa de enlace usa esta flag para reenviar el proceso

                        monitor.confirmacionRecibida();     // Setea flag "ack = true" y destraba el proceso

                        // Se sale el bucle while
                        ack = null;
                        confirmacionExitosa = true;     
                        intentos = 0;
                    }

                } catch (SocketTimeoutException se) {                   
                    // Se supera el tiempo de espera y se vuelve a intentar
                    System.out.println("Tiempo máximo alcanzado: " + se.getMessage() + ". Intento: " + intentos);
                } catch (IOException e) {
                    System.out.println("ERROR: " + e.getMessage());
                    break;
                }

                if (intentos == numeroIntentos) {
                    System.out.println("No se pudo establecer conexión");
                    monitor.terminarTransmision();
                    break;
                }

            }
        }

        //System.out.println("¡Transmision finalizada!");

    }

    @Override
    public void enviarTrama(String trama) {
        salida.println(trama);
    }

    @Override
    public void recibirConfirmacion() {
        try {
            ack = entrada.readLine();
        } catch (IOException e) {
            e.getMessage();
            e.printStackTrace();
        }
    }

    public void recibirConfirmacionConTimeOut() throws SocketTimeoutException, IOException {
        // Espera 5 segundos a que reaccione el receptor
        receptor.setSoTimeout(segundosEspera);

        // Si reacciona, recibe el ACK
        ack = entrada.readLine();
    }

}
