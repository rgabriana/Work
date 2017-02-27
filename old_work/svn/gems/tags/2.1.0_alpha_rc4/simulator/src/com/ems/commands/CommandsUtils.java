/**
 * 
 */
package com.ems.commands;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import com.ems.utils.Utils;

/**
 * @author Sameer Surjikar
 * 
 */
public final class CommandsUtils {
    /**
     * 
     * @deprecated Good for generic code, but not when performance is the key. Use hardcoded packet size since they are
     *             know
     * @param classQualifiedName
     * @return
     */
    public static long getTotalLengthOfClassVariables(final String classQualifiedName) {
        Class c;
        long length = 0;

        try {
            c = Class.forName(classQualifiedName);

            Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                Type t = field.getGenericType();
                if (t.equals(char.class)) {
                    length += 1;

                } else if (t.equals(short.class)) {
                    length += 2;

                }

                else if (t.equals(int.class)) {
                    length += 4;
                }

                else {
                    System.out.println("The instance the field " + t.getClass()
                            + " is not regisered with CommondsUtils.getTotalLengthOfClassVariables. please do so");

                }
            }

        } catch (ClassNotFoundException e) {
            // TODO YAuto-generated catch block
            e.printStackTrace();
        }
        return length;
    }

    public static byte[] preparePacket(byte[] sendData) {
        // adding the gateway header
        byte[] gwHeader = new byte[9];
        int i = 0;
        gwHeader[i++] = 'e'; // magic
        gwHeader[i++] = 's'; // magic
        byte[] lenByteArr = Utils.shortToByteArray(sendData.length + 9);
        System.arraycopy(lenByteArr, 0, gwHeader, i++, lenByteArr.length);
        i++;
        gwHeader[i++] = (byte) 2; // flags 11000
        int hashKey = 0;
        byte[] hashKeyByteArr = Utils.intToByteArray(hashKey);
        System.arraycopy(hashKeyByteArr, 0, gwHeader, i++, hashKeyByteArr.length);
        i += 3;
        byte[] gwPacket = new byte[gwHeader.length + sendData.length];
        System.arraycopy(gwHeader, 0, gwPacket, 0, gwHeader.length);
        System.arraycopy(sendData, 0, gwPacket, gwHeader.length, sendData.length);
        return gwPacket;
    }

}
