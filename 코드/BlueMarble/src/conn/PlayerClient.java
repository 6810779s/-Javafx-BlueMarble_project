package conn;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;



public class PlayerClient{
    private String ip;   
    private Socket socket;
    private BufferedReader inMsg = null;
    private PrintWriter outMsg = null;
    private String msg =null;    
    public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}      
    public BufferedReader getInMsg() {
		return inMsg;
	}

	public void setInMsg(BufferedReader inMsg) {
		this.inMsg = inMsg;
	}

	public PrintWriter getOutMsg() {
		return outMsg;
	}

	public void setOutMsg(PrintWriter outMsg) {
		this.outMsg = outMsg;
	}

	public PlayerClient(String ip) {
        this.ip = ip;
        connectServer();
    }
    
    public void connectServer() {
        try {
            socket = new Socket(ip, 8888);
            System.out.println("[Client]Server 연결 성공!!");
            inMsg = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outMsg = new PrintWriter(socket.getOutputStream(), true);
        } catch(Exception e) {
            System.out.println("[Client]connectServer() Exception 발생!!");
        }
    }
       
}