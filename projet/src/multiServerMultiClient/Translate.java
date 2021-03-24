package multiServerMultiClient;

import java.io.IOException;
import java.net.ServerSocket;

public class Translate {
    public static String translateByteInString(byte[] msg,int length){
        StringBuilder stringBuilder = new StringBuilder();
        for (int index = 0; index < length; index++){
            stringBuilder.append((char) msg[index]);
        }
        return stringBuilder.toString();
    }

}
