package au.com.mlcl.integration;

import java.io.Console;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;

import jcifs.smb1.smb1.NtlmPasswordAuthentication;
import jcifs.smb1.smb1.SmbFile;

public class Main {
	public static void main(String[] args) throws Exception {

		// java args - host "" username shareName password
		// localhost "" su.dg XXXXXXX1 share
		if (args == null || args.length != 4) {
			System.out.println("Must pass args: host domain user share [password]");
			System.exit(1);
		}
		String password = "";
		String host = args[0].trim();
		String domain = args[1].trim();
		String user = args[2].trim();
		String sharePath = args[3].trim();
		if (args.length == 5) {
			password = args[3].trim();
		} else {
			Console console = System.console();

			if (console != null) {
				System.out.print("Enter password for " + user + ": ");
				password = new String(console.readPassword());
				System.out.println("----------------------------------------------------");
			} else {
				throw new Exception("Samba password not found.");
			}
		}
		System.out.println("###############SMBJ Start#######################");
		try {
			smbj(host, domain, user, sharePath, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("###############SMBJ End#######################");

		System.out.println("###############JCIFS Start#######################");
		try {
			jcifs(host, domain, user, sharePath, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("###############JCIFS End#######################");
	}

	private static void smbj(String host, String domain, String user, String sharePath, String password)
			throws Exception {
		SMBClient client = new SMBClient();
		try (Connection connection = client.connect(host)) {
			AuthenticationContext ac = new AuthenticationContext(user, password.toCharArray(), domain);
			Session session = connection.authenticate(ac);

			// Connect to Share
			try (DiskShare share = (DiskShare) session.connectShare(sharePath)) {
				for (FileIdBothDirectoryInformation f : share.list("")) {
					System.out.println("File : " + f.getFileName());
				}
			}
		} finally {
			client.close();
		}
	}

	private static void jcifs(String host, String domain, String user, String sharePath, String password) {
		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, user, user);

		String path = getSambaConnectionString(host, sharePath);

		try {
			SmbFile resource = new SmbFile(path, auth);
			if (resource.isFile()) {

				System.out.println("Resource Found=> " + resource.getPath());
			} else if (resource.list().length > 0) {
				SmbFile[] fileList = resource.listFiles();
				System.out.println("Resource is a Folder. Below are the files in this folder => " + resource.getPath());
				for (SmbFile file : fileList) {

					System.out.println("Resource Found=> " + file.getPath());
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String getSambaConnectionString(String host, String sharePath) {
		String FRONT_SLASH = "/";
		String BACK_SLASH = "\\";
		String folder = sharePath;
		String connStringPostFix = FRONT_SLASH;
		if (folder.endsWith(FRONT_SLASH) || folder.endsWith(BACK_SLASH))
			connStringPostFix = "";

		StringBuilder connStr = new StringBuilder();
		connStr.append("smb://").append(host).append(FRONT_SLASH).append(folder).append(connStringPostFix);

		return connStr.toString();
	}
}
