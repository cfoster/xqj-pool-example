package simple;

import net.xqj.basex.local.BaseXConnectionPoolXQDataSource;
import net.xqj.pool.PooledXQDataSource;
import javax.xml.xquery.ConnectionPoolXQDataSource;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQResultSequence;

public class WithoutJNDI
{
  public static void main(String[] args) throws Exception
  {
    ConnectionPoolXQDataSource cpds = new BaseXConnectionPoolXQDataSource();

    cpds.setProperty("user", "admin");
    cpds.setProperty("password", "admin"); // any other properties

    PooledXQDataSource xqds = new PooledXQDataSource(cpds);
    xqds.setDescription("Datasource with connection pooling");

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
