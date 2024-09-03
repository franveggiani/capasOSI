package receptor.clases;

import java.util.zip.CRC32;

public class CapaDeEnlace implements Runnable {

    public CapaDeEnlace(Monitor monitor){
        this.monitor = monitor;
    }

    boolean estadoTransmision = true;
    Monitor monitor;
    String trama;
    String valorACK;

    char nroPaquete = 0;
    char nroPaqueteAnt = 0;

    int INDEX_COMIENZO_DATOS = 33;
    int INDEX_COMIENZO_CRC = 1;
    int INDEX_FIN_CRC = 33;

    int contadorPaquete = 0;


    @Override
    public void run() {
        
        String crcRecibido;
        String datos;
        int bytesRecibidos = 0;
        CRC32 crc32 = new CRC32();

        while (monitor.estadoTransmision) {
            trama = monitor.esperarTrama();     // Espera y recibe la trama de la capa física

            System.out.println("Trama recibida: " + trama);

            nroPaquete = trama.charAt(0);
            // System.out.println(nroPaquete);

            // Me fijo la secuencia de paquetes
            // Si el paquete actual empieza con 1, y el anterior también, entonces se perdió un paquete.
            if (!(nroPaquete != nroPaqueteAnt)) {
                System.out.println("Los paquetes son los mismos");
                enviarConfirmacion(false);
            } else {
                nroPaqueteAnt = nroPaquete;
                enviarConfirmacion(true);
            }

            datos = trama.substring(INDEX_COMIENZO_DATOS);
            crcRecibido = trama.substring(INDEX_COMIENZO_CRC, INDEX_FIN_CRC);

            // System.out.println("Datos: " + datos + ". CRC: " + crcRecibido + ".");

            // Obteniendo CRC32 para detección de errores
            // Obtengo el CRC a partir de los datos recibidos
            byte datosByte = (byte) Integer.parseInt(datos, 2);     // Convierte los datos a formato Byte para poder usarlo
            crc32.update(datosByte);                                      // Calcula CRC32 con los datos recibidos
            
            // Convierto el CRC32 obtenido a una cadena binaria
            Long valorcrc32 = crc32.getValue(); 
            String crc32bin = String.format("%32s", Long.toBinaryString(valorcrc32)).replace(' ', '0');

            // Comparo, si no son iguales, entonces solicito reenviar el paquete.
            if (crc32bin.equals(crcRecibido)) {
                // System.out.println("No hay errores en la trama recibida");
                enviarConfirmacion(true);
            } else {
                System.out.println("Hay errores en la trama: ");
                System.out.println("CRC Recibido: " + crcRecibido);
                System.out.println("CRC Calculado: " + crc32bin);

                enviarConfirmacion(false);
            }

            char caracterRecibido = (char) datosByte;

            System.out.println("Caracter recibido: " + caracterRecibido + ". En binario: " + datos + "\n");
            bytesRecibidos++;

            monitor.enviarConfirmacion(valorACK);   // Envia la confirmación a la capa física 
        }

    }

    public void enviarConfirmacion(boolean resultado){
        if (resultado) {
            valorACK = "1";
        } else {
            valorACK = "0";
        }
    }



}
