import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by andreas.maier on 16.05.17.
 */
public class HazelIT {

    private HazelcastInstance hazelCluster;

    @Before
    public void setUp() {
        // see https://stackoverflow.com/questions/14289845/how-do-you-create-a-hazelcast-instance-embedded-in-process-in-memory-without-ne
        Config config = new Config();
        config.setProperty("hazelcast.shutdownhook.enabled", "false");
        config.getGroupConfig().setName("test").setPassword("test-pw");
        NetworkConfig network = config.getNetworkConfig();
        network.getJoin().getTcpIpConfig().setEnabled(false);
        network.getJoin().getMulticastConfig().setEnabled(false);
        hazelCluster = Hazelcast.newHazelcastInstance(config);
    }

    @Test
    public void test() {

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getGroupConfig().setName("test").setPassword("test-pw");
        clientConfig.getNetworkConfig().addAddress("127.0.0.1");

        // first client writes data to Hazelcast cluster
        HazelcastInstance hazelClient = HazelcastClient.newHazelcastClient(clientConfig);

        IMap<String, String> map = hazelClient.getMap("strings");

        map.put("foo1", "bar1");
        map.put("foo1", "bar1_new");
        map.put("foo2", "bar2");

        hazelClient.shutdown();

        // second client reads data from Hazelcast cluster
        HazelcastInstance hazelClient2 = HazelcastClient.newHazelcastClient(clientConfig);
        IMap<String, String> map2 = hazelClient2.getMap("strings");

        assertEquals("bar1_new", map2.get("foo1"));
        assertEquals("bar2", map2.get("foo2"));
        assertNull(map2.get("foo"));

        hazelClient2.shutdown();
    }

    @After
    public void tearDown() {
        hazelCluster.shutdown();
    }

}






