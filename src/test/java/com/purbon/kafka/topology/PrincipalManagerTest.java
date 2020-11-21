package com.purbon.kafka.topology;

import static com.purbon.kafka.topology.BuilderCLI.BROKERS_OPTION;
import static com.purbon.kafka.topology.TopologyBuilderConfig.TOPOLOGY_EXPERIMENTAL_ENABLED_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.purbon.kafka.topology.actions.Action;
import com.purbon.kafka.topology.actions.accounts.ClearAccounts;
import com.purbon.kafka.topology.actions.accounts.CreateAccounts;
import com.purbon.kafka.topology.model.Impl.ProjectImpl;
import com.purbon.kafka.topology.model.Impl.TopologyImpl;
import com.purbon.kafka.topology.model.Project;
import com.purbon.kafka.topology.model.Topology;
import com.purbon.kafka.topology.model.cluster.ServiceAccount;
import com.purbon.kafka.topology.model.users.Consumer;
import com.purbon.kafka.topology.model.users.Producer;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class PrincipalManagerTest {

  private Map<String, String> cliOps;
  private Properties props;

  @Mock PrincipalProvider provider;

  ExecutionPlan plan;

  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  PrincipalManager principalManager;
  TopologyBuilderConfig config;

  BackendController backendController;

  @Mock PrintStream mockPrintStream;

  @Mock ExecutionPlan mockPlan;

  @Before
  public void before() throws IOException {

    Files.deleteIfExists(Paths.get(".cluster-state"));
    backendController = new BackendController();

    cliOps = new HashMap<>();
    cliOps.put(BROKERS_OPTION, "");
    props = new Properties();

    props.put(TOPOLOGY_EXPERIMENTAL_ENABLED_CONFIG, "true");

    plan = ExecutionPlan.init(backendController, mockPrintStream);
    config = new TopologyBuilderConfig(cliOps, props);
    principalManager = new PrincipalManager(provider, config);
  }

  @Test
  public void testFreshGeneration() throws IOException {

    Topology topology = new TopologyImpl();
    topology.setContext("context");
    Project project = new ProjectImpl("foo");
    project.setConsumers(Collections.singletonList(new Consumer("consumer")));
    project.setProducers(Collections.singletonList(new Producer("producer")));
    topology.addProject(project);

    doNothing().when(provider).configure();

    principalManager.apply(topology, plan);

    Collection<ServiceAccount> accounts =
        Arrays.asList(
            new ServiceAccount(-1, "consumer", "Managed by KTB"),
            new ServiceAccount(-1, "producer", "Managed by KTB"));

    assertThat(plan.getActions()).hasSize(1);
    assertThat(plan.getActions()).containsAnyOf(new CreateAccounts(provider, accounts));
  }

  @Test
  public void testDeleteAccountsRequired() throws IOException {

    Topology topology = new TopologyImpl();
    topology.setContext("context");
    Project project = new ProjectImpl("foo");
    project.setConsumers(Collections.singletonList(new Consumer("consumer")));
    project.setProducers(Collections.singletonList(new Producer("producer")));
    topology.addProject(project);

    doNothing().when(provider).configure();

    doReturn(new ServiceAccount(123, "consumer", "Managed by KTB"))
        .when(provider)
        .createServiceAccount(eq("consumer"), eq("Managed by KTB"));

    doReturn(new ServiceAccount(124, "producer", "Managed by KTB"))
        .when(provider)
        .createServiceAccount(eq("producer"), eq("Managed by KTB"));

    principalManager.apply(topology, plan);
    plan.run();
    assertThat(plan.getServiceAccounts()).hasSize(2);

    topology = new TopologyImpl();
    topology.setContext("context");
    project = new ProjectImpl("foo");
    project.setConsumers(Collections.singletonList(new Consumer("consumer")));
    topology.addProject(project);

    backendController = new BackendController();
    plan = ExecutionPlan.init(backendController, mockPrintStream);
    principalManager.apply(topology, plan);

    Collection<ServiceAccount> accounts =
        Arrays.asList(new ServiceAccount(124, "producer", "Managed by KTB"));

    assertThat(plan.getActions()).hasSize(1);
    assertThat(plan.getActions()).containsAnyOf(new ClearAccounts(provider, accounts));

    plan.run();

    assertThat(plan.getServiceAccounts()).hasSize(1);
    assertThat(plan.getServiceAccounts())
        .contains(new ServiceAccount(123, "consumer", "Managed by KTB"));
  }

  @Test
  public void testNotRunIfConfigNotExperimental() throws IOException {
    props.put(TOPOLOGY_EXPERIMENTAL_ENABLED_CONFIG, "false");

    config = new TopologyBuilderConfig(cliOps, props);
    principalManager = new PrincipalManager(provider, config);

    Topology topology = new TopologyImpl();

    principalManager.apply(topology, mockPlan);

    verify(mockPlan, times(0)).add(any(Action.class));
  }
}
