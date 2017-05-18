package io.openmessaging.demo.YmSerial;

import io.openmessaging.Message;
import io.openmessaging.demo.DefaultBytesMessage;

/**
 * Created by YangMing on 2017/5/8.
 */
public class YmMessageMetaDataParser {
    private DefaultBytesMessage msg;
    private byte[] metaData;
    private int current_offset;
    private int total_length;

    public YmMessageMetaDataParser() {
        this.current_offset = 0;
    }

    public void setMetaData(byte[] metaData) {
        this.metaData = metaData;
        msg = new DefaultBytesMessage(null);
    }


    public void readMessage() throws Exception {
        readMessageHead();
        readBody();
        while (current_offset < total_length) {
            readKeyValue();
        }
    }

    private void readMessageHead() throws Exception {
        int signature = readSignature();
        if (signature == SerialConfig.SIGNATURE_MESSAGE) {
            total_length = readLength();
        } else throw new Exception("bad message first signature");
    }

    private void readBody() throws Exception {
        int signature = readSignature();
        if (signature == SerialConfig.SIGNATURE_BODY) {
            int bodyLength = readLength();
            System.out.println("body length " + bodyLength);
            msg.setBody(getStringBytes(bodyLength));
        } else throw new Exception("bad body signature, offset " + current_offset);
    }

    private void readKeyValue() throws Exception {
        int keyValueHead = readSignature();
        if (keyValueHead == SerialConfig.SIGNATURE_HEADER || keyValueHead == SerialConfig.SIGNATURE_PROPERTY) {
            // pass the key signature
            readSignature();
            String key = readString();
            int signature = readSignature();
            switch (signature) {
                case SerialConfig.SIGNATURE_STRING:
                    if (keyValueHead == SerialConfig.SIGNATURE_HEADER) msg.putHeaders(key, readString());
                    else if (keyValueHead == SerialConfig.SIGNATURE_PROPERTY) msg.putProperties(key, readString());
                    else throw new Exception("bad key value signature, offset " + current_offset);
                    break;
                case SerialConfig.SIGNATURE_INT:
                    if (keyValueHead == SerialConfig.SIGNATURE_HEADER) msg.putHeaders(key, readInt());
                    else if (keyValueHead == SerialConfig.SIGNATURE_PROPERTY) msg.putProperties(key, readInt());
                    else throw new Exception("bad key value signature, offset " + current_offset);
                    break;
                case SerialConfig.SIGNATURE_LONG:
                    if (keyValueHead == SerialConfig.SIGNATURE_HEADER) msg.putHeaders(key, readLong());
                    else if (keyValueHead == SerialConfig.SIGNATURE_PROPERTY) msg.putProperties(key, readLong());
                    else throw new Exception("bad key value signature, offset " + current_offset);
                    break;
                case SerialConfig.SIGNATURE_DOUBLE:
                    if (keyValueHead == SerialConfig.SIGNATURE_HEADER) msg.putHeaders(key, readDouble());
                    else if (keyValueHead == SerialConfig.SIGNATURE_PROPERTY) msg.putProperties(key, readDouble());
                    else throw new Exception("bad key value signature, offset " + current_offset);
                    break;
                default:
                    throw new Exception("bad key value block, offset " + current_offset);

            }
        } else throw new Exception("bad key value signature, current offset " + current_offset);
    }


    public int readSignature() {
        return metaData[current_offset++];
    }

    private int readLength() {
        int r = ((metaData[current_offset] << 24 & 0xFF000000) |
                (metaData[current_offset + 1] << 16 & 0x00FF0000) |
                (metaData[current_offset + 2] << 8 & 0x0000FF00) |
                (metaData[current_offset + 3] & 0x000000FF));
        System.out.println("length: " + r);
        current_offset += 4;
        return r;
    }

    private int readInt() {
        return readLength();
    }

    private long readLong() {
        // to do
        return 1;
    }

    private double readDouble() {
        // to do ...
        return 1.0;
    }

    private byte[] getStringBytes(int stringLength) {
        byte[] r = new byte[stringLength];
        for (int i = current_offset; i < current_offset + stringLength; i++) {
            r[i - current_offset] = metaData[i];
        }
        current_offset += stringLength;
        return r;
    }

    private String readStringWithHead() throws Exception {
        if (readSignature() == SerialConfig.SIGNATURE_STRING) {
            int length = readLength();
            return new String(getStringBytes(length));
        } else {
            throw new Exception("bad string signature, current offset " + current_offset);

        }
    }

    private int readIntWithHead() throws Exception {
        if (readSignature() == SerialConfig.SIGNATURE_INT) {
            return readInt();
        } else {
            throw new Exception("bad int signature, current offset " + current_offset);
        }
    }

    private long readLongWithHead() throws Exception {
        // to do ...
        return 1;
    }

    private double readDoubleWithHead() throws Exception {
        // to do ...
        return 1.0;
    }

    private String readString() {
        return new String(getStringBytes(readLength()));
    }

    public Message getMsg() {
        return this.msg;
    }

}
