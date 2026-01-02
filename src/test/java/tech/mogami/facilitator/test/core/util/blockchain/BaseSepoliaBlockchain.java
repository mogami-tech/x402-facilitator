package tech.mogami.facilitator.test.core.util.blockchain;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.util.function.Consumer;

/**
 * Abstract class for Base Sepolia tests.
 * Inherit from it to start and stop the container.
 */
public abstract class BaseSepoliaBlockchain {

    /** Ethereum service name. */
    protected static final String BLOCKCHAIN_SERVICE_NAME = "anvil";

    /** Ethereum service port. */
    protected static final int BLOCKCHAIN_SERVICE_PORT = 8545;

    /** The Docker compose container. */
    protected static DockerComposeContainer<?> container;

    /**
     * Start the container.
     */
    @BeforeAll
    @SuppressWarnings("resource")
    public static void setUp() {
        container = new DockerComposeContainer<>(new File("src/test/resources/docker-compose-base-sepolia.yml"))
                .waitingFor(BLOCKCHAIN_SERVICE_NAME, new LogMessageWaitStrategy().withRegEx(".*Listening on 0.0.0.0:8545.*"))
                .withExposedService(BLOCKCHAIN_SERVICE_NAME, BLOCKCHAIN_SERVICE_PORT)
                .waitingFor("deployer", Wait.forLogMessage(".*\"deployedTo\":.*", 1))
                .withLogConsumer(BLOCKCHAIN_SERVICE_NAME, logConsumer())
                .withLogConsumer("deployer", logDeployer())
                .withLocalCompose(false);
        container.start();
    }

    /**
     * Log consumer to print the logs.
     *
     * @return the log consumer
     */
    private static Consumer<OutputFrame> logConsumer() {
        return outputFrame -> System.out.print(">" + outputFrame.getUtf8String());
    }

    /**
     * Log consumer to print the logs.
     *
     * @return the log consumer
     */
    private static Consumer<OutputFrame> logDeployer() {
        return outputFrame -> System.out.print("Deployer> " + outputFrame.getUtf8String());
    }

    /**
     * Stop the container.
     */
    @AfterAll
    public static void tearDown() {
        if (container != null) {
            container.stop();
        }
    }

}
