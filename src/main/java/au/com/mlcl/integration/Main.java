package au.com.mlcl.integration;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;

public class Main {
	public static void main(String[] args) throws Exception {

		// java args - host "" username shareName password
		// localhost "" su.dg XXXXXXX1 share
		if (args == null || args.length < 5) {
			System.out.println("Must pass args: host domain user share folder [password]");
			System.exit(1);
		}
		String password = "";
		String host = args[0].trim();
		String domain = args[1].trim();
		String user = args[2].trim();
		String sharePath = args[3].trim();
		String folder = args[4].trim();
		if (args.length == 6) {
			password = args[5].trim();
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
		SMBClient client = new SMBClient();
		try (Connection connection = client.connect(host)) {
			AuthenticationContext ac = new AuthenticationContext(user, password.toCharArray(), domain);
			Session session = connection.authenticate(ac);

			// Connect to Share
			try (DiskShare share = (DiskShare) session.connectShare(sharePath)) {
				for (FileIdBothDirectoryInformation f : share.list("")) {
					System.out.println("File : " + f.getFileName());
				}

				writeFile(share, "smb-test.txt", folder, false, "hello test" + (new Date()), "UTF-8");
			}
		} finally {
			client.close();
		}

	}

	private static void writeFile(DiskShare share, String fileName, String dirName, boolean append, Object data,
			String encoding) throws Exception {
		OutputStream out = null;
		try {
			Set<FileAttributes> fileAttributes = new HashSet<>();
			fileAttributes.add(FileAttributes.FILE_ATTRIBUTE_NORMAL);
			Set<SMB2CreateOptions> createOptions = new HashSet<>();
			createOptions.add(SMB2CreateOptions.FILE_RANDOM_ACCESS);
			createOptions.add(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE);

			File f;
			String fullPath = buildPath(dirName, fileName);
			if (dirName.trim().isEmpty()) {
				fullPath = fullPath.substring(1);
			}
			if (append) {
				f = share.openFile(fullPath, EnumSet.of(AccessMask.GENERIC_ALL), fileAttributes, SMB2ShareAccess.ALL,
						SMB2CreateDisposition.FILE_OPEN_IF, createOptions);
			} else {
				f = share.openFile(fullPath, EnumSet.of(AccessMask.GENERIC_ALL), fileAttributes, SMB2ShareAccess.ALL,
						SMB2CreateDisposition.FILE_OVERWRITE_IF, createOptions);
			}

			out = f.getOutputStream(append);

			if (data instanceof InputStream) {
				InputStream in = (InputStream) data;
				byte[] buffer = new byte[1024];
				while (in.read(buffer) > -1) {
					out.write(buffer);
				}
				out.flush();
				out.close();
				in.close();
			} else if (data instanceof byte[]) {
				byte[] dataBytes = (byte[]) data;
				out.write(dataBytes);
				out.flush();
				out.close();
			} else if (data instanceof String) {
				byte[] dataBytes = ((String) data).getBytes(encoding);
				out.write(dataBytes);
				out.flush();
				out.close();
			} else {
				throw new RuntimeException("unsupported object type for file write: " + data.getClass()
						+ ", supported types are InputStream, String or byte[]");
			}
			f.close();
			out = null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) {
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					System.err.println("error closing out file writer");
				}
			}
		}
	}

	private static String buildPath(String path1, String path2) {
		if (path1 != null && path2 != null) {
			path1 = cleanPath(path1);
			path2 = cleanPath(path2);
			StringBuilder sb = new StringBuilder(path1 + "/" + path2);
			return sb.toString();
		} else if (path1 == null && path2 != null) {
			return path2;
		} else if (path1 != null && path2 == null) {
			return path1;
		} else {
			return null;
		}
	}

	private static String cleanPath(String path) {
		if (path != null) {
			StringBuilder sb = new StringBuilder(path.replaceAll("(/)+", "/"));
			if (path.startsWith("/")) {
				sb.replace(0, 1, "");
			}
			if (path.endsWith("/")) {
				sb.replace(sb.length() - 1, sb.length(), "");
			}
			return sb.toString();
		} else {
			return "";
		}
	}

}
