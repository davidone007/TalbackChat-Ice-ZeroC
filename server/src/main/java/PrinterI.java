import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zeroc.Ice.Current;

import Demo.CallbackPrx;

//import Demo.Response;

public class PrinterI implements Demo.Printer {

    private List<String> registeredClients = new ArrayList<>();

    private Map<String, CallbackPrx> clientProxies = new HashMap<>();

    // -------------- Aquí gestionamos el menú -----------------
    @Override
    public void message(CallbackPrx proxy, String s, Current current) {
        // proxy.reportResponse("Hola mundo");
        long startTime, endTime;
        startTime = System.nanoTime();
        String[] parts = s.split(":", 2); // esto serpara el username y el hostaname del mensaje
        String userHost = parts[0];
        String message = parts[1];
        String clientResult = ""; // Para enviar al cliente
        String result = ""; // Este es el del servidor

        // ------- Para registrar clientes ---------------
        if (!registeredClients.contains(userHost)) {
            registeredClients.add(userHost);
            clientProxies.put(userHost, proxy);
        }

        // ---------------------- Punto 2a -----------------------
        if (message.matches("\\d+")) { // Si es un número entero positivo
            int n = Integer.parseInt(message);
            result = userHost + " Fibonacci series: " + fibonacci(n) + "\n  Prime factors: " + primeFactors(n);

            // pal client :)
            clientResult = userHost + " Prime factors: " + primeFactors(n);

            // ---------------------- Punto 2b -----------------------
        } else if (message.startsWith("listifs")) { // Listar interfaces lógicas
            // Servidor y cliente reciben lo mismo
            result = userHost + " Interfaces: " + listInterfaces();
            clientResult = result;

            // ---------------------- Punto 2c -----------------------
        } else if (message.startsWith("listports")) { // Listar puertos abiertos
            String[] cmdParts = message.split(" ");
            // Aquí en el segundo también reciben lo mismo
            if (cmdParts.length > 1) {
                String ipAddress = cmdParts[1];
                result = userHost + " Open Ports: " + listOpenPorts(ipAddress);
                clientResult = result;
            } else {
                result = userHost + " Error: IP address is missing for listports command.";
                clientResult = result;
            }

            // ---------------------- Punto 2d -----------------------
        } else if (message.startsWith("!")) { // Ejecutar un comando
            String command = message.substring(1);
            result = userHost + " Command output: " + executeCommand(command);
            clientResult = result;

            // ----------------- list clients --------------

        } else if (message.equalsIgnoreCase("list clients")) {
            result = "Registered clients: " + listRegisteredClients(current);
            clientResult = result;

            // --------------------- Mensaje directo ----------------
        } else if (message.startsWith("to ")) {
            result = sendDirectMessage(userHost, message);
            clientResult = result;

            // ------------------- Mensaje Broadcast -----------------

        } else if (message.startsWith("BC")) {
            String broadcastMessage = message.substring(3); // Mensaje que sigue de "BC"
            broadcastMessage(userHost, broadcastMessage, current);
            result = "Broadcasting message sent!!";
            clientResult = result;

        } else {
            result = "Unknown command.";
            clientResult = result;

        }

        endTime = System.nanoTime();
        result += "\n  Time of operation: " + ((endTime - startTime) / 1_000_000_000.0) + " seconds";

        // El servidor imprime todo
        System.out.println(result);

        proxy.reportResponse(clientResult);
    }

    // ---------------------- Métodos para el menú ---------------------------------

    /**
     * Calcula los factores primos de un número dado.
     *
     * @param n el número entero del cual se obtendrán los factores primos.
     * @return una lista de enteros que representan los factores primos de n.
     *
     *         Ejemplo: si n = 28, devuelve [2, 2, 7].
     */
    private List<Integer> primeFactors(int n) {
        List<Integer> factors = new ArrayList<>();
        while (n % 2 == 0) {
            factors.add(2);
            n /= 2;
        }
        for (int i = 3; i <= Math.sqrt(n); i += 2) {
            while (n % i == 0) {
                factors.add(i);
                n /= i;
            }
        }
        if (n > 2) {
            factors.add(n);
        }
        return factors;
    }

