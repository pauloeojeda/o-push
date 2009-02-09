package de.trantor.wap;

import java.io.IOException;
import java.util.Locale;

import org.xml.sax.DTDHandler;
import org.xml.sax.DocumentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;

/** An implementation of the org.xml.parser interface that  
 *  can be wrapped around a WbxmlParser or WmlParser. 
 */

public class SaxWrapper implements Parser {

    WbxmlParser wrapped;

    /** The wrapper needs a simple
     *  WbxmlParser (or WmlParser) to build on. All 
     *  method invocations are handed over to the
     *  wrapped WbxmlParser.
     */

    public SaxWrapper (WbxmlParser wrapped) {
	this.wrapped = wrapped;
    }


    /** Parse an XML document by invoking the parse
     *  method of the wrapped WbxmlParser. 
     */


    public void parse (InputSource is) throws IOException, SAXException {
	if (wrapped.dh == null) 
	    wrapped.setDocumentHandler (new HandlerBase ());

	wrapped.parse (is.getByteStream ());
    }

    /** Shortcut for  parse(new InputSource(systemId)); 
     */
    

    public void parse (String systemId) throws IOException, SAXException {
        parse(new InputSource(systemId));
    }


    /** passes the DocumentHandler to the wrapped WbxmlParser
     *  given in the constructor 
     */

    public void setDocumentHandler (DocumentHandler h) {
	wrapped.setDocumentHandler (h);
    }


    /** This method is included for compatibility and does 
     *  nothing  since the corresponding functionality is 
     *  not supported by WBXML */

    public void setDTDHandler (DTDHandler ignored) {
    }


    /** This method is included for compatibility and does 
     *  nothing  since the corresponding functionality is 
     *  not supported by WBXML */

    public void setEntityResolver (EntityResolver ignored) {
    }
    

    /** This method is included for compatibility and does 
     *  nothing  since the corresponding functionality is 
     *  not supported by WBXML */

    public void setErrorHandler (ErrorHandler ignored) {
    }



    /** This method is included for compatibility and does 
     *  nothing  since the corresponding functionality is 
     *  not supported by WBXML */

    public void setLocale (Locale ignored) {
    }

}
