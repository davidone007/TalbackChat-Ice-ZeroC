import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;

import Demo.Callback;
import Demo.CallbackPrx;
import Demo.PrinterPrx;

public class Client {
  public static void main(String[] args) {
    java.util.List<String> extraArgs = new java.util.ArrayList<>();

    try (Communicator communicator = Util.initialize(args, "config.client", extraArgs)) {
      PrinterPrx service = PrinterPrx.checkedCast(
          communicator.propertyToProxy("Printer.Proxy"));

      if (service == null) {
        throw new Error("Invalid proxy");
      }

      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      String username = System.getProperty("user.name");
      String hostname = InetAddress.getLocalHost().getHostName();
      String userHost = username + "@" + hostname;

      try {
        ObjectAdapter adapter = communicator.createObjectAdapter("Callback");
        Callback callback = new CallbackI();

        ObjectPrx prx = adapter.add(callback, Util.stringToIdentity("callback"));
        CallbackPrx callbackPrx = CallbackPrx.checkedCast(prx); // Aquí se realiza la conversión segura
        adapter.activate();

        System.out.println("Waiting for response...");

        String message;
        long totalStartTime = System.nanoTime();
        long totalMessages = 0;
        long totalResponseTime = 0;

        do {
          System.out.print("Type your message right here ('exit' to quit): ");
          message = reader.readLine();

          if (!message.equalsIgnoreCase("exit")) {
            String fullMessage = userHost + ":" + message;

            long requestStartTime = System.nanoTime();
            service.message(callbackPrx, fullMessage);
            long requestEndTime = System.nanoTime();

            long responseTime = requestEndTime - requestStartTime;
            totalResponseTime += responseTime;

            totalMessages++;
          }
        } while (!message.equalsIgnoreCase("exit"));

        long totalEndTime = System.nanoTime();
        long totalTime = totalEndTime - totalStartTime;
        System.out.println("Total time spent: " + totalTime / 1_000_000.0 + " ms");
        System.out.println("Total messages sent: " + totalMessages);
        System.out.println("Average response time: " + (totalResponseTime / totalMessages) / 1_000_000.0 + " ms");
        System.out.println("Throughput: " + (totalMessages / (totalTime / 1_000_000_000.0)) + " messages/second");
        communicator.shutdown();

      } catch (Exception e) {
        e.printStackTrace();
      }

      communicator.waitForShutdown();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
