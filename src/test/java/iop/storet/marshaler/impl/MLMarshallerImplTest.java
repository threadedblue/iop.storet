package iop.storet.marshaler.impl;

import static org.junit.Assert.assertNotNull;
import iop.account.account.Account;
import iop.account.account.AccountFactory;
import iop.storet.accumulod4m.builders.XML2PairImpl.RowColVal;
import iop.storet.accumulod4m.util.C;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import accumulo.provider.AccumuloProvider;
import accumulo.provider.impl.AccumuloProviderImpl;

public class MLMarshallerImplTest {

	private static final Logger log = LoggerFactory
			.getLogger(MLMarshallerImplTest.class);
	static final String path = "/Users/gcr/git/osgiWorkspace/ERHWorkarea/sample-ccdas";
	MLMarshalerImpl app;
	static List<File> files;
	String scd;

	@BeforeClass
	public static void oneTimeSetUp() {
		File root = new File(path);
		files = listFiles(new ArrayList<File>(), root);
		AccumuloProvider provider = new AccumuloProviderImpl();
	}

	@Before
	public void setUp() {
		scd = readCCD(files.get(0));
		Account acct = AccountFactory.eINSTANCE.createAccount();
		acct.setNumber(1L);
		app = new MLMarshalerImpl(scd, C.ENDIAN.LITTLE, C.SANS_ROOT.FALSE, C.DELIMITER.BAR);
		assertNotNull(app);
	}

	@Test
	public void testRun() {
		log.debug("start==>");
		app.build();
//		try {
//			Authorizations auths = new Authorizations("PUBLIC");
//			Scanner scanTedge = TedgeMarshalerImpl.provider.getConnection()
//					.createScanner("Tedge", auths);
//			assertNotNull(scanTedge);
//			scanTedge.setRange(new Range(app.formatAcctNo(1L), app.formatAcctNo(1L)));
//			for (Entry<Key, Value> entry : scanTedge) {
//				Text row = entry.getKey().getRow();
//				log.debug("row=" + row.toString());
//				Value value = entry.getValue();
//				log.debug("value=" + value.toString());
//			}
//			Scanner scanTedgeText = TedgeMarshalerImpl.provider.getConnection()
//					.createScanner("TedgeText", auths);
//			assertNotNull(scanTedgeText);
//			scanTedgeText.setRange(new Range(app.formatAcctNo(1L), app.formatAcctNo(1L)));
//			for (Entry<Key, Value> entry : scanTedgeText) {
//				Text row = entry.getKey().getRow();
//				log.debug("row=" + row.toString());
//				Value value = entry.getValue();
//				log.debug("value=" + value.toString());
//			}
//		} catch (TableNotFoundException e) {
//			log.error("", e);
//		}
	}

//	@Test
//	public void testFormatAcctNo() {
//		String s1 = app.formatAcctNo(1L);
//		assertEquals("0000000000000001", s1);
//		String s321 = app.formatAcctNo(321L);
//		assertEquals("0000000000000321", s321);
//	}

	@Test
	public void testGetFamilies() {
		List<RowColVal> list = app.getFamsNVals(scd);
		assertNotNull(list);
	}

	@Test
	public void testGetDocumentIdString() {
		try {
			String id = app.getDocumentId(scd);
			assertNotNull(id);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	static List<File> listFiles(List<File> list, File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles(new CDAFilter());
			for (File file1 : files) {
				listFiles(list, file1);
			}
		} else {
			list.add(file);
		}
		return list;
	}

	public String readCCD(File filename) {
		String s = null;
		try {
			java.net.URI uri = filename.toURI();
			s = new String(Files.readAllBytes(Paths.get(uri)));
		} catch (IOException e) {
			log.error("", e);
		}
		return s;
	}
}