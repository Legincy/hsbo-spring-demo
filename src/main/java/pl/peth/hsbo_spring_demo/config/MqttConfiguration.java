package pl.peth.hsbo_spring_demo.config;

public class MqttConfiguration {
    private String host;
    private int port;
    private int qualityOfService;
    private String clientId;
    private String username;
    private String password;
    private boolean ignoreRandomGenerator;

    public MqttConfiguration() {}

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getQualityOfService() {
        return qualityOfService;
    }

    public void setQualityOfService(int qualityOfService) {
        this.qualityOfService = qualityOfService;
    }

    public boolean isIgnoreRandomGenerator() {
        return ignoreRandomGenerator;
    }

    public void setIgnoreRandomGenerator(boolean ignoreRandomGenerator) {
        this.ignoreRandomGenerator = ignoreRandomGenerator;
    }

    /**
     * Returns the broker URL in the format "tcp://[HOST]:[PORT]".
     *
     * @return the broker URL
     */
    public String getBrokerUrl() {
        return String.format("tcp://%s:%d", host, port);
    }
}
