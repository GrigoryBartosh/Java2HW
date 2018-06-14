package ru.spbau.gbarto.server.architecture.blockingThreadPool;

import ru.spbau.gbarto.Serializer;
import ru.spbau.gbarto.server.architecture.AllMetrics;
import ru.spbau.gbarto.server.architecture.Metric;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class Writer implements Runnable {
    private Socket socket;
    private int[] array;
    private AllMetrics metrics;
    private Metric clientOnTheServer;
    private int x;
    private AtomicInteger stepNumber;
    private ExecutorService sender;

    Writer(Socket socket, int[] array, AllMetrics metrics, Metric clientOnTheServer, int x, AtomicInteger stepNumber, ExecutorService sender) {
        this.socket = socket;
        this.array = array;
        this.metrics = metrics;
        this.clientOnTheServer = clientOnTheServer;
        this.x = x;
        this.stepNumber = stepNumber;
        this.sender = sender;
    }

    @Override
    public void run() {
        clientOnTheServer.stop();
        metrics.clientOnTheServer.add(clientOnTheServer);

        try {
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            Serializer.writeArray(output, array);
            output.flush();

            if (stepNumber.incrementAndGet() == x) {
                metrics.request.stop();
                for (int i = 0; i < 3; i++) {
                    metrics.get(i).div(x);
                }

                socket.close();
                sender.shutdown();
            }
        } catch (IOException e) {
            System.err.println("BlockingThreadPoolArchitecture: Writer: Error working with client");
            System.exit(1);
        }
    }
}