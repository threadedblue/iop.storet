package iop.ccd2d4m;

import iop.ccd.shread.RowColVal;
import iop.ccd.shread.RowsColsVals;
import iop.ccd.shread.XML2Set;
import iop.ccd.shread.impl.AbstractXML2SetImpl;
import iop.ccd.shread.impl.XML2PathSetImpl;
import iop.ccd.shread.util.C;
import iop.tictoc.TicToc;
import iop.tictoc.impl.TicTocImpl;
import iop.xslt.XSLTProcessor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.dom4j.Document;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.mit.ll.cloud.connection.ConnectionProperties;
import edu.mit.ll.d4m.db.cloud.D4mException;
import edu.mit.ll.d4m.db.cloud.accumulo.AccumuloConnection;
import edu.mit.ll.d4m.db.cloud.accumulo.AccumuloInsert;
import edu.mit.ll.d4m.db.cloud.accumulo.AccumuloTableOperations;

public class CCD2D4M implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(CCD2D4M.class);

	private CmdLineParser CLI = new CmdLineParser(this);

	@Option(name = "-i", aliases = "--instance", required = true, usage = "Accumulo instance name")
	private String instance;

	@Option(name = "-h", aliases = "--host", required = true, usage = "Zookeeper host name with port")
	private String host;

	@Option(name = "-u", aliases = "--user", required = true, usage = "Accumulo user name")
	private String user;

	@Option(name = "-p", aliases = "--password", required = false, usage = "Accumulo password")
	private String password = "";

	@Option(name = "-m", aliases = "--stem", required = true, usage = "Stem value for D4M table names")
	private String stem;

	@Option(name = "-l", aliases = "--dblogi", required = false, usage = "Contruct input for dblLogi.")
	private boolean dblLogi;

	@Option(name = "-f", aliases = "--path", required = true, usage = "Path to the directory or file.")
	private String path;

	@Option(name = "-x", aliases = "--style", required = false, usage = "XSL file name")
	private String xsl;

	private final String tab;
	private final String tabT;
	private final String tabDeg;
	private final String tabTxt;

	AccumuloInsert ins;
	AccumuloInsert insT;
	AccumuloInsert insDeg;
	AccumuloInsert insTxt;

	int documentCount;
	int elementCountper;
	int elementCount;

	XSLTProcessor xslt;

	public CCD2D4M(String[] args) throws CmdLineException {
		super();
		try {
			CLI.parseArgument(args);
			ConnectionProperties props = new ConnectionProperties();
			props.setInstanceName(instance);
			props.setHost(host);
			props.setUser(user);
			props.setPass(password);
			props.setMaxNumThreads(50);
			AccumuloConnection conn = new AccumuloConnection(props);
			tab = stem;
			ins = new AccumuloInsert(props.getInstanceName(), props.getHost(), tab, props.getUser(), props.getPass());
			conn.createTable(tab);
			tabT = stem + C.TRANSFORM;
			insT = new AccumuloInsert(props.getInstanceName(), props.getHost(), tabT, props.getUser(), props.getPass());
			conn.createTable(tabT);
			tabDeg = stem + C.DEGREE;
			insDeg = new AccumuloInsert(props.getInstanceName(), props.getHost(), tabDeg, props.getUser(), props.getPass());
			conn.createTable(tabDeg);
			tabTxt = stem + C.TEXT;
			insTxt = new AccumuloInsert(props.getInstanceName(), props.getHost(), tabTxt, props.getUser(), props.getPass());
			conn.createTable(tabTxt);
		} catch (CmdLineException e) {
			CLI.printUsage(System.out);
			throw e;
		}
	}

	public void run() {
		File fileList = new File(path);
		File[] list = fileList.listFiles();
		ConnectionProperties props = new ConnectionProperties();
		props.setInstanceName(instance);
		props.setHost(host);
		props.setUser(user);
		props.setPass(password);

		props.setMaxNumThreads(10);
		AccumuloTableOperations ops = new AccumuloTableOperations(props);
		try {
			ops.designateCombiningColumns(tabDeg, "Degree", "sum", "");
		} catch (D4mException e) {
			log.error("", e);
		}
		TicToc tt0 = new TicTocImpl();
		tt0.tic();
		for (File file : list) {
			insertCCD(file);
			documentCount++;
			heartBeat();
		}
		tt0.toc();
		log.info("tt0all=" + tt0.hhmmss() + " size= " + list.length);
	}

	void insertCCD(File file) {
		String ccd = readFileContents(file);
		Document tranformedDoc = transformDoc(ccd);
		XML2Set xml2Set = new XML2PathSetImpl(tranformedDoc);
		RowsColsVals rcvs = xml2Set.build();
		elementCountper = rcvs.size();
		elementCount += elementCountper;
		String[] rcv4Ingest = null;
		rcv4Ingest = rcvs.assemble4Ingest();
		ins.doProcessing(rcv4Ingest[C.ROW], rcv4Ingest[C.COL],
				rcv4Ingest[C.VAL], "", "");
		insT.doProcessing(rcv4Ingest[C.COL], rcv4Ingest[C.ROW],
				rcv4Ingest[C.VAL], "", "");
		String[] rcv4Degree = assemble4Degree(rcv4Ingest);
		insDeg.doProcessing(rcv4Degree[C.ROW], rcv4Degree[C.COL],
				rcv4Degree[C.VAL], "", "");
	}

	Document transformDoc(String ccd) {
		Document doc = AbstractXML2SetImpl.parse(ccd);
		return transformDoc(doc);
	}

	Document transformDoc(Document doc) {
		XSLTProcessor xslt = null;
		xslt = getXSLTProcessor();
		return xslt.run(doc);
	}

	XSLTProcessor getXSLTProcessor() {
		try {
			if (xslt == null) {
				String xslContents = readFileContents(xsl);
				xslt = new XSLTProcessor(xslContents);
			}
		} catch (URISyntaxException e) {
			log.error("", e);
		}
		return xslt;
	}

	String lastCharOf(String s) {
		return s.substring(s.length() - 1);
	}

	public String readFileContents(String filename) throws URISyntaxException {
		log.debug("filename=" + filename);
		URL url = getClass().getClassLoader().getResource(filename);
		log.debug("url=" + url);
		return readFileContents(url.toURI());
	}

	public String readFileContents(File filename) {
		java.net.URI uri = filename.toURI();
		return readFileContents(uri);
	}

	public String readFileContents(URI uri) {
		String s = null;
		try {
			s = new String(Files.readAllBytes(Paths.get(uri)));
		} catch (IOException e) {
			log.error("", e);
		}
		return s;
	}

	String[] assemble4Degree(String[] rcv) {
		String[] sss = new String[3];
		sss[C.ROW] = rcv[C.ROW].split("\t")[0] + C.DEFAULT_DELIMITER;
		sss[C.COL] = "Degree" + C.DEFAULT_DELIMITER;
		sss[C.VAL] = String.valueOf(rcv[C.VAL].split("\t").length)
				+ C.DEFAULT_DELIMITER;
		return sss;
	}

	void heartBeat() {
		if (documentCount % 100 == 0) {
			log.info("documents=" + documentCount);
			log.info("elements per=" + elementCountper);
			log.info("elements=" + elementCount);
		}
	}

	public static void main(String[] args) {
		try {
			CCD2D4M app = new CCD2D4M(args);
			log.info("Start==>");
			app.run();
			log.info("<==Finish");
		} catch (CmdLineException e) {
			log.error("Soaping is wrong.", e);
		}
	}
}
