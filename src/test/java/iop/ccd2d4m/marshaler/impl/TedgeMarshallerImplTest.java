package iop.storet.marshaler.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

public class TedgeMarshallerImplTest {
	
	private static final Logger log = LoggerFactory
			.getLogger(TedgeMarshallerImplTest.class);
	static final String path = "/Users/gcr/git/osgiWorkspace/ERHWorkarea/sample-ccdas";
	TedgeMarshalerImpl app;
	static List<File> files;
	String scd;
	
	@BeforeClass
	public static void oneTimeSetUp() {
		File root = new File(path);
		files = listFiles(new ArrayList<File>(), root);
	}
	
	@Before
	public void setUp() {
		scd = readCCD(files.get(0));
		app = new TedgeMarshalerImpl(scd);
	}

	@Test
	public void testRun() {
		app.run();
	}

	@Test
	public void testFormatAcctNo() {
		String s1 = app.formatAcctNo(1L);
		assertEquals("0000000000000001", s1);
		String s321 = app.formatAcctNo(321L);
		assertEquals("0000000000000321", s321);
	}

	@Test
	public void testGetFamilies() {
		List<String> list = app.getFamilies(scd);
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
