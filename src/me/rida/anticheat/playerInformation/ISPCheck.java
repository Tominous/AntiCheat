package me.rida.anticheat.playerInformation;

import java.net.InetSocketAddress;

public class ISPCheck {


	public static String getISP(InetSocketAddress ip) {


		return ip.getHostName().toString();
	}
}
