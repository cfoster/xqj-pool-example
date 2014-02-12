package perf;

// import net.xqj.basex.local.BaseXXQDataSource;
// import net.xqj.basex.local.BaseXConnectionPoolXQDataSource;

import net.xqj.basex.BaseXXQDataSource;
import net.xqj.basex.BaseXConnectionPoolXQDataSource;
import net.xqj.pool.PooledXQDataSource;
import perf.util.Performance;

import javax.xml.xquery.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Properties;

public class MultiThreaded
{
  static final int ITERATIONS = 100000;
  static final boolean EXECUTE_QUERY = false;
  static final int THREADS = 6;

  static final boolean PERFORM_REGULAR_TEST = false;
  static final boolean PERFORM_POOLED_TEST = true;

  public static void main(String[] args) throws Exception
  {
    XQDataSource unpooled = new BaseXXQDataSource();
    unpooled.setProperties(connectionProperties());

    Performance perf = new Performance();

    long memBefore=0, memAfter=0;

    ArrayList<Thread> threads = new ArrayList<Thread>(THREADS);

    if(PERFORM_REGULAR_TEST)
    {
      for(int i=0;i<THREADS;i++)
        threads.add(new Thread(new Runner(unpooled, ITERATIONS, EXECUTE_QUERY)));

      memBefore = Performance.memory();
      perf.time(); // reset timer

      for(Thread thread : threads) thread.start();
      for(Thread thread : threads) thread.join();

      memAfter = Performance.memory();

      formatResult(
        "Regular XQDataSource",
        perf.time(),
        ITERATIONS * THREADS,
        memAfter - memBefore
      );
    }

    threads.clear();

    Performance.gc(100);

    ConnectionPoolXQDataSource cpds = new BaseXConnectionPoolXQDataSource();
    cpds.setProperties(connectionProperties());
    XQDataSource pooled = new PooledXQDataSource(cpds);

    if(PERFORM_POOLED_TEST)
    {
      for(int i=0;i<THREADS;i++)
        threads.add(new Thread(new Runner(pooled, ITERATIONS, EXECUTE_QUERY)));

      memBefore = Performance.memory();
      perf.time(); // reset timer

      for(Thread thread : threads) thread.start();
      for(Thread thread : threads) thread.join();

      memAfter = Performance.memory();

      formatResult(
        "Pooled XQDataSource",
        perf.time(),
        ITERATIONS * THREADS,
        memAfter - memBefore
      );
    }

    threads.clear();

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

  static class Runner implements Runnable
  {
    private final XQDataSource ds;
    private final int times;
    private final boolean executeQuery;

    public Runner(XQDataSource ds, int times, boolean executeQuery)
    {
      this.ds = ds;
      this.times = times;
      this.executeQuery = executeQuery;

    }

    public void run()
    {
      try
      {
        for(int i=0;i<times;i++)
        {
          XQConnection conn = ds.getConnection();

          if(executeQuery)
            conn.prepareExpression("1,2,3,4").executeQuery();

          conn.close();
        }
      }
      catch(Throwable e) {
        e.printStackTrace();
      }
    }
  }


}
