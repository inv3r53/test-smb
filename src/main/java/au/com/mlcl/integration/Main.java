package au.com.mlcl.integration;

import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;

public class Main {
	public static void main(String[] args) throws Exception {

		// java args -  host "" username password shareName
		// localhost "" sekhtu.dagia XXXXXXX1 share
		if (args == null || args.length !=5) {
			System.out.println("Must pass args: host domain user password share");
			System.exit(1);
		}
		
		String host=args[0].trim();
		String domain=args[1].trim();
		String user=args[2].trim();
		String password=args[3].trim();
		String sharePath=args[4].trim();
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
		}finally{
			client.close();
		}

	}
}
