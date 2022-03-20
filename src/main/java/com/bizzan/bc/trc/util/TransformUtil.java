package com.bizzan.bc.trc.util;


import lombok.extern.slf4j.Slf4j;
import org.jline.utils.Log;
import org.tron.common.utils.ByteArray;
import org.tron.trident.core.ApiWrapper;
import org.tron.walletserver.WalletApi;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.utils.Numeric;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TransformUtil {


    /**
     * 长度不够前面补0
     *
     * @param str
     * @param strLength
     * @return
     */
    public static String addZeroForNum(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                sb.append("0").append(str);// 左补0
                // sb.append(str).append("0");//右补0
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }

    public static String delZeroForNum(String str) {
        return str.replaceAll("^(0+)", "");
    }

    public static String getSeqNumByLong(Long l, int bitCount) {
        return String.format("%0" + bitCount + "d", l);
    }


    /**
     * 字符串转换为16进制字符串
     *
     * @param s
     * @return
     */
    public static String stringToHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

    /**
     * 16进制字符串转换为字符串
     *
     * @param s
     * @return
     */
    public static String hexStringToString(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        s = s.replace(" ", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(
                        s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "gbk");
            new String();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    /**
     * 16进制表示的字符串转换为字节数组
     *
     * @param s 16进制表示的字符串
     * @return byte[] 字节数组
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] b = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            b[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return b;
    }

    /**
     * byte数组转16进制字符串
     *
     * @param bArray
     * @return
     */
    public static final String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }


    /**
     * @param: [hex]
     * @return: int
     * @description: 按位计算，位值乘权重
     */
    public static int hexToDecimal(String hex) {
        int outcome = 0;
        for (int i = 0; i < hex.length(); i++) {
            char hexChar = hex.charAt(i);
            outcome = outcome * 16 + charToDecimal(hexChar);
        }
        return outcome;
    }

    /**
     * @param: [c]
     * @return: int
     * @description:将字符转化为数字
     */
    public static int charToDecimal(char c) {
        if (c >= 'A' && c <= 'F')
            return 10 + c - 'A';
        else
            return c - '0';
    }


    /**
     * 把原始字符串分割成指定长度的字符串列表
     *
     * @param inputString 原始字符串
     * @param length      指定长度
     * @return
     */
    public static List<String> getStrList(String inputString, int length) {
        int size = inputString.length() / length;
        if (inputString.length() % length != 0) {
            size += 1;
        }
        return getStrList(inputString, length, size);
    }

    /**
     * 把原始字符串分割成指定长度的字符串列表
     *
     * @param inputString 原始字符串
     * @param length      指定长度
     * @param size        指定列表大小
     * @return
     */
    public static List<String> getStrList(String inputString, int length,
                                          int size) {
        List<String> list = new ArrayList<String>();
        for (int index = 0; index < size; index++) {
            String childStr = substring(inputString, index * length,
                    (index + 1) * length);
            list.add(childStr);
        }
        return list;
    }

    /**
     * 分割字符串，如果开始位置大于字符串长度，返回空
     *
     * @param str 原始字符串
     * @param f   开始位置
     * @param t   结束位置
     * @return
     */
    public static String substring(String str, int f, int t) {
        if (f > str.length())
            return null;
        if (t > str.length()) {
            return str.substring(f, str.length());
        } else {
            return str.substring(f, t);
        }
    }

    static <T extends NumericType> T decodeNumeric(String input, Class<T> type) {
        try {
            byte[] inputByteArray = Numeric.hexStringToByteArray(input);
            int typeLengthAsBytes = getTypeLengthInBytes(type);

            byte[] resultByteArray = new byte[typeLengthAsBytes + 1];

            if (Int.class.isAssignableFrom(type) || Fixed.class.isAssignableFrom(type)) {
                resultByteArray[0] = inputByteArray[0];  // take MSB as sign bit
            }

            int valueOffset = Type.MAX_BYTE_LENGTH - typeLengthAsBytes;
            System.arraycopy(inputByteArray, valueOffset, resultByteArray, 1, typeLengthAsBytes);

            BigInteger numericValue = new BigInteger(resultByteArray);
            return type.getConstructor(BigInteger.class).newInstance(numericValue);

        } catch (NoSuchMethodException | SecurityException
                | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            throw new UnsupportedOperationException(
                    "Unable to create instance of " + type.getName(), e);
        }
    }

    static <T extends NumericType> int getTypeLengthInBytes(Class<T> type) {
        return getTypeLength(type) >> 3;  // divide by 8
    }

    static <T extends NumericType> int getTypeLength(Class<T> type) {
        if (IntType.class.isAssignableFrom(type)) {
            String regex = "(" + Uint.class.getSimpleName() + "|" + Int.class.getSimpleName() + ")";
            String[] splitName = type.getSimpleName().split(regex);
            if (splitName.length == 2) {
                return Integer.parseInt(splitName[1]);
            }
        } else if (FixedPointType.class.isAssignableFrom(type)) {
            String regex = "(" + Ufixed.class.getSimpleName() + "|"
                    + Fixed.class.getSimpleName() + ")";
            String[] splitName = type.getSimpleName().split(regex);
            if (splitName.length == 2) {
                String[] bitsCounts = splitName[1].split("x");
                return Integer.parseInt(bitsCounts[0]) + Integer.parseInt(bitsCounts[1]);
            }
        }
        return Type.MAX_BIT_LENGTH;
    }

    //获取收款地址
    public static String decodeAddress(String input) {
        String address = null;
        try {
            address = WalletApi.encode58Check(ByteArray
                    .fromHexString(input.substring(128, 168)));
        } catch (Exception e) {
            Log.error("解析收款地址异常,cause={}", e.getMessage());
        }
        return address;
    }

    //获取发送金额
    public static BigDecimal transitionAmount(String data) {
        BigDecimal amount = BigDecimal.ZERO;
        try {
            Uint256 rawAmount = decodeNumeric(data.substring(168, 232), Uint256.class);
            amount = new BigDecimal(rawAmount.getValue()).divide(new BigDecimal("1000000"));
        } catch (Exception e) {
            Log.error("解析发送金额异常,cause={}", e.getMessage());
        }
        return amount;
    }

    //获取发送金额
    public static BigDecimal getTransitionAmount(String data) {
        BigDecimal amount = BigDecimal.ZERO;
        try {
            Uint256 rawAmount = decodeNumeric(data, Uint256.class);
              amount = new BigDecimal(rawAmount.getValue()).divide(new BigDecimal("1000000"));
        } catch (Exception e) {
            Log.error("解析发送金额异常,cause={}", e.getMessage());
        }
        return amount;
    }

    public static void main(String[] args) {

        ApiWrapper wrapper = ApiWrapper.ofMainnet("5c51f727cfa124d32a4bb96ef85cc4fc9a9d69f98b6ff85e97f82f955647286b", "22e0780c-55fc-4dcf-919d-f74d6edbaee0");
//        ApiWrapper.parseHex("0000000000000000000000000d0707963952f2fba59dd06f2b425ace40b492fe").toString();
        System.err.println(TypeDecoder.decodeAddress("0000000000000000000000000000000000000000000000000000000003567e00"));
        System.err.println(TypeDecoder.decodeAddress("0000000000000000000000000d0707963952f2fba59dd06f2b425ace40b492fe"));
        System.err.println(TypeDecoder.decodeAddress("000000000000000000000000a3668dc100673b38ec5a3d11186c6be12cfb801f"));
        Uint256 rawAmount = decodeNumeric("0000000000000000000000000000000000000000000000000000000003567e00", Uint256.class);
        BigDecimal  amount = new BigDecimal(rawAmount.getValue()).divide(new BigDecimal("1000000"));
        System.err.println(amount);

    }

}

