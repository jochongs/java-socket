package server;

import request.RawRequest;
import request.Request;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    private ServerSocket serverSocket;
    private HashMap<HttpMethod, Router> routers;

    public void listen(int port) {
        initSocket(port);

        while(true) {
            try {
                // Waiting for connecting
                System.out.println("==================================");
                Socket socket = serverSocket.accept();

                // Input Process
                Request request = createRequest(socket);

                if (request == null) {
                    send(socket, "HTTP/1.1 200 OK");
                    continue;
                }

                System.out.println(request);

                // Output Process
                send(socket, "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "\r\n" +
                        "Hello, World!");

                // Close the socket
                socket.close();
            } catch(IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public void get(String path, RequestHandler requestHandler) {
        addRoutes(HttpMethod.GET, path, requestHandler);
    }

    public void post(String path, RequestHandler requestHandler) {
        addRoutes(HttpMethod.POST, path, requestHandler);
    }

    public void put(String path, RequestHandler requestHandler) {
        addRoutes(HttpMethod.PUT, path, requestHandler);
    }

    public void delete(String path, RequestHandler requestHandler) {
        addRoutes(HttpMethod.DELETE, path, requestHandler);
    }

    public void patch(String path, RequestHandler requestHandler) {
        addRoutes(HttpMethod.PATCH, path, requestHandler);
    }

    private void addRoutes(HttpMethod method, String path, RequestHandler requestHandler) {
        Router router;

        if (!routers.containsKey(method)) {
            router = new Router();
        } else {
            router = routers.get(method);
        }

        router.addRoute(path, requestHandler);
    }

    private void send(Socket socket, String sendStr) {
        try {
            OutputStream outputStream = socket.getOutputStream();

            byte[] bytes = sendStr.getBytes("UTF-8");

            outputStream.write(bytes);
            outputStream.flush();
        } catch(IOException exception) {
            exception.printStackTrace();
            System.out.println("Fail to send");
        }
    }

    private Request createRequest(Socket socket) {
        try {
            InputStream inputStream = socket.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);

            // Method Line
            String methodLine = bufferedReader.readLine();

            // Header Line
            String headerLines = "";
            String line = bufferedReader.readLine();
            if (line != null) {
                while(!line.isEmpty()) {
                    headerLines = headerLines + line + "\n";
                    line = bufferedReader.readLine();
                }
                headerLines = headerLines.substring(0, headerLines.length() - 1);
            }

            // Body Line
            StringBuilder bodyBuilder = new StringBuilder();
            while (bufferedReader.ready()) {
                bodyBuilder.append((char) bufferedReader.read());
            }

            return new Request(
                    new RawRequest(
                            methodLine,
                            headerLines,
                            bodyBuilder.toString()
                    )
            );
        } catch(IOException exception) {
            exception.printStackTrace();

            return null;
        }
    }

    private void initSocket(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch(IOException exception) {
            System.out.println("Fail to create socket");

            System.exit(1);
        }
    }

    private void destroySocket() {
        try {
            if (serverSocket.isBound()) {
                serverSocket.close();
            }

            serverSocket = null;
        } catch(IOException exception) {
            System.out.println("Fail to destroy socket");

            System.exit(1);
        }
    }
}


