import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.valvesoftware.source.query.QueryClient;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestQueryClient {
	private static QueryClient client;

	@BeforeClass
	public static void initClient() {
		client = new QueryClient();
	}

	@Test
	public void testQueryServer() throws UnknownHostException, InterruptedException, ExecutionException, TimeoutException {
		System.out.println(client.queryServer(new InetSocketAddress(InetAddress.getByName("85.190.148.213"), 27015)).get(5000, TimeUnit.MILLISECONDS));
	}

	@Test
	public void testQueryPlayers() throws UnknownHostException, InterruptedException, ExecutionException, TimeoutException {
		System.out.println(client.queryPlayers(new InetSocketAddress(InetAddress.getByName("85.190.148.213"), 27015)).get(5000, TimeUnit.MILLISECONDS));
	}

	@Test
	public void testQueryRules() throws UnknownHostException, InterruptedException, ExecutionException, TimeoutException {
		System.out.println(client.queryRules(new InetSocketAddress(InetAddress.getByName("85.190.148.213"), 27015)).get(5000, TimeUnit.MILLISECONDS));
	}

	@AfterClass
	public static void cleanUp() {
		client.shutdown();
	}
}
