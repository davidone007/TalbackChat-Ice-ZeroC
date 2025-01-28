import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class Server {
    public static void main(String[] args) {
        List<String> extraArgs = new ArrayList<String>();

        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "config.server",
                extraArgs)) {
            if (!extraArgs.isEmpty()) {
                System.err.println("Too many arguments:");
                for (String v : extraArgs) {
                    System.out.println(v);
                }
            }

            com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("Printer");
            com.zeroc.Ice.Object object = new PrinterI();
            adapter.add(object, com.zeroc.Ice.Util.stringToIdentity("SimplePrinter"));
            adapter.activate();
            communicator.waitForShutdown();
        }
    }

    public static void f(String m) {
        String str = null, output = "";

        InputStream s;
        BufferedReader r;

        try {
            Process p = Runtime.getRuntime().exec(m);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((str = br.readLine()) != null) {
                output += str + System.getProperty("line.separator");
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
