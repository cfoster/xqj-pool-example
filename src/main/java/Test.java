import net.xqj.basex.local.BaseXConnectionPoolXQDataSource;
import net.xqj.pool.PooledXQDataSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.xquery.ConnectionPoolXQDataSource;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQResultSequence;

public class Test
{
  static final void ApplicationServerCode() throws Exception
  {
    // Compensating for lack of App Server Container JNDI Context Factory

    System.setProperty(
      "java.naming.factory.initial", "org.osjava.sj.memory.MemoryContextFactory"
    );
    System.setProperty(
      "org.osjava.sj.jndi.shared", "true"
    );

    // ConnectionPoolDS implements the ConnectionPoolXQDataSource
    // interface. Create an instance and set properties.

    ConnectionPoolXQDataSource cpds = new BaseXConnectionPoolXQDataSource();

    cpds.setProperty("user", "admin");
    cpds.setProperty("password", "admin"); // any other properties

    // Register the ConnectionPoolDS with JNDI, using the logical name
    // "xqj/pool/basexserver_pool"

    Context ctx = new InitialContext();
    ctx.bind("xqj/pool/basexserver_pool", cpds);

    // PooledDataSource implements the XQDataSource interface.
    // Create an instance and set properties.
    PooledXQDataSource ds = new PooledXQDataSource();
    ds.setDescription("Datasource with connection pooling");

    // Reference the previously registered ConnectionPoolXQDataSource
    ds.setDataSourceName("xqj/pool/basexserver_pool");

    // Register the XQDataSource implementation with JNDI,
    // using the logical name "xqj/basexserver".

    Context userContext = new InitialContext();
    userContext.bind("xqj/basexserver", ds);     ds=null;
  }


  public static void main(String[] args) throws Exception
  {
    ApplicationServerCode();

    // -----------------------------------------------------------------------
    // User code
    // -----------------------------------------------------------------------
    XQDataSource xqds =
      (XQDataSource)new InitialContext().lookup("xqj/basexserver");

    XQConnection conn = null;

    for(int i = 0 ; i < 100 ; i++)
    {
      // get a connection off the pool
      conn = xqds.getConnection();

      XQResultSequence rs =
        conn.prepareExpression("1,2,3,4").executeQuery();

      while(rs.next())
        System.out.println(rs.getItemAsString(null));

      // returns the XQConnection to the pool behind the scenes
      conn.close();
    }

    System.out.println("Finished.");
    System.exit(0);

    // -----------------------------------------------------------------------
  }
}
