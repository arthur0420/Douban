package arthur.mq.message;

public class CommandDefine {
	public final static byte CLOSESERVER = -1;
	public final static byte GETMESSAGE = 1;
	public final static byte SETMESSAGE = 2;
	public final static byte HEARTBEAT = 3;
}
