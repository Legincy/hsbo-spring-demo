package pl.peth.hsbo_spring_demo.config;


import java.util.UUID;

/**
 * This class represents the configuration for the MQTT client.
 * It contains properties such as host, port, clientId, username, password, and topics.
 * The class also provides methods to get and set these properties.
 */
public class MqttConfiguration {
    private String host;
    private int port;
    private int qualityOfService;
    private String clientId;
    private String username;
    private String password;
    private String[] topics;

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

    /**
     * This method returns either a pre-configured client ID or generates a new one
     * with the pattern "mqtt-client-__UUID__".
     *
     * @return a String representing the client ID.
     */
    public String getClientId(){
        if (this.clientId == null || this.clientId.isEmpty()) {
            this.clientId = "mqtt-client-" + UUID.randomUUID();
        } else if (this.clientId.endsWith("-")) {
            this.clientId = clientId + UUID.randomUUID();
        }

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

    public String[] getTopics() {
        return topics;
    }

    public void setTopics(String[] topics) {
        this.topics = topics;
    }

    public int getQualityOfService() {
        return qualityOfService;
    }

    public void setQualityOfService(int qualityOfService) {
        this.qualityOfService = qualityOfService;
    }

    /**
     * This method returns the broker URL in the format "tcp://__HOST__:__PORT__".
     *
     * @return a String representing the broker URL.
     */
    public String getBrokerUrl() {
        return String.format("tcp://%s:%d", host, port);
    }
}
