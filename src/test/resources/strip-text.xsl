<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	version='2.0' xmlns:hl7="urn:hl7-org:v3">

	<xsl:output indent="yes" />
	<xsl:strip-space elements="*" />

	<!-- identity template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

	<!-- remove descriptive sections -->
	<xsl:template match="hl7:text" />

</xsl:stylesheet>