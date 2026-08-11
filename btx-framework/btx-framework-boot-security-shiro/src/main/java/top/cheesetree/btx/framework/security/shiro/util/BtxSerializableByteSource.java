package top.cheesetree.btx.framework.security.shiro.util;

import org.apache.shiro.lang.util.ByteSource;
import org.apache.shiro.lang.util.SimpleByteSource;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author van
 * @date 2026/8/11 09:15
 * @description TODO
 */
public class BtxSerializableByteSource  implements ByteSource, Serializable {
    private static final long serialVersionUID = 1L;
    private final byte[] bytes;

    public BtxSerializableByteSource(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public boolean isEmpty() {
        return bytes == null || bytes.length == 0;
    }

    @Override
    public String toHex() {
        return new SimpleByteSource(bytes).toHex();
    }

    @Override
    public String toBase64() {
        return new SimpleByteSource(bytes).toBase64();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BtxSerializableByteSource that = (BtxSerializableByteSource) o;
        return Arrays.equals(bytes, that.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    public static BtxSerializableByteSource bytes(String str) {
        return new BtxSerializableByteSource(str.getBytes());
    }
}