    /**
     * Escanea los puertos abiertos en una dirección IP utilizando el comando nmap.
     *
     * @param ipAddress la dirección IP a escanear.
     * @return una cadena con la lista de puertos abiertos o un mensaje indicando si
     *         no se encontraron puertos abiertos.
     *         En caso de error, devuelve un mensaje de error.
     *
     *         Ejemplo: si ipAddress es "192.168.1.1", entonces devuelve los puertos
     *         abiertos de esa dirección ip.
     */
    private String listOpenPorts(String ipAddress) {
        StringBuilder openPorts = new StringBuilder();
        try {
            String command = "nmap -p 1-65535 " + ipAddress;
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean foundOpenPorts = false;
            while ((line = reader.readLine()) != null) {
                openPorts.append(line).append("\n");
                if (line.contains("open")) {
                    foundOpenPorts = true;
                }
            }

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                openPorts.append("ERROR: ").append(line).append("\n");
            }

            process.waitFor();

            if (!foundOpenPorts) {
                openPorts.append("No open ports found on ").append(ipAddress).append(".");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            openPorts.append("Error executing port scan.");
        }
        return openPorts.toString();
    }

    /**
     * Este es para ejecutar un comando del sistema y capturamos su salida.
     *
     * @param command el comando a ejecutar.
     * @return una cadena con la salida del comando o un mensaje de error si ocurre
     *         una excepción.
     *
     *         Ejemplo: si el comando es "ls", devuelve la lista de archivos en el
     *         directorio.
     */
    private String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            output.append("Error executing command.");
        }
        return output.toString();
    }

    /**
     * Lista los nombres de las interfaces de red disponibles en el sistema.
     *
     * @return una cadena con los nombres de las interfaces de red, separadas por
     *         espacios.
     *         En caso de error, devuelve un mensaje con la especificación del
     *         error.
     *
     *         Ejemplo: "eth0 lo wlan0 ".
     */
    private String listInterfaces() {
        StringBuilder interfaces = new StringBuilder();
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netIf : java.util.Collections.list(nets)) {
                interfaces.append(netIf.getName()).append(" ");
            }
        } catch (SocketException e) {
            e.printStackTrace();
            interfaces.append("Error retrieving network interfaces.");
        }
        return interfaces.toString();
    }

    /**
     * Genera la serie de Fibonacci hasta un número dado.
     *
     * @param n es el límite para la serie de Fibonacci.
     * @return una cadena con la serie de Fibonacci, separada por espacios.
     *
     *         Ejemplo: si n = 10, retorna "0 1 1 2 3 5 8 ".
     */
    private String fibonacci(int n) {
        StringBuilder series = new StringBuilder();
        int a = 0, b = 1;
        for (int i = 0; a <= n; i++) {
            series.append(a).append(" ");
            int sum = a + b;
            a = b;
            b = sum;
        }
        return series.toString();
    }

    /**
     * Esto me lista los usuarios que se han conectado
     * 
     * @param current el usuaurio que está conectado
     * @return
     */
    public String listRegisteredClients(com.zeroc.Ice.Current current) {
        return String.join(", ", registeredClients);
    }

    // Método para manejar mensajes directos a clientes específicos
    private String sendDirectMessage(String fromUser, String msg) {
        String[] parts = msg.split(":", 2); // Separar el destino y el mensaje
        if (parts.length == 2) {
            String targetHost = parts[0].substring(3); // Extraer el nombre del cliente destino
            String messageToSend = parts[1].trim(); // Obtener el mensaje a enviar

            // Buscar el proxy del cliente destino en el mapa
            CallbackPrx targetProxy = clientProxies.get(targetHost);
            if (targetProxy != null) {
                // Enviar el mensaje al cliente destino usando su proxy
                try {
                    targetProxy.reportResponse("Mensaje directo de " + fromUser + ": " + messageToSend);
                    System.out.println("Mensaje directo de " + fromUser + " a " + targetHost + ": " + messageToSend);
                    return "Mensaje enviado a " + targetHost;
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Error al enviar el mensaje a " + targetHost;
                }
            } else {
                return "Error: Cliente destino no encontrado.";
            }
        } else {
            return "Error: Formato de mensaje incorrecto. Use 'to X: mensaje'.";
        }
    }

    // Método para enviar un mensaje a todos los clientes registrados (broadcast)
    public void broadcastMessage(String fromUserHost, String message, Current current) {
        for (String clientHost : registeredClients) {
            CallbackPrx clientProxy = clientProxies.get(clientHost); // Proxy del cliente basado en su host
            if (clientProxy != null) {
                clientProxy.reportResponse("Broadcast message from " + fromUserHost + " :" + message);
            }
        }
    }

    // Esto pertenecía al anterior patrón de diseño que era el Response
    @Override
    public void printString(String s, Current current) {
        System.out.println("Esto no debe aparecer, es un easter egg ;)");
    }

}
