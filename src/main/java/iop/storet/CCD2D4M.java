package iop.storet;


import iop.ccd.prep.XSLTProcessor;
import iop.ccd.shread.RowsColsVals;
import iop.ccd.shread.XML2Set;
import iop.ccd.shread.impl.AbstractXML2SetImpl;
import iop.ccd.shread.impl.XML2PathSetImpl;
import iop.ccd.shread.util.C;
import iop.ccd.shread.util.CCDUtils;
import iop.ccd.shread.util.U;
import iop.tictoc.TicToc;
import iop.tictoc.impl.TicTocImpl;

import java.io.File;

import org.dom4j.Document;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.mit.ll.cloud.connection.ConnectionProperties;
import edu.mit.ll.d4m.db.cloud.D4mException;
import edu.mit.ll.d4m.db.cloud.accumulo.AccumuloInsert;
import edu.mit.ll.d4m.db.cloud.accumulo.AccumuloTableOperations;

public class CCD2D4M implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(CCD2D4M.class);

	private CmdLineParser CLI = new CmdLineParser(this);

	private CCDUtils utilC = new CCDUtils();

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

	@Option(name = "-s", aliases = "--sansroot", required = false, usage = "Construct without path root.")
	private boolean sansroot;

	@Option(name = "-e", aliases = "--endian", required = false, usage = "Construct T big or F little endian values.")
	private boolean endian;

	@Option(name = "-l", aliases = "--dblogi", required = false, usage = "Contruct input for dblLogi.")
	private boolean dblLogi;

	@Option(name = "-f", aliases = "--path", required = true, usage = "Path to the directory or file.")
	private String path;

	@Option(name = "-t", aliases = "--stops", required = false, usage = "Delimited list of element names not to use. Last character is the delimiter. Do not use space.")
	private String stops;

	@Option(name = "-z", aliases = "--size", required = true, usage = "Size to sample,-1 sampes all.")
	private int size;

	private final String tab;
	private final String tabT;
	private final String tabDeg;
	private final String tabTxt;

	AccumuloInsert ins;
	AccumuloInsert insT;
	AccumuloInsert insDeg;

	int documentCount;
	int elementCount;

	public CCD2D4M(String[] args) throws CmdLineException {
		super();
		try {
			CLI.parseArgument(args);
			tab = stem;
			tabT = stem + C.TRANSFORM;
			tabDeg = stem + C.DEGREE;
			tabTxt = stem + C.TEXT;
			ins = new AccumuloInsert(instance, host, tab, user, password);
			insT = new AccumuloInsert(instance, host, tabT, user, password);
			insDeg = new AccumuloInsert(instance, host, tabDeg, user, password);
		} catch (CmdLineException e) {
			CLI.printUsage(System.out);
			throw e;
		}
	}

	public void run() {
		File fileList = new File(path);
		File[] list = CollectionSampler.assembleRands(size, fileList);
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
		String ccd = (String) utilC.readCCD(file);
		Document doc = AbstractXML2SetImpl.parse(ccd);
		Document tranformedDoc = transformDoc(doc);
		XML2Set xml2Set = new XML2PathSetImpl(tranformedDoc, U.getEndian(endian),
				U.getSansroot(sansroot), dblLogi);
		RowsColsVals rcvs = xml2Set.build();
		elementCount += rcvs.size();
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
	
	Document transformDoc(Document doc) {
		XSLTProcessor xslt = new XSLTProcessor("");
		return xslt.run(doc);
	}
	
	String lastCharOf(String s) {
		return s.substring(s.length() -1);
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
