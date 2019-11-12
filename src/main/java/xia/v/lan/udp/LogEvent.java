package xia.v.lan.udp;

import java.net.InetSocketAddress;

/**
 * @author chenhao
 * @description <p>
 * created by chenhao 2019/7/4 9:29
 */
public final class LogEvent {
    public static final byte SEPARATOR = (byte) ':';

    private final InetSocketAddress source;
    private final String logfile;
    private final String msg;
    private final long received;

    public LogEvent(String logfile, String msg) { //1
        this(null, logfile, msg,-1);
    }


    public LogEvent(InetSocketAddress source, String logfile, String msg, long received) {
        this.source = source;
        this.logfile = logfile;
        this.msg = msg;
        this.received = received;
    }



    public InetSocketAddress getSource() {
        return source;
    }

    public String getLogfile() {
        return logfile;
    }

    public String getMsg() {
        return msg;
    }

    public long getReceived() {
        return received;
    }

    public long getReceivedTimestamp() {  //6
        return received;
    }
}
