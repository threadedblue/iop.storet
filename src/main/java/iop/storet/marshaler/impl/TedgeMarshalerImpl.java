package iop.storet.marshaler.impl;

import iop.account.account.Account;
import iop.account.account.AccountFactory;
import iop.storet.accumuloemf.TedgeEMFcoder;
import iop.storet.accumuloemf.XML2Tedge;
import iop.storet.accumuloemf.builders.TedgeEMFcoderImpl;
import iop.storet.accumuloemf.builders.XML2TedgeImpl;
import iop.storet.accumuloemf.util.C;
import iop.storet.marshaler.TedgeMarshaler;

import java.io.Reader;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.apache.accumulo.core.client.Connector;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import accumulo.provider.AccumuloProvider;

public class TedgeMarshalerImpl implements Runnable, TedgeMarshaler {
	
	private static final Logger log = LoggerFactory.getLogger(TedgeMarshalerImpl.class);

	private static AccumuloProvider provider;

	String scd;
	
	public TedgeMarshalerImpl(String scd) {
		Account acct = AccountFactory.eINSTANCE.createAccount();
		this.scd = scd;
	}
	
	/* (non-Javadoc)
	 * @see iop.storet.marshaler.impl.TedgeMarshaller#run()
	 */
	@Override
	public void run() {
		Connector conn = provider.getConnection();
		Account acct = AccountFactory.eINSTANCE.createAccount();
		String documentId = null;
		try {
			documentId = getDocumentId(scd);
		} catch (Exception e) {
			log.error("", e);
		}
		List<String> families = getFamilies(scd);
		acct.setNumber(1L);
		TedgeEMFcoder coder = new TedgeEMFcoderImpl(conn, formatAcctNo(acct.getNumber()), documentId, scd, families);
		coder.run();
	}
	
	String formatAcctNo(long acctNo) {
		NumberFormat fmt = DecimalFormat.getInstance();
		 if (fmt instanceof DecimalFormat) {
		     ((DecimalFormat) fmt).applyPattern(ACCOUNT_NO_FORMAT);
		 }
		return fmt.format(acctNo);
	}
	
	List<String> getFamilies(String scd) {
		XML2Tedge xml = new XML2TedgeImpl(scd);
		return xml.build();
	}
	
	String getDocumentId(String scd) throws Exception {
		Reader reader = new StringReader(scd);
		InputSource is = new InputSource(reader);
		ClinicalDocument cd = CDAUtil.load(is);
		return getDocumentId(cd);
	}
	
	String getDocumentId(ClinicalDocument cd) {
		II ii = cd.getId();
		StringBuilder bld = new StringBuilder();
		bld.append(ii.getRoot() != null ? ii.getRoot() : "was null");
		bld.append(C.DEFAULT_DELIMITER);
		bld.append(ii.getExtension() != null ? ii.getExtension() : "");
		return bld.toString();
	}
}
