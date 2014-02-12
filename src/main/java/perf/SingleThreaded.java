package perf;

import net.xqj.basex.local.BaseXXQDataSource;
import net.xqj.basex.local.BaseXConnectionPoolXQDataSource;

// import net.xqj.basex.BaseXXQDataSource;
// import net.xqj.basex.BaseXConnectionPoolXQDataSource;
import net.xqj.pool.PooledXQDataSource;
import perf.util.Performance;

import javax.xml.xquery.*;
import java.text.NumberFormat;
import java.util.Properties;

public class SingleThreaded
{
  static final int ITERATIONS = 100;
  static final boolean EXECUTE_QUERY = true;

  public static void main(String[] args) throws XQException
  {
    ConnectionPoolXQDataSource cpds = new BaseXConnectionPoolXQDataSource();

    cpds.setProperties(connectionProperties());

    XQDataSource pooled = new PooledXQDataSource(cpds);

    XQDataSource unpooled = new BaseXXQDataSource();

    unpooled.setProperties(connectionProperties());

    Performance perf = new Performance();

    long memBefore=0, memAfter=0;

    memBefore = Performance.memory();
    perf.time(); // reset timer
    connectQueryClose(unpooled, ITERATIONS, EXECUTE_QUERY);
    memAfter = Performance.memory();

    formatResult(
      "Regular XQDataSource",
      perf.time(),
      ITERATIONS,
      memAfter - memBefore
    );

    Performance.gc(100);

    memBefore = Performance.memory();
    perf.time(); // reset timer
    connectQueryClose(pooled, ITERATIONS, EXECUTE_QUERY);
    memAfter = Performance.memory();

    formatResult(
      "Pooled XQDataSource",
      perf.time(),
      ITERATIONS,
      memAfter - memBefore
    );

    System.out.println("Finished.");
    System.exit(0);
  }

  static final void formatResult(
    String dsType,
    long totalElapsed,
    int iterations,
    long memoryConsumed
  )
  {
    StringBuilder b = new StringBuilder();
    b.append(dsType);
    b.append(" took a total of ");
    b.append(formatNumber((double)totalElapsed / (double)1000000000));
    b.append(" seconds to execute ");
    b.append(formatNumber(iterations));
    b.append(" iterations, ");
    b.append(Performance.getTime(totalElapsed, iterations));
    b.append(" and the local JVM consumed ");
    b.append(Performance.format(memoryConsumed));

    System.out.println(b);
  }

  static void connectQueryClose(
    XQDataSource ds, int times, boolean executeQuery) throws XQException
  {
    for(int i=0;i<times;i++)
    {
      XQConnection conn = ds.getConnection();

      if(executeQuery)
        conn.prepareExpression("1,2,3,4").executeQuery();

      conn.close();
    }
  }

  static final String formatNumber(long number) {
    return NumberFormat.getInstance().format(number);
  }

  static final String formatNumber(double number) {
    return NumberFormat.getInstance().format(number);
  }


  static final Properties connectionProperties() {
    Properties properties = new Properties();
    properties.setProperty("user", "admin");
    properties.setProperty("password","admin");
    return properties;
  }
}
