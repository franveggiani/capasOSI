package receptor.clases;

public class Monitor {

    boolean ack = false;
    String paquete;
    boolean paqueteDisponible = false;
    String resultadoAck;
    boolean estadoTransmision = true;

    // Utilizado por la capa física para esperar el ACK de la capa de enlace
    public synchronized void esperarConfirmacion(){
        while (!ack) {
            try{
                wait();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public synchronized void enviarConfirmacion(String valorACK){
        ack = true;
        notify();   
        this.resultadoAck = valorACK; 
    }

    public synchronized void enviarTrama(String paquete){
        this.paquete = paquete;
        paqueteDisponible = true;
        notify();
        // System.out.println("Trama enviada: " + paquete);
    }

    public synchronized String esperarTrama(){
        while (!paqueteDisponible) {
            try {
                wait();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        //Reseteo de estado
        paqueteDisponible = false;

        return paquete;
    }

    public void terminarTransmision(){
        estadoTransmision = false;
    }

}
