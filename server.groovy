import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.input.ReversedLinesFileReader;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;
import org.eclipse.jetty.websocket.WebSocket.Connection;

public class WebSocketServerJetty {

	private static class MyWebSocketHandler extends WebSocketHandler {
		Connection connection;

		@Override
		public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
			return new WebSocket.OnTextMessage() {
				@Override
				public void onOpen(Connection conn) {
					connection = conn;
				}

				@Override
				public void onClose(int closeCode, String message) {
					connection = null;
				}

				@Override
				public void onMessage(String data) {
				
					File file = new File("/var/log/system.log");
					int n_lines = 100000;
					int counter = 0; 
					ReversedLinesFileReader object = new ReversedLinesFileReader(file);
					while(!object.readLine().isEmpty()  && counter < n_lines) {
						try {
							Thread.sleep(1000);
						} catch(Exception e){ 
							throw new RuntimeException(e);
						}
						//System.out.println(object.readLine());
						counter++;
						System.out.println("onMessage() - data: " + counter);
						try {
							connection.sendMessage(object.readLine());
						} catch (IOException x) {
							connection.close();
						}
					}

				}
			};
		}

	}

	public static void main(String[] args) {
		try {
			Server server = new Server(8081);
			WebSocketHandler chatWebSocketHandler = new MyWebSocketHandler();
			chatWebSocketHandler.setHandler(new DefaultHandler());
			server.setHandler(chatWebSocketHandler);
			server.start();
			server.join();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
