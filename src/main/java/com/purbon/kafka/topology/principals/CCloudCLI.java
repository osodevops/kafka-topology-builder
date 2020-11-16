package com.purbon.kafka.topology.principals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Map;

public class CCloudCLI {

  private static final Logger LOGGER = LogManager.getLogger(CCloudCLI.class);

  private ObjectMapper mapper;
  private Runtime run;

  public CCloudCLI() {
    mapper = new ObjectMapper();
    run = Runtime.getRuntime();
  }

  public List<Cluster> clusters() throws IOException {
    try {
      String stdout = run("ccloud kafka cluster list --output json");
      Cluster[] items = mapper.readValue(stdout, Cluster[].class);
      return Arrays.asList(items);
    } catch (IOException | InterruptedException e) {
      LOGGER.error("Something happen during calling ccloud", e);
      throw new IOException(e);
    }
  }

  public Map<String, ServiceAccount> serviceAccounts() throws IOException {
    try {
      String stdout = run("ccloud service-account list --output json");
      ServiceAccount[] items = mapper.readValue(stdout, ServiceAccount[].class);
      return Arrays.asList(items).stream().collect(Collectors.toMap(i -> ""+i.getId(), i->i));
    } catch (IOException | InterruptedException e) {
      LOGGER.error("Something happen during calling ccloud", e);
      throw new IOException(e);
    }
  }

  private ServiceAccount newServiceAccount(String name, String description) throws IOException {
    //String cmd = "ccloud service-account create "+name+" --description \""+description+"\" --output json 2>&1";
    try {
      ProcessBuilder pb = new ProcessBuilder("ccloud", "service-account", "create "+name, "--description", description, "--output json");
      Process pr = pb.start();
      pr.waitFor();
      String stdout = readStdOut(pr);
      ServiceAccount sa = mapper.readValue(stdout, ServiceAccount.class);
      return sa;
    } catch (IOException | InterruptedException e) {
      LOGGER.error("Something happen during calling ccloud", e);
      throw new IOException(e);
    }
  }

  public void setEnvironment(String environment) throws IOException {
    try {
      run("ccloud environment use " + environment);
    } catch (IOException | InterruptedException e) {
      LOGGER.error("Something happen during setting an environment", e);
      throw new IOException(e);
    }
  }

  private String run(String cmd) throws IOException, InterruptedException {
    Process pr = run.exec(cmd);
    pr.waitFor();
    return readStdOut(pr);
  }

  private String readStdOut(Process pr) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
    StringBuilder sb = new StringBuilder();
    String line = "";
    while ((line = br.readLine()) != null) {
     sb.append(line);
     sb.append("\n");
    }
    return sb.toString();
  }

  public static void main(String[] args) throws IOException {
    CCloudCLI cli = new CCloudCLI();
    cli.setEnvironment("env-j9wgp");

    ServiceAccount sa = cli.newServiceAccount("ktb", "created by KTB");
    System.out.println(sa);

  }
}
