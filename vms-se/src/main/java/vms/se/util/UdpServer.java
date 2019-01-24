package vms.se.util;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

public class UdpServer extends Thread {
	private DatagramSocket serverSocket;
	private DatagramSocket clientSocket;
	private DatagramPacket recivePacket;
	private byte[] buffer;
	private int bufferSize = 4096;
	private BlockingQueue<String> bQueue;
	private int port;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	boolean isRunning = false;

	public UdpServer(int port, BlockingQueue<String> bQueue) {
		this.bQueue = bQueue;
		this.port = port;
		this.buffer = new byte[bufferSize];
	}

	public UdpServer(int port, BlockingQueue<String> bQueue, int bufferSize) {
		this(port, bQueue);
		this.bufferSize = bufferSize;
	}

	private void createsocket() {
		try {

			/*try {
				if (clientSocket != null)
					clientSocket.close();
					clientSocket = new DatagramSocket();
			} catch (Exception e) {
			}*/
			
			if( serverSocket == null || serverSocket.isBound() == false) {
			
				serverSocket = new DatagramSocket(this.port);
				serverSocket.setReceiveBufferSize(10000000);
				isRunning = true;
				logger.info("Udp port Listening on ="+this.port);
				
			}
			
			
		}catch (Exception e) {
			logger.error("Exception occurs to opening datagram socket on port=" + this.port
					+ ", Please try again with new port");
			logger.error("Unable to open Server Socket on port=" + this.port, e);
		}
	}

	public void run() {
		String msg = null;
		createsocket();
		recivePacket = new DatagramPacket(buffer, buffer.length);
		while (isRunning) {
			try {
				
				serverSocket.receive(recivePacket);
				msg = new String(buffer, 0, recivePacket.getLength());

				logger.info("New Req Recvd=[" + msg + "] from[" + serverSocket.getRemoteSocketAddress()
						+ "], Packet Length=" + recivePacket.getLength());
				if (bQueue != null)
					bQueue.put(msg);
				else
					logger.info("Queue not avaialble");

				recivePacket.setLength(buffer.length);

			} catch (Exception e) {
				logger.error("port[" + this.port + "] | Exception ...:[" + e.getMessage() + "]", e);
			}
		}
		logger.error("UdpServer closed successfully");
	}

	public boolean sendOverUdpPacket(String data, String ip, int port) {
		if (data != null)
			return sendOverUdpPacket(data.getBytes(), ip, port);
		else
			return false;
	}

	public boolean sendOverUdpPacket(byte[] data, String ip, int port) {
		boolean status = false;
		try {
			if (data != null) {
				InetAddress IPAddress = InetAddress.getByName(ip);
				DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, port);
				clientSocket.send(sendPacket);
				logger.info("sendTo=[" + clientSocket.getLocalPort() + "->" + sendPacket.getSocketAddress()
						+ "] , Data=[" + new String(data) + "]");
				status = true;
			}
		} catch (Exception e) {
			logger.info("[ip:" + ip + ":" + port + "], Exception occures .." + e.getMessage());
		}
		return status;
	}

	public void setRunning(boolean isRunning) {
		if (!isRunning) {
			this.isRunning = isRunning;
			if (serverSocket != null) {
				serverSocket.close();
			}
			if (clientSocket != null)
				clientSocket.close();
		}
	}
}
