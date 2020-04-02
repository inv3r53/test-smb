package au.com.mlcl.integration;

import java.io.Console;

import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;

public class Main {
	public static void main(String[] args) throws Exception {

		// java args - host "" username  shareName password
		// localhost "" su.dg XXXXXXX1 share
		if (args == null || args.length != 4) {
			System.out.println("Must pass args: host domain user share [password]");
			System.exit(1);
		}
		String password="";
		String host = args[0].trim();
		String domain = args[1].trim();
		String user = args[2].trim();
		String sharePath = args[3].trim();
		if(args.length==5){
			 password = args[3].trim();
		}else{
            Console console = System.console();

            if (console != null) {
                System.out.print("Enter password for " + user + ": ");
                password = new String(console.readPassword());
                System.out.println("----------------------------------------------------");
            } else {
                throw new Exception("Samba password not found.");
            }
        }
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
}
