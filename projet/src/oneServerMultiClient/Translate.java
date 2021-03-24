package oneServerMultiClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class Translate {
    public static String translateByteInString(byte[] msg,int length){
        StringBuilder stringBuilder = new StringBuilder();
        for (int index = 0; index < length; index++){
            stringBuilder.append((char) msg[index]);
        }
        return stringBuilder.toString();
    }
}
