package gl.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.math.BigInteger;
import java.net.URLConnection;
import java.net.SocketTimeoutException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.DatagramSocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Utility
{
    static volatile int seed;
    private Logger logger;
    private static final char[] hexCode;
    
    public Utility() {
        this.logger = LogManager.getRootLogger();
    }
    
    public boolean sendOverUdpPefixLength(String data, final String ip, final int port, int lengthDigits) {
        if (lengthDigits > 0) {
            if (lengthDigits < ("" + data.length()).length()) {
                lengthDigits = ("" + data.length()).length();
            }
            String base = null;
            for (int i = 0; i < lengthDigits; ++i) {
                base += "0";
            }
            data = base.substring(("" + data.length()).length()) + data.length() + data;
        }
        return this.sendOverUdp(data, ip, port);
    }
    
    public boolean sendOverUdp(final String data, final String ip, final int port) {
        boolean status = true;
        try {
            final DatagramSocket clientSocket = new DatagramSocket();
            final InetAddress IPAddress = InetAddress.getByName(ip);
            final DatagramPacket sendPacket = new DatagramPacket(data.getBytes(), data.length(), IPAddress, port);
            clientSocket.send(sendPacket);
            clientSocket.close();
            this.logger.debug(data + " " + ip + ":" + port);
        }
        catch (Exception e) {
            status = false;
        }
        return status;
    }
    
    public void sendOverUdpPacket(final String data, final String ip, final int port) {
        try {
            final DatagramSocket clientSocket = new DatagramSocket();
            final InetAddress IPAddress = InetAddress.getByName(ip);
            final DatagramPacket sendPacket = new DatagramPacket(data.getBytes(), data.length(), IPAddress, port);
            clientSocket.send(sendPacket);
            clientSocket.close();
        }
        catch (Exception e) {
            this.logger.error("Exception ip:" + ip + ":" + port);
            e.printStackTrace();
        }
    }
    
    public void sendBufferOverUdp(final byte[] data, final String ip, final int port) {
        try {
            final DatagramSocket clientSocket = new DatagramSocket();
            final InetAddress IPAddress = InetAddress.getByName(ip);
            final DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, port);
            clientSocket.send(sendPacket);
            clientSocket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public boolean isNumeric(final String number) {
        if (number == null || number.length() == 0) {
            return false;
        }
        for (int i = 0; i < number.length(); ++i) {
            if (number.charAt(i) < '0' || number.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }
    
    public String addMinuteInDate(final int min) {
        return this.getCalculatedDate(12, min, "yyyy-MM-dd HH:mm:ss");
    }
    
    public String addHourInDate(final int hour) {
        return this.getCalculatedDate(10, hour, "yyyy-MM-dd HH:mm:ss");
    }
    
    public String addDayInDate(final String date, final int days) {
        String convertedDate = null;
        try {
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            final Calendar cal = Calendar.getInstance();
            cal.setTime(dateFormat.parse(date));
            cal.add(5, days);
            convertedDate = dateFormat.format(cal.getTime());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return convertedDate;
    }
    
    public Date addDayInDate(final Date date, final int days) {
        Calendar cal = null;
        try {
            cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(5, days);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return cal.getTime();
    }
    
    public java.sql.Date getConvertedDateTime(final String date, final String format) {
        try {
            final SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            final Date d = dateFormat.parse(date);
            final java.sql.Date sqlStartDate = new java.sql.Date(d.getTime());
            return sqlStartDate;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String covertSqlDateToString(final java.sql.Date date, final String format) {
        final Date newDate = new Date(date.getTime());
        return this.getDateTime(newDate, format);
    }
    
    public String getCalculatedDate(final int calenderField, final int changeValue, final String format) {
        final SimpleDateFormat sdf = new SimpleDateFormat(format);
        final Calendar c1 = Calendar.getInstance();
        c1.add(calenderField, changeValue);
        return sdf.format(c1.getTime());
    }
    
    public String getDateTime() {
        return this.getDateTime("yyyy-MM-dd HH:mm:ss");
    }
    
    public Date getDate(final String format) {
        final SimpleDateFormat sdf = new SimpleDateFormat(format);
        final Calendar c1 = Calendar.getInstance();
        return c1.getTime();
    }
    
    public Date addMinuteInDate(final Date date, final int min) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(12, min);
        return cal.getTime();
    }
    
    public String getDateTime(final String format) {
        final SimpleDateFormat sdf = new SimpleDateFormat(format);
        final Calendar c1 = Calendar.getInstance();
        return sdf.format(c1.getTime());
    }
    
    public String getDateTime(final Date date, final String format) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        final SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(cal.getTime());
    }
    
    public String getPrintStackTrace(final Exception exception) {
        final StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        exception.printStackTrace();
        return sw.toString();
    }
    
    public static String getStackTrace(final Exception exception) {
        final StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        exception.printStackTrace();
        return sw.toString();
    }
    
    public int dateDiff(final Date date1, final Date date2) {
        final long diff = date2.getTime() - date1.getTime();
        return (int)(diff / 86400000L);
    }
    
    public int compareDate(final String paramdate1, final String paramdate2) {
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            final Date date1 = sdf.parse(paramdate1);
            final Date date2 = sdf.parse(paramdate2);
            return this.compareDate(date1, date2);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return -2;
        }
    }
    
    public int compareDate(final Date date1, final Date date2) {
        try {
            if (date1.after(date2)) {
                return 1;
            }
            if (date1.before(date2)) {
                return -1;
            }
            if (date1.equals(date2)) {
                return 0;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return -2;
    }
    
    public String getPrintStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        throwable.printStackTrace();
        return sw.toString();
    }
    
    public String getDate(final Date date, final String format) {
        final SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(date);
    }
    
    public int callCPUrl(final String urlString, final int connectTimeout, final int readTimeout, final StringBuilder freeFlow, final StringBuilder response) {
        String result = "";
        HttpURLConnection urlConnection = null;
        BufferedReader in = null;
        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setConnectTimeout(connectTimeout * 1000);
            urlConnection.setReadTimeout(readTimeout * 1000);
            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String freeflow = urlConnection.getHeaderField("FREEFLOW");
            if (freeflow == null) {
                freeflow = "FB";
            }
            System.out.println("freeflow ==" + freeflow);
            freeFlow.append(freeflow);
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("test" + inputLine);
                result += inputLine;
                System.out.println("result==" + result);
            }
            if (result == null || result.equals("")) {
                result = "Cp no resp";
            }
            response.append(result);
            System.out.println("result freeflow =" + result);
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            result = "MalformedURL Exception";
        }
        catch (ConnectException e2) {
            e2.printStackTrace();
            result = "Not able to Connect";
        }
        catch (SocketTimeoutException e3) {
            e3.printStackTrace();
            result = "Read TimeOut";
        }
        catch (Exception e4) {
            e4.printStackTrace();
            result = "Generic Exception";
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                }
                catch (Exception e5) {
                    e5.printStackTrace();
                }
            }
        }
        return 0;
    }
    
    public static String CallReURL(final String urlString, final int connectTimeout, final int readTimeout) {
        String inputResponse = "";
        try {
            final URL url = new URL(urlString);
            final URLConnection urlConn = url.openConnection();
            urlConn.setConnectTimeout(connectTimeout * 1000);
            urlConn.setReadTimeout(readTimeout * 1000);
            final BufferedReader inputReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            String inputLine;
            while ((inputLine = inputReader.readLine()) != null) {
                inputResponse += inputLine;
            }
            inputReader.close();
        }
        catch (Exception ex) {}
        return inputResponse;
    }
    
    public String CallURL(final String urlString) {
        String inputResponse = "";
        try {
            final URL url = new URL(urlString);
            final URLConnection urlConn = url.openConnection();
            urlConn.setConnectTimeout(30000);
            urlConn.setReadTimeout(30000);
            final BufferedReader inputReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            String inputLine;
            while ((inputLine = inputReader.readLine()) != null) {
                inputResponse += inputLine;
            }
            inputReader.close();
        }
        catch (Exception exp) {
            inputResponse = exp.getMessage();
        }
        return inputResponse;
    }
    
    public static String CallURL(final String urlString, final int i) {
        String inputResponse = "";
        System.out.println("xmlData  urlString =" + urlString);
        try {
            final URL url = new URL(urlString);
            final URLConnection urlConn = url.openConnection();
            urlConn.setConnectTimeout(30000);
            urlConn.setReadTimeout(30000);
            final BufferedReader inputReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            String inputLine;
            while ((inputLine = inputReader.readLine()) != null) {
                inputResponse += inputLine;
            }
            System.out.println("xmlData = " + inputResponse);
            inputReader.close();
        }
        catch (Exception exp) {
            inputResponse = exp.getMessage();
        }
        return inputResponse;
    }
    
    public String callUrl(final String urlString, final int connectTimeout, final int readTimeout) {
        String result = "";
        HttpURLConnection urlConnection = null;
        BufferedReader in = null;
        try {
            final URL url = new URL(urlString);
            this.logger.error(urlString);
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setConnectTimeout(connectTimeout * 1000);
            urlConnection.setReadTimeout(readTimeout * 1000);
            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                result += inputLine;
            }
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            result = "MalformedURL Exception";
        }
        catch (ConnectException e2) {
            e2.printStackTrace();
            result = "Not able to Connect";
        }
        catch (SocketTimeoutException e3) {
            e3.printStackTrace();
            result = "Read TimeOut";
        }
        catch (Exception e4) {
            e4.printStackTrace();
            result = "Generic Exception";
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                }
                catch (Exception e5) {
                    e5.printStackTrace();
                }
            }
        }
        return result;
    }
    
    public static String bytesToHex(final byte[] data, final String delimiter) {
        final StringBuilder r = new StringBuilder(data.length * 2);
        for (final byte b : data) {
            r.append(Utility.hexCode[b >> 4 & 0xF]);
            r.append(Utility.hexCode[b & 0xF]);
            r.append(delimiter);
        }
        return r.toString();
    }
    
    public static String bytesToHex(final byte[] data) {
        return bytesToHex(data, " ");
    }
    
    public int appendArray(final byte[] des, final byte[] src, int index) {
        for (int len = src.length, l = 0; l < len; ++l) {
            des[index] = src[l];
            ++index;
        }
        return index;
    }
    
    public byte[] appendArray(final byte[] source1, final byte[] source2) {
        final int len = source1.length + source2.length;
        final byte[] dest = new byte[len];
        int i;
        for (i = 0, i = 0; i < source1.length; ++i) {
            dest[i] = source1[i];
        }
        for (int j = 0; j < source2.length; ++j) {
            dest[i++] = source2[j];
        }
        return dest;
    }
    
    public byte[] getCodedString(final String data) {
        try {
            return this.utf8toutf16(data);
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    public byte[] getCodedString(final byte[] t) {
        try {
            final String temp = this.byteArrayToHex(t);
            return this.utf8toutf16(temp);
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    public String dec2hex4(final char textString) {
        final char[] hexequiv = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        return "" + hexequiv[textString >> 12 & 0xF] + hexequiv[textString >> 8 & 0xF] + hexequiv[textString >> 4 & 0xF] + hexequiv[textString & '\u000f'];
    }
    
    public String putSpaces(final String data, final int digit, final String spacer) {
        if (data == null) {
            return data;
        }
        final StringBuilder str = new StringBuilder();
        boolean spaceOn = false;
        int counter = 1;
        for (int i = 0; i < data.length(); ++i) {
            str.append(data.charAt(i));
            if (spaceOn) {
                str.append(spacer);
                spaceOn = false;
            }
            if (++counter == digit) {
                spaceOn = true;
                counter = 0;
            }
        }
        return str.toString();
    }
    
    public byte[] parseHexBinary(final String str) throws Exception {
        byte[] a = new BigInteger(str, 16).toByteArray();
        if (a.length != str.length() / 2) {
            a = Arrays.copyOfRange(a, 1, a.length);
        }
        return a;
    }
    
    public byte getHexCodePointToByte(final char a, final char b) {
        final int c = this.hex2int(a) * 16 + this.hex2int(b);
        return (byte)c;
    }
    
    public int getNumberOfTrailingByte(final byte b) {
        final int result = 0;
        if ((b & 0x80) == 0x0) {
            return 0;
        }
        if ((b & 0xE0) == 0xC0) {
            return 1;
        }
        if ((b & 0xF0) == 0xE0) {
            return 2;
        }
        if ((b & 0xF8) == 0xF0) {
            return 3;
        }
        if ((b & 0xFC) == 0xF8) {
            return 4;
        }
        if ((b & 0xFE) == 0xFC) {
            return 5;
        }
        return result;
    }
    
    public int getUtf8byteToUnicode(final byte[] b, final int i, final int trailbytecount) {
        final int counter = 0;
        int result = 0;
        switch (trailbytecount) {
            case 0: {
                result = b[i];
                break;
            }
            case 1: {
                result = (b[i] & 0x1F) << 6;
                result |= (b[i + 1] & 0x3F);
                break;
            }
            case 2: {
                result = (b[i] & 0xF) << 6;
                result = (result | (b[i + 1] & 0x3F)) << 6;
                result |= (b[i + 2] & 0x3F);
                break;
            }
            case 3: {
                result = (b[i] & 0x7) << 6;
                result = (result | (b[i + 1] & 0x3F)) << 6;
                result = (result | (b[i + 2] & 0x3F)) << 6;
                result |= (b[i + 3] & 0x3F);
                break;
            }
            case 4: {
                result = (b[i] & 0x3) << 6;
                result = (result | (b[i + 1] & 0x3F)) << 6;
                result = (result | (b[i + 2] & 0x3F)) << 6;
                result = (result | (b[i + 3] & 0x3F)) << 6;
                result |= (b[i + 4] & 0x3F);
                break;
            }
            case 5: {
                result = (b[i] & 0x1) << 6;
                result = (result | (b[i + 1] & 0x3F)) << 6;
                result = (result | (b[i + 2] & 0x3F)) << 6;
                result = (result | (b[i + 3] & 0x3F)) << 6;
                result = (result | (b[i + 4] & 0x3F)) << 6;
                result |= (b[i + 5] & 0x3F);
                break;
            }
        }
        return result;
    }
    
    public byte[] unicodeToUTF16(int data) {
        if (data < 65535) {
            final byte[] b = { (byte)(data >> 8), (byte)(data & 0xFF) };
            return b;
        }
        if (data >= 65536 && data <= 1114111) {
            final byte[] c = new byte[4];
            data -= 65536;
            int highbit = data >> 10;
            int lowbit = data & 0x3FF;
            highbit += 55296;
            lowbit += 56320;
            c[0] = (byte)(highbit >> 8);
            c[1] = (byte)(highbit & 0xFF);
            c[2] = (byte)(lowbit >> 8);
            c[3] = (byte)(lowbit & 0xFF);
            return c;
        }
        return null;
    }
    
    public byte[] utf8toutf16(String str) throws Exception {
        str = str.replace("%", "");
        final byte[] a = new byte[str.length() / 2];
        final int length = 0;
        final byte[][] result = new byte[str.length() / 2][];
        int index = 0;
        int resultIndex = 0;
        for (int i = 0; i < str.length(); i += 2) {
            a[i / 2] = this.getHexCodePointToByte(str.charAt(i), str.charAt(i + 1));
            final int trailbyteCount = this.getNumberOfTrailingByte(a[i / 2]);
            index = i / 2;
            for (int k = 0; k < trailbyteCount; ++k) {
                i += 2;
                a[i / 2] = this.getHexCodePointToByte(str.charAt(i), str.charAt(i + 1));
            }
            final int unicode = this.getUtf8byteToUnicode(a, index, trailbyteCount);
            final byte[] data = this.unicodeToUTF16(unicode);
            result[resultIndex++] = data;
        }
        final byte[] array = new byte[resultIndex * result[0].length];
        int j = 0;
        for (int l = 0; l < resultIndex; ++l) {
            for (int m = 0; m < result[0].length; ++m) {
                array[j++] = result[l][m];
            }
        }
        return array;
    }
    
    public String int2hex(int dec) {
        final char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        String hex = "";
        while (dec != 0) {
            final int rem = dec % 16;
            hex = hexDigits[rem] + hex;
            dec /= 16;
        }
        return hex;
    }
    
    public int hex2int(final char hex) {
        switch (hex) {
            case '0': {
                return 0;
            }
            case '1': {
                return 1;
            }
            case '2': {
                return 2;
            }
            case '3': {
                return 3;
            }
            case '4': {
                return 4;
            }
            case '5': {
                return 5;
            }
            case '6': {
                return 6;
            }
            case '7': {
                return 7;
            }
            case '8': {
                return 8;
            }
            case '9': {
                return 9;
            }
            case 'A':
            case 'a': {
                return 10;
            }
            case 'B':
            case 'b': {
                return 11;
            }
            case 'C':
            case 'c': {
                return 12;
            }
            case 'D':
            case 'd': {
                return 13;
            }
            case 'E':
            case 'e': {
                return 14;
            }
            case 'F':
            case 'f': {
                return 15;
            }
            default: {
                return -1;
            }
        }
    }
    
    public String byteArrayToHex(final byte[] a) {
        final StringBuilder sb = new StringBuilder(a.length * 2);
        for (final byte b : a) {
            sb.append(String.format("%02x", b & 0xFF));
        }
        return sb.toString();
    }
    
    public String sort(final String data, final String newSeqNumber) {
        final StringBuilder stringBuilder = new StringBuilder();
        final StringBuilder token = new StringBuilder();
        token.append("-").append(newSeqNumber).append("-");
        final int index = data.indexOf(token.toString());
        if (data.trim().length() == 0) {
            stringBuilder.append((CharSequence)token);
        }
        else if (index != -1) {
            stringBuilder.append("-").append(newSeqNumber).append(data.substring(0, index)).append(data.substring(token.length() + index - 1, data.length()));
        }
        else {
            stringBuilder.append("-").append(newSeqNumber).append(data);
        }
        return stringBuilder.toString();
    }
    
    public byte[] byteArraysCopy(byte[] a, byte[] b) {
        if (a == null) {
            a = "".getBytes();
        }
        if (b == null) {
            b = "".getBytes();
        }
        final int aLen = a.length;
        final int bLen = b.length;
        final byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }
    
    static String getSeed() {
        if (Utility.seed >= 10000) {
            Utility.seed = 0;
        }
        ++Utility.seed;
        return String.format("%04d", Utility.seed);
    }
    
    public static String getSessionId(final String Msisdn) {
        final long date = System.currentTimeMillis();
        final String sid = Long.toString(date);
        return sid + Msisdn.substring(7, 10) + "" + getSeed();
    }
    
    public int copyArray(final byte[] source, int startIndex, final int lastIndex, final List<Byte> result) {
        for (int i = startIndex; i < lastIndex; ++i) {
            result.add(source[i]);
            ++startIndex;
        }
        return startIndex;
    }
    
    public byte[] getBytes(final List<Byte> result) {
        final byte[] b = new byte[result.size()];
        for (int index = 0; index < result.size(); ++index) {
            b[index] = result.get(index);
        }
        return b;
    }
    
    public byte[] replace(final byte[] source, final byte[] replace, final byte[] replaceWith) {
        if (source.length < 1 || replace.length < 1) {
            return source;
        }
        final List<Byte> result = new ArrayList<Byte>();
        int startIndex = 0;
        int lastIndex = 0;
        int index = 0;
        int counter = 0;
        while (index < source.length) {
            counter %= replace.length;
            if (source[index] == replace[counter]) {
                ++counter;
            }
            if (counter == replace.length) {
                lastIndex = index - counter + 1;
                this.copyArray(source, startIndex, lastIndex, result);
                this.copyArray(replaceWith, 0, replaceWith.length, result);
                startIndex = lastIndex + counter;
                counter = 0;
            }
            ++index;
        }
        if (startIndex < source.length) {
            this.copyArray(source, startIndex, source.length, result);
        }
        return this.getBytes(result);
    }
    
    public boolean isDate(final String str, final String format) {
        try {
            final SimpleDateFormat formatter = new SimpleDateFormat(format);
            formatter.parse(str);
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }
    
    static {
        Utility.seed = 0;
        hexCode = "0123456789ABCDEF".toCharArray();
    }
}