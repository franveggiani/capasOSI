package transmisor.clases;

import java.util.ArrayList;
import java.util.zip.CRC32;

import transmisor.interfaces.InterfazCapaDeEnlace;

public class CapaDeEnlace implements InterfazCapaDeEnlace, Runnable {

    String input;
    ArrayList<String> tramasList;
    Monitor monitor;    // Clase para comunicar hilos

    public CapaDeEnlace(String input, Monitor monitor){ this.input = input; this.monitor = monitor; }
    
    // Esta es la secuencia de ejecución del Thread
    @Override
    public void run() {
        
        entramado(input);   // Genera una lista de entramados con los caracteres de las palabras

        int contadorTramas = 0;
        boolean reenvioNecesario;

        for (String trama : tramasList){

            contadorTramas++;

            do{
                reenvioNecesario = false;

                // Envio la trama al monitor, que después va a recibir la capa física
                monitor.enviarTrama(trama);

                // Si es la ultima trama, entonces le dice a la capa física que deje de escuchar
                if (contadorTramas == tramasList.size()) {
                    monitor.terminarTransmision();
                }

                // Hilo queda en espera de la confirmación para seguir mandando tramas
                monitor.recibirConfirmacion();

                // Si el flag "reenvioNecesario = true" hay que reenviar la trama
                if (monitor.reenvioNecesario) {
                    reenvioNecesario = true;
                    System.out.println("Reenviando la trama " + contadorTramas + "...");
                    //Esto es opcional, para observar bien cuándo se produce un reenvío
                    try{
                        Thread.sleep(2000);
                    } catch(Exception e) {
                        System.out.println(e.getMessage());
                    }
                }

            } while (reenvioNecesario);


            System.out.println("Confirmación recibida: Trama: " + contadorTramas + "\n");
        }

        monitor.terminarTransmision();
        System.out.println("Fin");


    }

    public void setInput(String input){
        this.input = input;
    }

    // Realización del entramado
    @Override
    public void entramado(String inputF) {
        
        String input = inputF;
        byte[] inputEnBytes = input.getBytes();

        String trama;

        ArrayList<String> tramas = new ArrayList<>();

        //Inicializamos CRC32 (Detección de errores)
        CRC32 crc32 = new CRC32();

        //El primer paquete va a tener el valor 0, el siguiente 1.
        char nroPaquete = '0';

        for (byte b : inputEnBytes){

            //Inicialmente cada elemento b está en código ASCII

            //La operación b & 0xFF convierte el formato de entero con signo a entero sin signo. Saca el bit de signo.
            //Integer.toBinaryString convierte el el entero sin signo a su representación binaria
            String caracterbin = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace( ' ', '0');

            //tramas.add(binario);

            //Crea el código CRC32 para detección de errores
            crc32.update(b);

            //Obtengo el valor del código CRC en binario 
            Long valorcrc = crc32.getValue();
            String crcbin = String.format("%32s", Long.toBinaryString(valorcrc)).replace(' ', '0');

            //Si el numero de paquete es 1, entonces lo cambia a 0. Sino, lo deja en 1,
            nroPaquete = nroPaquete == '1' ? '0' : '1';
            
            // 1 bit + 32 bits + 8 bits = 41 bits
            trama = nroPaquete + crcbin + caracterbin;

            tramas.add(trama);
            //System.out.println(trama);
        }

        tramasList = tramas;

    }

}
