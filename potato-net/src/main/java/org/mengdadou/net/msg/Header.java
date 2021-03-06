package org.mengdadou.net.msg;

/**
 * Created by mengdadou on 16-11-4.
 */
public class Header {
    private byte version = 0x01;
    private int length;
    // see {@link MsgType}
    private byte type;
    
    public Header() {
    }
    
    public Header(byte type) {
        this.type = type;
    }
    
    public byte getVersion() {
        return version;
    }
    
    public int getLength() {
        return length;
    }
    
    public void setLength(int length) {
        this.length = length;
    }
    
    
    public byte getType() {
        return type;
    }
    
    public void setType(byte type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return "Header{" +
                "version=" + version +
                ", length=" + length +
                ", type=" + type +
                '}';
    }
}
