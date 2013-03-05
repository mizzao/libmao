package net.andrewmao.misc;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import net.andrewmao.misc.Base64Coder;

public class Utils {

	private static final String SIMPLE_TIMESTAMP_FORMAT = "yyyy-MM-dd HH.mm.ss.SSS";
	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(SIMPLE_TIMESTAMP_FORMAT);

	private static MessageDigest md;	
	
	static {
		try { md = MessageDigest.getInstance("SHA-1"); }
		catch (NoSuchAlgorithmException e) {}
	}
	
	public synchronized static String base64Hash(BigInteger bi) {
		md.update(bi.toByteArray());
		byte[] output = md.digest();
		return Base64Coder.encodeLines(output, 0, output.length, 120, "");
	}
	
	public synchronized static String base64Hash(String s) {
		md.update(s.getBytes());
		byte[] output = md.digest();
		return Base64Coder.encodeLines(output, 0, output.length, 120, "");
	}
	
	public static String clockString(long millis) {
		long totalSecs = millis / 1000;
		
		long mins = totalSecs / 60;
		long secs = totalSecs % 60;
		
		return String.format("%d:%02d", mins, secs);		
	}
	
	public static String paddedClockString(long millis) {
		long totalSecs = millis / 1000;
		
		long mins = totalSecs / 60;
		long secs = totalSecs % 60;
		
		return String.format("%02d:%02d", mins, secs);		
	}

	public static String getCurrentTimeZoneTimeAsString() {						
		return DATE_FORMAT.format(Calendar.getInstance().getTime());
	}
	
	public static String getTimeString(long startTime) {		
		return DATE_FORMAT.format(new Date(startTime));
	}

	public static String getTimeStringFromDate(Date date) {
		return DATE_FORMAT.format(date);
	}

	public static long getEpochFromString(String formatted) throws ParseException {
		return DATE_FORMAT.parse(formatted).getTime();
	}
	
	public static String clockStringMillis(long millis) {
		long totalSecs = millis / 1000;
		
		long mins = totalSecs / 60;
		long secs = totalSecs % 60;
		
		return String.format("%02d:%02d.%03d", mins, secs, millis % 1000);		
	}

	public static InetAddress getNetworkAddr() {
		InetAddress localAddr = null;

		// Find our public IP address
		Enumeration<NetworkInterface> netInterfaces;
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {				
				NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();				
				Enumeration<InetAddress> addresses = ni.getInetAddresses();				
				while( addresses.hasMoreElements() ) {
					InetAddress addr = addresses.nextElement();

					// System.out.println("Checking out " + ni.getName() + " with address " + addr.toString());

					if (!addr.isSiteLocalAddress() && 
							!addr.isLoopbackAddress() && 
							!addr.isLinkLocalAddress() &&
							addr.getHostAddress().indexOf(":") == -1) { // MAC/IPv6 address detection
						System.out.println("Interface " + ni.getName()
								+ " seems to be InternetInterface. I'll take address " + addr.toString());
						System.out.println("Associated hostname: " + addr.getHostName());
						localAddr = addr;
						break;
					}
				}	
				if( localAddr != null ) break;
			}
		} catch( NoSuchElementException e) {
			System.out.println("Couldn't find a public address");
			localAddr = null;
		} catch (SocketException e) {
			e.printStackTrace();
			localAddr = null;
		}

		return localAddr;
	}
	
}
