package frontEnd;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

import replicaManagement.Request;

public class FIFOQueue implements Runnable {
	private static FIFOQueue queue;
	Queue<Request> myQ = new LinkedList<Request>();
	Request sentRequest;

	private FIFOQueue() {
	}

	public static FIFOQueue getInstance() {
		if (queue == null)
			queue = new FIFOQueue();
		return queue;

	}

	public void push(Request R) {
		myQ.add(R);

	}

	public void pull() {

	}

	public void run() {
		String leaderInfo = "", result = "";
		DatagramSocket socket = null;
		while (myQ.size() != 0) {
			try {
				synchronized (sentRequest) {
					do {
						leaderInfo = FrontEnd.leaderInfo;
					} while (null == leaderInfo
							&& "".equalsIgnoreCase(leaderInfo));

					String[] leader = leaderInfo.split(":");

					// serialize the message object
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ObjectOutput oo = new ObjectOutputStream(bos);
					oo.writeObject(sentRequest);
					oo.close();
					socket = new DatagramSocket();
					InetAddress host = InetAddress.getByName(leader[0]);
					byte[] serializedMsg = bos.toByteArray();
					DatagramPacket request = new DatagramPacket(serializedMsg,
							serializedMsg.length, host,
							Integer.parseInt(leader[1]));
					socket.send(request);

					// send reply back to client
					byte[] buffer = new byte[1000];
					DatagramPacket reply = new DatagramPacket(buffer,
							buffer.length);
					socket.receive(reply);
					result = new String(reply.getData());
					
					//TO-DO: SEND RESULT BACK TO CLIENT IF IT IS VALID
				}
			} catch (SocketException s) {
				System.out.println("Socket: " + s.getMessage());
			} catch (Exception e) {
				System.out.println("IO: " + e.getMessage());
			} finally {
				if (socket != null) {
					socket.close();
				}
			}
		}
	}

}
