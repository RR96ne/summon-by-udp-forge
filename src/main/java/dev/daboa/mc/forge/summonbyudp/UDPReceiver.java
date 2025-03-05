package dev.daboa.mc.forge.summonbyudp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPReceiver implements Runnable {
    private final int port;
    private DatagramSocket socket = null;
    private volatile boolean running = true;
    private volatile boolean spawn = false;
    public UDPReceiver(int port) {
        this.port = port;
    }

    @Override
    public void run() {

        try {
            socket = new DatagramSocket(port);
            byte[] buffer = new byte[1024];
            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                } catch (Exception e) {
                    if (!running) {
                        //System.out.println("Receiver thread interrupted.");
                    } else {
                        e.printStackTrace();
                    }
                    break; // `running`がfalseである場合、ループを抜ける
                }
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received UDP message: " + message);
                // ゾンビのスポーン条件を成立させる
                if(message.equals("zombie")) spawn = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getSpawn(){
        if(spawn){
            spawn = false;
            return true;
        }else {
            return false;
        }
    }

    public void stop() {
        running = false;
        if(socket!=null) {
            socket.close();
        }
    }
}
