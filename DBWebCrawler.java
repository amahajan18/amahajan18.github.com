package samples.content.dbwebcrawler;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.net.*;
import com.inquira.infra.*;
import com.inquira.navigate.classify.DocTypeClassifier;
import com.inquira.content.*;
import com.inquira.content.custom.*;
import com.inquira.scheduler.job.*;
import com.inquira.util.sql.*;

/* The DBWebCrawler class implements a custom crawler that accesses 
* a database containing URLs of documents to crawl
*/

public class DBWebCrawler extends Crawler

{
	protected Connection conn;
	protected Statement st;
	protected ResultSet rs;
	
	/* Called by the content acquisition framework prior to
	* call starting the crawl
	*/
	
	@Override
	public void connect(CrawlerConfig configuration) throws CrawlerException 
	{
		
		DBWebCrawlerConfig rcc = (DBWebCrawlerConfig)configuration;
		try {
			conn = Datasource.forName( rcc.getDatasourceName( ) ).getConnection( );
			st = conn.createStatement();
			rs = st.executeQuery( rcc.getQuery( ));
			}
		catch( Throwable t ) 
		{
			throw new CrawlerException(t);
			}
	}
	
	/* Called by the content acquisition framework after 
	* the crawl is completed 
	*/
	
	@Override
	public void rundown() throws CrawlerException 
	{
		try
		{
			if( rs != null )
			{
				rs.close();
			}
			if( st != null )
			{
				st.close();
			}
			if( conn != null)
			{
				conn.close();
			}
			
		}
		catch(Throwable t)
		{
			throw new CrawlerException( t );
		}
		
	}
	
	
	
	/* Called by the content acquisition framework prior to call starting 
	* the crawl after calling connect
	*/
	
	@Override
	public void start() {
		
		
	}
	
	
	
	/* Indicates that a single call to the findContent method discovers
	 * a current document
	 */
	@Override
	public boolean findComplete() {
		
		return true;
	}

	/* Indicates that this is a custom crawler */
	
	
	@Override
	public ContentSourceType getType() 
	{
		
		return ContentSourceType.HTTP;
	}
	
	
	/* Returns all currently known document objects that are found
	* in the data source 
	*/
	
	
	@Override
	public Collection findContent(Collection priorContent, CrawlerConfig conf,
			CrawlerState state, TaskStatus status) throws CrawlerException
			{
		Collection rc = new ArrayList( );
		try {
			String temp = null;
			while( rs.next( ) ) {
				String url = rs.getString( 1 );
				
				if( !rs.wasNull( ) && !url.equals( temp ) ) {
					System.out.println( "Getting URL: " + url );
					Timestamp time = rs.getTimestamp(2);
					Document d = new Document( );
					d.setCollection( conf.getCollection( ) );
					d.setFetchURL( url );
					d.setDisplayURL( url );
					d.setCSType( ContentSourceType.CUSTOM );
					d.setLastModificationTime( time );
					d.setIndexingAllowed( true );
					d.setStatusCode( Document.STATUS_OK );
					rc.add( d );
					temp = url;
				}
				else {
					System.out.println( "NULL or Dupe!" );
				}
			}
		}
		catch(Throwable t)
		{
			throw new CrawlerException( t );
		}
		return rc;
	}

	
	/* Returns the raw content for the given document */
	
	
	@Override
	public byte[] getContent(CrawlerConfig conf, Document doc)
			throws CrawlerException
			{
		byte[] rc = null;
		URL url = null;
		URLConnection urlconn = null;
		InputStream is = null;
		ByteArrayOutputStream baos = null;
		//Proxy instance, proxy ip = 10.0.0.1 with port 8080
		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("172.18.65.22", 80));
		//conn = new URL(urlString).openConnection(proxy);
		
		try {
			url= new URL( doc.getFetchURL( ) );
			System.out.println( "In getContent, getting URL: " + url );
			
			urlconn = url.openConnection(proxy );
			
			is = new BufferedInputStream( urlconn.getInputStream( ) );
			baos = new ByteArrayOutputStream( );
			byte[] buf = new byte[8192];
			int count = 0;
			while( ( count = is.read( buf, 0, buf.length ) ) > 0 ) {
				baos.write( buf, 0, count );
			}
			rc = baos.toByteArray( );
			doc.setContent( DataComponent.RAW, rc );
			doc.setDocSize( rc.length );
			if(doc.getFetchURL( ).contains(".pdf")){
				doc.setDocType(DocumentType.PDF);
			}else{
				doc.setDocType(DocumentType.CMS_HTML);
			}
			System.out.println("Document to string " + doc.toString());
			
		}
		catch(ContentStoreException t)
		{
			throw new CrawlerException( t );
			}
		catch( IOException t )
		{
			throw new CrawlerException( t );
		}
		finally {
			if( is != null ) {
			try {
			is.close( );
			} catch( IOException ex ) {
			// ignore on close
			}
			}
		}
		
		return rc;
	}

}
