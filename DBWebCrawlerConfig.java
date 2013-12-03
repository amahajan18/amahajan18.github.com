package samples.content.dbwebcrawler;
import java.util.*;

import com.inquira.content.*;
import com.inquira.content.custom.*;

/* The CustomCrawlerConfig class implements a custom crawler configuration
* object that knows about two non-standard configuration items:
*
* datasourceName - defines the name of the data source that
* contains the document information
* query - defines the query string used to find the document information
*/

public class DBWebCrawlerConfig extends CustomCrawlerConfig
{
	private static final String __ident = "$Revision: 1.1.2.2 $";
	
	/* Compares the last modification dates of the two documents passed,
	* to determine if the document has changed
	*/
	
	@Override
	public boolean isModifiedDocument(Document currentDocument, Document newDocument) {
		
		return newDocument.getLastModificationTime( ).after( currentDocument.getLastModificationTime( ));
	}
	
	/* Returns the data source name */
	
	public String getDatasourceName( )throws CrawlerException
	{
		String dataSourceName = configValues.getProperty( "datasourceName" );
		if( dataSourceName == null || dataSourceName.length( ) == 0 )
		{
			throw new CrawlerException( "CUSTOM_DBWEB_CRAWLER_NO_DATASOURCE", new Object[]{ getCollectionName( )} );
		}
		return dataSourceName;
	}
	
	
	/* Returns the query string */
	
	public String getQuery( )throws CrawlerException
	{
		
		String query = configValues.getProperty( "query" );
		if( query == null || query.length( ) == 0 ) {
			throw new CrawlerException( "CUSTOM_DBWEB_CRAWLER_NO_QUERY", new Object[]{
					getCollectionName( ) } );
			}
		return query;
	}
	
	
	/* Returns a new DBWebCrawler object */
	
	
	@Override
	public Crawler getCrawler() throws CrawlerException {
		
		return new DBWebCrawler( );
	}

	/* Indicates that this crawler compares existing documents in the
	* content store with documents it discovers to identify content changes
	*/
	
	public boolean fetchExistingContent( )
	{
	return true;
	}
	

}
