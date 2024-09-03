package transmisor.clases;

// La clase Monitor sirve para comunicar ambos hilos Capa de Enlace y Capa física.

public class Monitor {

    //Flags

    boolean ack = false;
    String paquete;
    boolean paqueteDisponible = false;
    boolean estadoTransmision = true;
    boolean reenvioNecesario = false;

    // Función que usa la C. de enlace para esperar confirmación y enviar siguiente paquete
    // El hilo espera hasta que es notificado
    public synchronized void recibirConfirmacion(){
        while (!ack) {
            try{
                wait();
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        //Reseteo del ACK
        ack = false;
    }

    // Función que usa la C. física para avisar que recibió el ACK
    // Destraba el hilo de Capa de enlace que espera la confirmación
    public synchronized void confirmacionRecibida(){
        ack = true;
        notify();
    }

    // Función que usa la C. de enlace para avisar que hay un paquete disponible a enviar.
    // Destraba el hilo de C. física para que tome el paquete
    public synchronized void enviarTrama(String paquete){
        this.paquete = paquete;
        paqueteDisponible = true;
        notify();
        if (reenvioNecesario) {
            reenvioNecesario = false;
            ack = false;
        }
        //System.out.println("Trama enviada: " + paquete);
    }

    // Función que usa la C. física para esperar la trama de la capa de enlace
    // Destraba el hilo y toma el paquete
    public synchronized String esperarTrama(){
        while (!paqueteDisponible) {
            try {
                wait();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        // Reseteo de estado
        // Si no es necesario reenviar el paquete, entonces se desactiva la flag. 
        // Sino, es necesario tener el paquete anterior y no resetar.
        if (!reenvioNecesario) {
            paqueteDisponible = false;
        }

        return paquete;
    }

    public void terminarTransmision(){
        estadoTransmision = false;
    }

    public boolean getEstadoTransmision(){
        return estadoTransmision;
    }
    
    // La usa la C. física para solicitar el reenvío a la capa de enlace
    public void solicitarReenvioPaquete(){
        reenvioNecesario = true;
    }

}
